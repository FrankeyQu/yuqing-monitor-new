# 开发规范 — 卓然舆情

## 命名规范

### Java 后端
- **类名**：PascalCase（`UserService`、`MonitorController`）
- **方法名**：camelCase（`getArticleList`、`updateWarningStatusById`）
- **变量名**：camelCase（`projectId`、`userUtil`）
- **常量**：UPPER_SNAKE_CASE（`KAPTCHA_SESSION_KEY`）
- **包名**：全小写（`com.stonedt.intelligence.controller`）
- **Mapper XML ID**：camelCase，与 DAO 方法名一致

### Vue 前端
- **组件名**：PascalCase（`PublicSentiment.vue`、`SourceDistribution.vue`）
- **文件名**：kebab-case（`popularinformation.vue`、`sourcedistribution.vue`）⚠️ 当前项目不一致
- **变量/方法**：camelCase
- **路由 path**：kebab-case（`/opinion_screen/`）

### 数据库
- **表名**：snake_case（`monitor_analysis`、`opinion_condition`）
- **列名**：snake_case（`project_id`、`create_time`）
- **主键**：`id`
- **逻辑删除**：`del_status`（0=正常，1=删除）
- **状态**：`status`（1=正常，0=禁用，2=注销 — 各表含义可能不同）
- **时间**：`create_time`、`update_time`

## 文件组织规范

### 后端
```
controller/  → 接收请求 + 参数校验 + 调用 Service
service/     → 业务接口
service/impl/ → 业务实现
dao/         → MyBatis Mapper 接口
entity/      → 数据库表对应的实体
dto/         → 数据传输对象（API 请求/响应）
vo/          → 视图对象（页面渲染 + API 统一响应）
config/      → Spring 配置类
interceptor/ → 拦截器
aop/         → AOP 切面
util/        → 工具类（无状态静态方法）
constant/    → 常量类
quartz/      → 定时任务
```

### 前端（large_screen）
```
api/     → API 调用封装
router/  → Vue Router 配置
views/   → 页面组件
assets/  → 静态资源
```

⚠️ 当前项目视图组件名称不统一（既有 PascalCase 又有 lowercase）。新增组件统一使用 PascalCase。

### Odoo 式模块约定

- 新业务先确认 `docs/modules/*/manifest.md`，再进入代码实现。
- 模块 manifest 必须说明：业务归属、依赖模块、数据模型、API、权限、测试和文档影响。
- 新增校园业务优先使用现有 `controller/campus`、`service/campus`、`dao/campus`、`entity/campus`、`mapper/campus` 边界，不新增平行体系。
- 跨模块能力通过 Service/API 组合，不在 Controller 中直接跨库拼接写入。
- 新增 Flyway 迁移必须同步实体、Mapper、前端类型、API 契约和权限种子。

## Controller / Service / Model / Schema / DTO 分层规范

### Controller
- 接收请求、解析参数、返回结果
- 方法上标注 `@SystemControllerLog` 记录关键操作
- 参数校验应在 Controller 层完成
- **不应包含业务逻辑**（现状：大量 Controller 包含业务逻辑，逐步清理）

### Service
- 业务逻辑实现
- Service 接口定义契约，Impl 实现细节
- 事务注解 `@Transactional(rollbackFor = Exception.class)` 在需要事务的方法上
- 跨模块调用通过 Service 接口

### DAO / Mapper
- MyBatis Mapper 接口
- 对应 XML 定义 SQL
- 方法名应体现 SQL 操作（如 `selectUserByTelephone`、`updateProjectTask`）
- 复杂查询使用 XML 定义，简单查询可使用注解

### Entity
- 对应数据库表结构
- 存量代码手写 getter/setter，新代码使用 Lombok `@Data`
- Entity 不应包含业务逻辑方法

### DTO / VO
- DTO：API 层输入输出对象
- VO：视图渲染对象
- 避免 Entity 直接暴露给 API 调用方

## 错误处理规范

### 当前模式（共存）
1. **旧模式**：`ResultUtil.build(code, msg, data)` — 在 systemService、monitorService 中使用
2. **新模式**：`ResultVO.success(data)` / `ResultVO.error(msg)` — 在 ApiController 中使用
3. **原始模式**：直接 `JSONObject` put code/msg — 在大多数 Controller 中使用

### 推荐模式（新代码）
- 统一使用 `ResultVO<T>` 作为 REST API 返回
- 使用 `@ControllerAdvice` + `@ExceptionHandler` 做全局异常处理（暂未实现）
- 业务异常使用自定义异常类

### 数据库操作
- 回滚使用 `@Transactional(rollbackFor = Exception.class)`
- 不捕获异常让 Spring 管理事务回滚

## 日志规范

1. **操作日志**：使用 `@SystemControllerLog(module, submodule, type, operation)` 注解记录关键操作
2. **AOP 日志**：`SystemLogAspect` 自动拦截 `@SystemControllerLog` 注解方法，记录操作日志到 `system_log` 表
3. **调试日志**：使用 Lombok `@Slf4j` 的 `log.info/warn/error`
4. **禁止** `System.out.println` 在生产代码中使用（存量代码中存在大量 `System.err.println`，需逐步清理）

## 配置规范

1. **框架配置**：`application.yml`（数据库、Redis、MyBatis、Flyway、日志、Swagger）
2. **敏感配置**：`config/application.properties`（token 私钥、Kafka 地址、外部服务 URL）
3. **业务配置**：`config/config.properties`（xml 文件路径、产品手册 PDF 路径）
4. **运行时配置**：读取方式 `@Value("${...}")` `@PropertySource(value = "file:./config/...")`
5. **新增配置**：优先放 application.properties，敏感信息使用环境变量

## API 返回格式规范

### REST API（新）
```json
{
  "code": 200,
  "msg": "success",
  "data": {...}
}
```
- code=200 成功，code=500 业务错误
- msg 描述错误原因

### 旧 Controller API
```json
{
  "code": 200,
  "msg": "方案组数据返回成功",
  "data": [...]
}
```
- code 和 msg 字段名一致，但类型可能不同（String/Integer）

## 前端组件规范

### Vue 大屏
- 使用 Element UI 组件库
- 样式使用 SCSS
- 适配 1920x1080 分辨率
- 使用 `amfe-flexible` 做 rem 适配
- ECharts 用于图表展示

## 状态管理规范

Vue 大屏不涉及复杂状态管理（无 Vuex），组件间通过 `$refs` 调用方法传递数据。

## 表单 / 校验规范

### 后端校验
- 简单校验在 Controller 层进行
- 复杂校验在 Service 层进行
- 目前未使用 JSR-303 Bean Validation（`@Valid`）

### 前端校验
- 使用 Element UI 表单校验
- 图形验证码使用 Kaptcha

## 测试命名规范

目前项目已有基础后端测试，但核心业务、API、前端测试仍不足。建议新代码：

- **测试类**：`{被测类名}Test`（如 `UserServiceTest`）
- **测试方法**：`{方法名}_{场景}_{预期结果}`（如 `getArticleList_正常参数_返回分页数据`）
- **测试目录**：与源码对应 `src/test/java/com/stonedt/intelligence/`

## Git Commit 规范

- **格式**：`<type>: <description>`
- **type**：feat / fix / docs / refactor / test / chore
- **description**：英文，简要说明变更内容
- **示例**：`feat: 增加 LLM 内容解读优化功能`
- **禁止**：--no-verify、--no-gpg-sign

## 中文交接规范

- Agent 间任务交接使用中文
- AI_PROGRESS.md 使用中文
- commit message 使用英文
- 代码注释使用英文或中文均可，同一文件内保持一致
