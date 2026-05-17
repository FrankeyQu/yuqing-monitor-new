# Batch33 预警中心 405 排查与监测任务后台详细设置实施方案

## 1. 背景

当前校园前端已经把 `/monitor` 收口为“监测信息工作台”，用于统一展示监测命中和线索数据；但监测任务自身的完整配置入口没有同步迁移出来：

- 前台只保留了“快速创建任务”弹窗。
- 后端已有监测任务列表、保存、启停、删除、运行、运行日志、重点账号/链接维护等接口。
- `MonitorView.vue` 中存在一块“监测结果 + 本任务重点账号/链接维护”界面，但被 `v-if="false"` 硬隐藏，用户无法从页面维护任务范围。
- `docs/campus-web-runbook.md` 仍描述 `/monitor` 包含任务配置、运行日志等能力，已与实际页面不完全一致。

同时，预警中心存在 405 报错现象。当前代码扫描结果显示：

- `/campus/alert/list` 后端只支持 `GET`，前端预警中心列表当前也使用 `GET`。
- `/campus/alert/handle` 后端只支持 `POST`，前端处理/忽略当前也使用 `POST`。
- `campusBusiness.ts` 中存在 `POST /campus/alert/create-from-clue` 调用，文档也记录了该接口，但 `CampusAlertController` 目前没有对应方法。
- 旧版 `/system/warning*` 页面已下线，旧接口多数只支持 `POST`；如果旧页面或残留链接以 `GET` 调用旧数据接口，也可能触发 405。

本批次目标是先把 405 排查收口，再新增后台“监测任务详细设置”页面，补齐任务删改、启停、运行、接入绑定、重点目标和运行日志入口。

## 2. 模块归属

本批次不新增业务模块，归属如下：

| 能力 | 归属模块 | 说明 |
| --- | --- | --- |
| 预警中心 405 排查 | `campus_monitor` / `campus_alert` | 监测来源告警走 `/campus/monitor/alert/**`，通用预警中心走 `/campus/alert/**` |
| 监测任务后台设置页 | `campus_monitor` | 维护 `campus_monitor_task`、`campus_monitor_ingest_task_relation`、`campus_monitor_watch_target` |
| 接入任务选择 | `campus_ingest` | 只读取合法接入任务，不在监测模块创建外部接入 |
| 权限入口 | 校园权限模块 | 复用 `campus:monitor:view/read/operate`，如新增菜单需补初始化数据 |

边界要求：

- 监测任务不能直接调用外部平台 API，只扫描已进入 `campus_ingest` 的合法接入记录。
- 重点账号/指定链接必须保留授权或来源说明。
- Controller 只做参数接收和 Service 调用，不在新增页面需求中引入 Controller 业务逻辑扩散。

## 3. 本批目标

### 3.1 预警中心 405 收口

1. 复现并记录 405 的完整请求路径、请求方法、页面入口和触发操作。
2. 对照后端映射修正前端调用方法或路径。
3. 补齐 `/campus/alert/create-from-clue` 的处理策略：
   - 优先方案：后端新增兼容端点，内部复用现有 `CampusAlertService.evaluateClue(...)`。
   - 备选方案：前端 `createAlertFromClue` 改为调用 `/campus/alert/evaluate-clue`，同时修正文档。
4. 检查部署层 Vue history fallback，避免 `/alerts` 刷新或深链进入时落到后端旧页面或错误接口。

### 3.2 监测任务后台详细设置页

新增后台入口 `/admin/monitor-tasks`，用于完整维护监测任务：

- 查询任务列表。
- 新增/编辑任务。
- 删除任务。
- 启用、暂停、禁用任务。
- 手动运行任务。
- 配置自动扫描、频率、告警模式。
- 绑定接入任务。
- 管理任务内重点账号和指定链接。
- 查看任务运行日志。

前台 `/monitor` 保持“监测信息工作台”定位，不再承载复杂任务维护。

## 4. 不做范围

- 不改监测任务核心表结构。
- 不新增外部平台抓取能力。
- 不保存 API Key、Cookie、Token、代理、设备指纹或签名参数。
- 不重构现有 `MonitorView.vue` 的线索处理主流程。
- 不一次性迁移旧 `/system/warning` 预警配置页面。
- 不调整校园权限拦截器核心逻辑。

## 5. 现有接口复用清单

### 5.1 监测任务

| 方法 | 路径 | 用途 |
| --- | --- | --- |
| `GET` | `/campus/monitor/task/list` | 任务列表 |
| `POST` | `/campus/monitor/task/save` | 新增/编辑任务 |
| `POST` | `/campus/monitor/task/update-status` | 启用/暂停/禁用 |
| `POST` | `/campus/monitor/task/delete` | 删除任务 |
| `POST` | `/campus/monitor/task/run` | 手动运行 |
| `GET` | `/campus/monitor/task/run-log/list` | 运行日志 |

### 5.2 重点账号/链接

| 方法 | 路径 | 用途 |
| --- | --- | --- |
| `GET` | `/campus/monitor/watch-target/list` | 重点目标列表 |
| `POST` | `/campus/monitor/watch-target/save` | 新增/编辑重点目标 |
| `POST` | `/campus/monitor/watch-target/delete` | 删除重点目标 |

### 5.3 接入任务选择

| 方法 | 路径 | 用途 |
| --- | --- | --- |
| `GET` | `/campus/ingest/task/list` | 选择可绑定的接入任务 |

### 5.4 预警中心

| 方法 | 路径 | 用途 | 当前风险 |
| --- | --- | --- | --- |
| `GET` | `/campus/alert/list` | 预警列表 | 前端必须保持 GET |
| `POST` | `/campus/alert/handle` | 处理/忽略预警 | 前端必须保持 POST |
| `POST` | `/campus/alert/evaluate-clue` | 线索触发预警评估 | 已实现 |
| `POST` | `/campus/alert/create-from-clue` | 从线索产生告警 | 文档和前端存在，后端缺失 |

## 6. 前端实施设计

### 6.1 路由和导航

新增路由：

```text
/admin/monitor-tasks
```

建议页面文件：

```text
campus-web/src/views/admin/MonitorTaskAdminView.vue
```

如果不新增 `views/admin` 目录，也可放在：

```text
campus-web/src/views/MonitorTaskAdminView.vue
```

后台导航新增“监测任务”：

- 路径：`/admin/monitor-tasks`
- 权限：`campus:monitor:view`
- 与“检测任务”“数据接入”同级。

### 6.2 页面布局

页面采用后台管理风格，不做营销化页面。

建议结构：

1. 顶部筛选栏
   - 关键词：任务名称、主体、关键词。
   - 状态：启用、暂停、禁用。
   - 平台：按 `platformScope` 模糊筛选。
   - 按钮：查询、重置、新增任务。

2. 任务列表表格
   - 任务名称。
   - 监测主体。
   - 平台范围。
   - 关键词/负面词摘要。
   - 接入任务绑定摘要。
   - 自动扫描状态。
   - 扫描频率。
   - 告警模式。
   - 任务状态。
   - 上次运行时间。
   - 下次运行时间。
   - 操作：编辑、启停、运行、重点目标、运行日志、删除。

3. 新增/编辑抽屉
   - 基础信息：任务名称、监测主体、主体别名、备注。
   - 关键词：通用关键词、中文/蒙语/维语关键词。
   - 负面词：通用负面词、中文/蒙语/维语负面词。
   - 排除词：通用排除词、多语言排除词。
   - 平台范围：多选平台。
   - 接入任务绑定：多选已有接入任务。
   - 调度：自动扫描、扫描频率、告警模式、状态。

4. 任务详情侧栏或 Tab
   - 重点账号/指定链接维护。
   - 运行日志。
   - 最近监测结果入口，可跳转 `/monitor` 并携带任务筛选参数。

### 6.3 交互约束

- 删除任务前二次确认。
- 禁用任务后不允许立即运行。
- 自动扫描打开时必须显示扫描频率和下次运行时间。
- 保存任务时前端提示不得填写密钥、Cookie、Token 等敏感配置。
- `campus:monitor:operate` 不足时隐藏或禁用写操作按钮。
- 只读用户可以查看任务、重点目标和日志，但不能保存、删除、启停或运行。

## 7. 后端实施设计

本批优先复用既有 `CampusMonitorController` 和 `CampusMonitorService`。

预期不需要新增监测任务 API；仅当 405 排查确认前后端契约缺口时，补以下兼容接口：

```text
POST /campus/alert/create-from-clue
```

建议实现口径：

- 参数：`clueId`
- 返回：`ResultVO<List<CampusAlert>>`
- 行为：复用 `campusAlertService.evaluateClue(clueId, operatorUserId)`
- 审计：模块“预警中心”，动作“从线索生成预警”

说明：

- 该接口已在 `docs/API_CONTRACT.md` 出现，补后端方法属于补齐既有契约，不改变前端调用方语义。
- 如最终改为前端调用 `/campus/alert/evaluate-clue`，必须同步删除或修正文档中的 `/create-from-clue` 记录。

## 8. 权限与菜单

现有权限：

- `campus:monitor:view`
- `campus:monitor:read`
- `campus:monitor:operate`

如新增后台菜单初始化数据，需补 Flyway 迁移：

- 菜单：`/admin/monitor-tasks`
- 权限码：`campus:monitor:view`
- 默认角色：
  - `campus_admin`：可见、可读、可操作。
  - `campus_operator`：可见、可读、可操作。
  - `campus_viewer`：可见、只读。

如果只在前端静态后台导航中新增入口，但不补菜单种子，管理员仍可能可见，普通角色依赖前端 fallback，正式试运行不推荐这种做法。

## 9. 文档影响

需要同步更新：

- `docs/API_CONTRACT.md`
  - 如补 `/campus/alert/create-from-clue` 后端实现，校准返回值。
  - 细化 `/campus/monitor/task/**` 参数和响应。

- `docs/PERMISSION_RULES.md`
  - 新增后台监测任务菜单说明。
  - 明确 operator/viewer 在后台任务页面的按钮权限差异。

- `docs/STATE_MACHINE.md`
  - 如果不新增状态，不需要改状态机。
  - 如新增“恢复删除”或“重新打开告警”，必须补状态流转。

- `docs/modules/campus_monitor/manifest.md`
  - 增加后台任务详细设置页说明。

- `docs/campus-web-runbook.md`
  - 修正 `/monitor` 和 `/admin/monitor-tasks` 的职责边界。

- `docs/AI_PROGRESS.md`
  - 记录本批文档、代码、验证和遗留风险。

## 10. 建议任务拆分

### 子任务 A：405 精确复现与契约修正

授权范围：

- `campus-web/src/services/*`
- `campus-web/src/views/AlertView.vue`
- `campus-web/src/views/JudgmentView.vue`
- `src/main/java/com/stonedt/intelligence/controller/campus/CampusAlertController.java`
- `docs/API_CONTRACT.md`
- `docs/AI_PROGRESS.md`

验收：

- 能列出 405 的 URL、Method、触发页面和修正点。
- `/campus/alert/list`、`/campus/alert/handle`、`/campus/alert/create-from-clue` 或替代接口无方法错配。

### 子任务 B：后台监测任务列表与表单

授权范围：

- `campus-web/src/router/index.ts`
- `campus-web/src/layouts/AdminLayout.vue`
- `campus-web/src/services/monitor.ts`
- `campus-web/src/views/MonitorTaskAdminView.vue` 或 `campus-web/src/views/admin/MonitorTaskAdminView.vue`
- `campus-web/src/types/api.ts`

验收：

- 后台有“监测任务”入口。
- 可以查询、新增、编辑、删除、启停、运行任务。
- 低权限用户按钮受 `campus:monitor:operate` 控制。

### 子任务 C：重点目标和运行日志

授权范围：

- `campus-web/src/views/MonitorTaskAdminView.vue` 或同一后台页面文件。
- `campus-web/src/services/monitor.ts`
- `campus-web/src/types/api.ts`

验收：

- 可按任务查看重点账号/指定链接。
- 可新增、编辑、启停、删除重点目标。
- 可查看任务运行日志。

### 子任务 D：权限种子和文档收口

授权范围：

- `src/main/resources/db/migration/*`
- `docs/API_CONTRACT.md`
- `docs/PERMISSION_RULES.md`
- `docs/modules/campus_monitor/manifest.md`
- `docs/campus-web-runbook.md`
- `docs/TEST_CHECKLIST.md`
- `docs/AI_PROGRESS.md`

验收：

- 新后台菜单在角色菜单树中可控。
- API 权限、菜单权限和文档一致。
- 测试清单记录本批验证。

## 11. 验收清单

### 11.1 后端

```powershell
.\mvnw.cmd -DskipTests compile
```

如涉及预警接口补齐，建议追加：

```powershell
.\mvnw.cmd test -DskipTests=false
```

手工接口验证：

- `GET /campus/alert/list`
- `POST /campus/alert/handle`
- `POST /campus/alert/create-from-clue` 或 `POST /campus/alert/evaluate-clue`
- `GET /campus/monitor/task/list`
- `POST /campus/monitor/task/save`
- `POST /campus/monitor/task/update-status`
- `POST /campus/monitor/task/run`
- `POST /campus/monitor/task/delete`
- `GET /campus/monitor/watch-target/list`
- `POST /campus/monitor/watch-target/save`
- `POST /campus/monitor/watch-target/delete`
- `GET /campus/monitor/task/run-log/list`

### 11.2 前端

```powershell
cd campus-web
npm run build
```

手工页面验证：

1. 打开 `/alerts`，列表加载不再出现 405。
2. 在预警中心处理和忽略预警，状态可更新。
3. 从舆情研判生成告警，不再调用不存在的后端接口。
4. 打开 `/admin/monitor-tasks`，任务列表可加载。
5. 新增监测任务后能在列表看到。
6. 编辑任务后字段回显和保存正确。
7. 启用、暂停、禁用状态正确流转。
8. 禁用任务不能手动运行。
9. 删除任务为软删除，列表不再显示。
10. 重点账号/链接可维护。
11. 运行日志可查看。
12. viewer 角色只能看，不能操作。

### 11.3 部署层

检查 Nginx / 前端部署：

- `/campus/**` 必须代理到 Spring Boot。
- Vue history fallback 必须让 `/alerts`、`/monitor`、`/admin/monitor-tasks` 刷新返回前端 `index.html`。
- 不能让 `/alerts`、`/admin/**` 深链落入旧 Spring MVC 页面。

## 12. 风险与回滚

风险：

- 新后台页面如果复用过多 `MonitorView.vue` 逻辑，可能把线索处理工作台再次复杂化。
- 直接改 `/monitor` 页面可能影响当前已上线的统一监测信息列表。
- 补权限种子时如菜单路径或权限码不一致，会导致 operator/viewer 看不到入口或按钮。
- 若选择删除 `/campus/alert/create-from-clue` 文档而不补兼容接口，现有前端调用必须同步修改，否则会继续失败。

回滚策略：

- 前端新增页面可以通过路由和后台导航移除快速回滚，不影响现有 `/monitor`。
- 后端如只补兼容接口，回滚风险低；如修改现有接口方法或路径，必须按 Breaking Change 流程处理。
- Flyway 菜单权限种子为幂等插入，回滚时可将菜单 `visible/status` 置为 0，不删除历史记录。

## 13. 推荐实施顺序

1. 先复现并修复预警中心 405，避免带着已知接口错位继续扩展页面。
2. 新增后台监测任务页面骨架和路由。
3. 完成任务列表、新增、编辑、删除、启停、运行。
4. 接入任务绑定选择和权限控制。
5. 增加重点目标维护和运行日志。
6. 补菜单权限种子、文档和测试清单。
7. 完成后端 compile、前端 build 和手工验收。
