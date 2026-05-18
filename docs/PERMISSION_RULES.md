# 权限规则 — 卓然舆情

## 登录/认证方式

### Web 页面登录
1. 用户通过登录页面输入手机号+密码+图形验证码
2. 服务端验证后，通过 `UserUtil.setUser()` 将用户信息写入 session
3. 同时生成 JWT token，写入 Cookie（name=token）
4. 后续请求通过 `LoginHandlerInterceptor` 从 Cookie 或 URL 参数获取 token 验证
5. Cookie path= `/`，maxAge = token 过期时间

### API Token 登录
1. POST `/api/getToken` 或 POST `/user/getToken` — 传入用户名+密码
2. 验证成功后返回 JWT token 字符串
3. 后续 API 请求在请求头中携带 `token`（header name = "token"）
4. 或通过 URL 参数 `?token=xxx` 传递

### 微信扫码登录
1. 用户扫码后通过 `/wechat/checkLogin` 接口验证
2. 或通过 `/wechatJumpLogin` 直接在浏览器登录
3. 使用 SHA256 签名验证身份

## Token / Session / Cookie 使用情况

| 机制 | 用途 | 实现 |
|------|------|------|
| JWT Token | API 鉴权 + Web 页面鉴权 | nimbus-jose-jwt HS256 |
| Session | 用户状态存储 | Spring Session + Redis |
| Cookie | Web 页面 token 传递 | name=token, path=/ |
| 请求头 | API token 传递 | header name=token |
| URL 参数 | 紧急/特殊场景 token 传递 | `?token=xxx` |

### Token 内容
JWT payload 包含：
- `user_id`：用户ID
- `telephone`：手机号
- `tokenIssueTime`：签发时间
- （可能包含更多 UserDTO 字段）

### Token 验证流程（LoginHandlerInterceptor）
1. 检查 URL 参数中的 token → 验证有效则通过
2. 检查 Cookie 中的 token → 验证有效则通过
3. 验证失败 → 重定向到 `/login` 或返回 403（对于 AJAX 请求）
4. 验证通过 → 将 UserDTO 设置到 Context 线程上下文

### 白名单路径（无需登录）
- `/login`（GET/POST）
- `/api/getToken`
- `/user/getToken`
- [待确认] `/wechat/*` 部分接口
- [待确认] 静态资源路径

## 用户模型（User Entity）

| 字段 | 类型 | 说明 |
|------|------|------|
| user_id | Long | 用户唯一ID（Snowflake） |
| telephone | String | 手机号（登录名） |
| password | String | MD5 加密密码 |
| status | Integer | 0=禁止/1=正常/2=注销 |
| identity | Integer | 身份标识（代码硬编码为 1，未参与任何权限逻辑，属预留字段） |
| user_type | Integer | 用户类型（有字段但代码中从未使用，属预留字段） |
| user_level | Integer | 用户级别（有字段但代码中从未使用，属预留字段） |
| organization_id | String | 所属机构ID |
| wechatflag | Integer | 微信绑定标志 |
| term_of_validity | Date | 账号有效期 |
| nlp_flag | Integer | NLP 服务绑定状态 |
| xie_flag | Integer | 写作宝服务绑定状态 |
| mail_json | String | 邮箱配置JSON |
| username | String | 用户名（显示名称） |
| openid | String | 微信 OpenID |

## 角色模型

当前项目存在两套权限现状：

1. **旧系统模块**：仍没有正式角色模型。`user.identity`、`user_type`、`user_level` 基本属于预留字段，未形成统一权限控制。
2. **校园模块**：已在 `V1.11__CampusPermissionTables.sql` 起建立最小角色模型，并在后续迁移中持续补充菜单和 API 权限。

校园模块内置角色：

| 角色编码 | 角色名称 | 说明 |
|----------|----------|------|
| `campus_admin` | 校园系统管理员 | 默认拥有校园系统全部菜单和接口权限 |
| `campus_operator` | 舆情处置员 | 可处理线索、预警、事件、检测、报告等业务 |
| `campus_viewer` | 校园查看员 | 可查看工作台、态势和报告等只读能力 |

## 权限模型

旧系统仍没有统一权限模型，主要依赖登录拦截和按 `user_id` 拼接查询条件。

校园模块已有轻量权限模型，未引入 Shiro / Spring Security，而是使用项目内表和拦截器实现：

- `campus_permission_role`：角色
- `campus_permission_menu`：菜单/按钮权限
- `campus_permission_api`：API 权限
- `campus_user_role`：用户角色关系
- `campus_role_menu`：角色菜单关系
- `campus_role_api`：角色 API 关系
- `CampusPermissionInterceptor`：拦截 `/campus/**` API 并调用 `CampusPermissionService`

## 菜单权限 / API 权限 / 数据权限

### 当前状态
- **旧系统菜单权限**：无。旧 Thymeleaf 页面所有登录用户看到基本相同菜单
- **旧系统 API 权限**：仅有登录拦截（token 校验），无方法级权限控制
- **旧系统数据权限**：通过 `user_id` 字段控制用户只能看到自己的数据
  - 方案/方案组按 `user_id` 隔离
  - 报告按 `user_id` 隔离
  - 收藏按 `user_id` 隔离
  - 预警设置按 `user_id` 隔离（通过 `project` → `user_id`）
- **校园菜单权限**：通过 `campus_permission_menu` + `campus_role_menu` 控制，并由 `campus-web` 侧栏消费后端菜单树
- **校园 API 权限**：通过 `campus_permission_api` + `campus_role_api` 控制 `/campus/**` 接口
- **校园数据权限**：当前以学校/角色最小边界为主，仍需按学校正式组织和数据范围继续细化

### P0-P2 新增权限点

| 模块 | 菜单/接口 | 权限码 | 说明 |
|------|-----------|--------|------|
| 监测任务管理 | `/admin/monitor-tasks` | `campus:monitor:view` | 后台监测任务配置维护入口，复用监测任务菜单权限 |
| 监测结果 | `/campus/monitor/result/list` | `campus:monitor:read` | 支持语言、平台、转线索状态筛选 |
| 监测结果 | `/campus/monitor/result/convert-clue` | `campus:monitor:operate` | 监测命中转入线索库 |
| 监测结果 | `/campus/monitor/result/alert-cleanup/preview` | `campus:monitor:read` | 疑似误预警候选预览，复用 `/campus/monitor/**` 只读通配权限 |
| 监测结果 | `/campus/monitor/result/alert-cleanup/execute` | `campus:monitor:operate` | 疑似误预警批量取消，复用 `/campus/monitor/**` 写操作通配权限 |
| 任务内重点目标 | `/campus/monitor/watch-target/**` | `campus:monitor:read` / `campus:monitor:operate` | 重点账号、指定链接、从结果一键加入 |
| 事件处置 | `GET /campus/event/clue/suggest` | `campus:event:read` | 相似线索推荐，复用 `GET /campus/event/**` 只读权限 |
| 事件处置 | `POST /campus/event/**` | `campus:event:operate` | 定级、分派、反馈、退回、复核、归档均由后端状态前置校验保护 |
| 数据接入 | `/campus/ingest/task/save/run` | `campus:ingest:view` / `campus:ingest:operate` | 接入后端能力保留；客户后台暂不展示“数据接入”菜单，`/admin/ingest` 重定向到 `/admin/monitor-tasks` |
| AI 能力管理 | `/admin/settings/ai` | `campus:ai:view` | 后台 AI 能力管理入口，仅管理员默认拥有 |
| AI 能力管理 | `GET /campus/ai/**` | `campus:ai:read` | 查询供应商、模型、功能绑定、提示词和调用日志 |
| AI 能力管理 | `POST /campus/ai/**` | `campus:ai:operate` | 保存供应商/模型/功能/提示词和测试供应商配置 |
| 教育专题 | `/admin/education` | `campus:education:view` | 后台教育专题入口 |
| 教育专题 | `GET /campus/education/**` | `campus:education:read` | 教育新闻/政策/招生专题、学校排名、学校导入模板 |
| 教育专题 | `POST /campus/education/**` | `campus:education:operate` | 学校主体维护、学校导入、百度接入任务创建/立即运行 |

`campus_admin` 仍通过管理员角色短路拥有全部校园 API；`campus_operator` 可获得教育专题读写；`campus_viewer` 只应获得教育专题只读。新增 API 必须同步补迁移脚本中的 `campus_permission_api` 和角色绑定。

## 后端权限校验位置

### 登录拦截
- `LoginHandlerInterceptor.preHandle()` — 所有请求的 token 校验
- `WebConfigurer` — 注册拦截器

### 校园 API 权限
- `CampusPermissionInterceptor.preHandle()` — `/campus/**` 接口权限入口
- `CampusPermissionServiceImpl.hasApiPermission()` — 判断当前用户是否拥有 API 权限
- `CampusPermissionServiceImpl.menuTree()` — 返回当前用户可见菜单树

### 数据隔离
- 各 Service/Controller 中通过 `userUtil.getUserId(request)` 获取当前用户ID
- 查询条件中拼入 `user_id` 参数

## 前端权限控制位置

### Thymeleaf 前端
- 无前端权限控制（服务端渲染）

### Vue 大屏
- 无前端权限控制

### 校园 Vue 前端
- `campus-web` 通过权限接口获取菜单/角色/接口权限信息
- 进入受保护页面前，`campus-web` 先调用 `/campus/system/current-user` 校验真实 session；失败时清理本地登录标记并跳转登录页
- 后台管理入口会按当前用户菜单/权限过滤：重点账号、监测任务管理、AI能力、教育专题、系统设置；权限加载失败或过滤为空时不再 fallback 展示全部后台菜单
- 前台侧边栏过滤所有后台/管理路由；没有后台菜单权限的账号隐藏“后台管理”；数据接入客户入口暂时隐藏
- 前台侧边栏只保留一个“舆情态势”入口；历史 `/situation` 态势大屏菜单保留权限和路由兼容，但菜单 `visible=0`，由 `/` 页内“大屏模式”进入
- 后台监测任务管理页使用 `campus:monitor:operate` 控制新增、编辑、启停、前台展示开关、删除、手动运行和重点目标维护按钮；不展示具体监测内容、命中结果或运行日志
- 监测任务普通表单不再授权人工选择接入任务；自动接入由 `campus_monitor` 通过 `campus_ingest` Service 编排，页面只展示自动绑定任务和最近错误作为高级诊断信息
- AI 能力管理页使用 `campus:ai:view` 控制后台入口；供应商/模型/提示词等保存接口最终以后端 `campus:ai:operate` 为准，不在前端保存真实 API Key；如果 `/campus/ai/**` 查询失败，页面应保留壳层并展示加载失败原因
- 监测结果页会用 `campus:monitor:operate` 控制转线索、转预警、取消预警、疑似误预警治理、重点账号/链接维护按钮
- 报告模板前台独立菜单 `/report-templates` 仍使用 `campus:report:view`；模板新增、编辑、删除继续复用 `/campus/report/template/**`，最终以后端 `campus:report:operate` 为准
- 前台不再展示独立“线索库”菜单；历史 `/clues` 入口仅保留兼容重定向到 `/monitor`。`campus:clue:read` / `campus:clue:operate` 仍用于线索 API，不因此放宽或移除。
- 教育专题页会用 `campus:education:operate` 控制学校新增/编辑/删除/导入和百度任务创建/立即运行按钮
- 前端权限只做体验约束，最终仍以后端 `CampusPermissionInterceptor` 为准

## 高风险权限点

1. **POST /user/save**（新增用户）
   - 当前无权限限制，任何登录用户均可调用
   - 可创建任意账号

2. **POST /project/commitproject**（提交方案）
   - 发送数据到 Kafka + 调用外部 API
   - 无权限隔离

3. **系统日志查看**
   - [待确认] 是否有日志查看权限

4. **Token 签发**
   - `/api/getToken` 和 `/user/getToken` 无登录拦截
   - 仅依赖密码验证

5. **账号管理**
   - [待确认] 是否可以跨用户查看和修改

6. **校园权限边界**
   - 校园模块已有最小角色模型，但学校正式角色矩阵、部门/学院数据范围和值班/研判/处置/领导/审计边界仍需确认

7. **按钮级权限**
   - 后端已有 API 权限拦截，前端按钮级隐藏还不完整，低权限角色可能看到部分不可操作按钮

## 权限变更规则

1. 任何涉及认证/授权逻辑的修改，必须先在 PERMISSION_RULES.md 中评估影响
2. 子线程修改权限相关代码前必须向主控说明
3. 新增 API 默认需要 token 鉴权（除非明确公开）
4. Odoo 式新模块必须在 manifest 中声明菜单权限、API 权限和默认角色影响
5. 重点账号、指定链接、DPI 或第三方推送账号必须保留来源依据和授权范围，禁止无来源入库

## 子线程修改权限相关代码的限制

1. 子线程禁止修改 LoginHandlerInterceptor 的逻辑（除非专门任务）
2. 子线程禁止修改 UserUtil/JWTUtils 等核心鉴权工具类（除非专门任务）
3. 子线程新增 API 时，必须评估是否需要鉴权

## 当前缺口

1. **旧系统无角色体系**：旧接口仍无法完整实现不同角色不同权限
2. **旧系统无方法级权限控制**：大量非校园接口仍是“登录即有权”
3. **校园权限模型仍需学校化**：已有 `campus_admin` / `campus_operator` / `campus_viewer`，但正式角色矩阵和数据范围需学校确认
4. **按钮级权限不完整**：后端拒绝未授权 API，前端仍需进一步隐藏不可操作按钮
5. **操作审计仍需增强**：当前审计覆盖校园关键操作，但操作前后状态和失败细节仍不完整
6. **旧用户管理接口无权限**：`POST /user/save` 仍需限制为管理员
7. **密码传输与存储风险**：密码在传输中依赖 HTTPS，存储仍是旧 MD5 模式
8. **Token 无刷新机制**：过期后必须重新登录

## 上线前必须补齐的权限检查项

- P0：确认学校正式角色矩阵、部门/学院数据范围和值班责任人
- P0：新增用户接口必须限制为管理员可调用
- P0：确认 `campus_operator` / `campus_viewer` 的菜单和 API 是否符合学校试运行要求
- P1：完善校园前端按钮级权限控制
- P1：旧系统方法级权限控制（如 @PreAuthorize 或项目内拦截器扩展）
- P2：操作审计日志增强（记录操作前后状态）
- P2：密码传输加密（推荐 HTTPS + BCrypt）
- P2：Token 刷新机制
