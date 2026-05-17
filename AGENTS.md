# AGENTS.md — 卓然舆情 AI 协作规范

## 总体原则

1. **AI 是辅助角色**：所有代码变更最终由人类开发者审核确认
2. **知情同意**：重大架构决策必须提前说明影响范围并得到确认
3. **文档驱动**：每次变更前理解相关文档，变更后更新对应文档
4. **最小变更**：只改需要改的代码，不引入无关优化
5. **可追溯**：所有 AI 生成的变更必须记录在 AI_PROGRESS.md 中

## Odoo 式模块化约束

1. 所有新业务必须先确认模块归属，优先落入 `docs/modules/*/manifest.md` 已声明模块。
2. 新增模块或跨模块能力必须先补 manifest，写清业务边界、依赖模块、数据模型、API、权限、测试和文档影响。
3. 跨模块调用必须通过 Service/API 层，不允许 Controller 直接拼接其它模块的数据写入。
4. 模块间共享数据必须有明确来源：接入数据归 `campus_ingest`，监测命中归 `campus_monitor`，可用于报表分析的数据归 `campus_clue` 或事件模块。
5. 新业务提交前必须同步检查：manifest、权限种子、API 契约、状态机、测试清单、AI_PROGRESS.md。
6. 子线程授权范围必须写到模块或文件级别，不能以“校园模块全部”作为宽泛授权。

## 主控 Agent 规则

1. 负责项目级任务拆分、分发、审核、合并协调
2. 主控 Agent **不直接编写大规模业务代码**
3. 主控 Agent 负责维护文档体系的一致性
4. 主控 Agent 管理 Git 状态和分支策略
5. 主控 Agent 在拆分任务时，必须为每个子线程明确：
   - 授权修改的目录/文件范围
   - 任务目标和验收标准
   - 禁止触碰的模块
6. 主控 Agent 必须审核每个子线程的交付物

## 子线程 Agent 规则

1. 子线程 Agent **只在主控授权的目录和文件范围内修**改
2. 子线程 Agent 不得修改未授权的模块
3. 子线程 Agent 修改公共层代码前，必须先通知主控说明影响范围
4. 子线程 Agent 完成工作后，必须向主控提交完成报告
5. 子线程 Agent 不得创建或切换 Git 分支
6. 子线程 Agent 不得执行 git push、git merge、git reset 等操作
7. 子线程 Agent 在不确定时应暂停并向主控提问

## 多线程 / SubAgent 协作规则

1. 子线程默认在主控所在的同一个 worktree 中工作
2. 如果子线程需要独立分支 → 由主控创建独立的 worktree
3. 子线程之间不直接通信，统一由主控协调
4. 子线程任务按文件目录划分边界，减少文件冲突
5. 高冲突风险的任务串行执行，低冲突风险的任务并行执行
6. 子线程写文件前检查文件是否被其他子线程锁定

## Worktree / Branch 规则

1. 功能开发必须在独立分支上进行
2. 主控 Agent 使用 `EnterWorktree` 创建隔离开发环境
3. **未确认 merge 前禁止删除 worktree**
4. 禁止在非本任务创建的 worktree 中修改代码
5. worktree 的 branch 命名规范：`claude/<描述性名称>`
6. worktree 任务完成后，由主控审核通过后执行 merge

## Commit / Push / Merge 规则

1. **禁止 git reset --hard**（会丢失 AI 的工作成果）
2. **禁止粗暴删除 worktree 目录**（必须通过 ExitWorktree 操作）
3. 禁止 --no-verify 跳过 hooks
4. commit message 必须说明变更内容
5. merge 前必须确认无冲突
6. merge 到主分支前必须通过基础编译检查

## Controller / Service / Model 分层规则

```
Controller 层：
  - 接收 HTTP 请求，解析参数
  - 调用 Service 层
  - 返回 JSON 或 ModelAndView
  - 不应包含业务逻辑
  - 应使用 @SystemControllerLog 记录关键操作

Service 层：
  - 业务逻辑实现
  - 调用 DAO 层
  - 事务管理（@Transactional）
  - 跨模块调用通过 Service 接口

DAO 层：
  - MyBatis Mapper 接口
  - 对应 mapper/*.xml SQL 定义
  - 不应包含业务逻辑

Entity 层：
  - 对应数据库表结构
  - 使用 Lombok @Data（新代码）或手写 getter/setter（存量代码）

DTO / VO 层：
  - DTO：数据传输对象，用于 API 请求/响应
  - VO：视图对象，用于页面模板渲染
  - 不应直接暴露 Entity 给前端
```

⚠️ **当前项目现状**：大量 Controller 直接包含业务逻辑，Service 层和 Controller 层边界模糊。这是存量问题，AI 修改时应逐步梳理，不要一次性大改。

## API / Contract 变更规则

1. 新增 API 必须更新 docs/API_CONTRACT.md
2. 修改 API 参数/响应格式必须标注 BREAKING CHANGE
3. API 返回值尽量统一使用 ResultVO<T> 或标准 JSON 格式
4. 所有 API 必须经过鉴权（LoginHandlerInterceptor 或 @SystemControllerLog）
5. 新增 API 建议使用 @RestController + @RequestMapping("/api/...")
6. 旧 API（Thymeleaf Controller）和新 REST API 共存，逐步迁移

## 公共层修改规则

1. 公共层指：config/、interceptor/、aop/、util/、constant/、context/、dto/、vo/
2. 修改公共层前必须向主控/用户说明：
   - 修改原因
   - 影响范围（哪些模块会受影响）
   - 是否向后兼容
3. 公共层修改必须经过更严格的审核

## 状态流转规则

1. 涉及业务状态变更的代码修改，必须同步更新 docs/STATE_MACHINE.md
2. 状态变更必须是单向（或明确双向）的，避免产生非法状态
3. 状态变更的逻辑应该在 Service 层实现，不在 Controller 层

## 权限规则

1. 所有涉及认证/授权的修改必须同步更新 docs/PERMISSION_RULES.md
2. 新增 API 必须考虑是否需要鉴权
3. 子线程修改权限相关代码前必须向主控说明

## 测试规则

1. 目前项目测试覆盖率仍偏低；已有接入模块单测，但核心业务 Service、API、前端测试仍不足
2. 后端最低门禁：`.\mvnw.cmd -DskipTests compile`
3. 后端完整测试：`.\mvnw.cmd test -DskipTests=false`（2026-05-14 已恢复为 33 个用例稳定通过，执行结果仍必须如实记录）
4. 校园前端测试：`campus-web npm run build`
5. 大屏前端测试：`large_screen npm run build`（仅修改大屏时需要）
6. 至少保证：修改的代码编译/构建通过；如无法执行，必须说明原因
7. AI 子线程完成后的最低要求：相关模块编译无错误，新增/修改接口有必要的手工或自动化验证记录

## 文档更新规则

1. 每次任务结束后必须更新 AI_PROGRESS.md
2. API 变更后必须更新 docs/API_CONTRACT.md
3. 架构变更后必须更新 docs/ARCHITECTURE.md
4. 状态变更后必须更新 docs/STATE_MACHINE.md
5. 权限变更后必须更新 docs/PERMISSION_RULES.md
6. 约定变更后必须更新 docs/CONVENTIONS.md

## 中文交接规范

1. 所有 Agent 间的交接记录使用中文
2. AI_PROGRESS.md 使用中文记录
3. commit message 使用英文（保持项目现有风格）
4. 代码注释使用英文或中文均可（保持文件内一致）
5. 用户交互使用中文

## 禁止事项

- ❌ 严禁 git reset --hard
- ❌ 严禁跳过 hooks（--no-verify）
- ❌ 严禁在未授权目录修改代码
- ❌ 严禁在非本任务 worktree 中工作
- ❌ 严禁删除他人 worktree
- ❌ 严禁无关改动混入功能分支
- ❌ 严禁一次性大规模重构存量代码
- ❌ 严禁修改 pom.xml 核心依赖版本（除非专门任务）
- ❌ 严禁修改 application.yml 核心配置（除非专门任务）
- ❌ 严禁 merge 未审核的子线程代码
