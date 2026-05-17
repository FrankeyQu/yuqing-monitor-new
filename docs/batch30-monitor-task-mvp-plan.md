# Batch30 监测任务中心 MVP 实施方案

## 1. 背景和目标

当前系统已经具备校园数据接入、检测任务、预警中心、线索和事件处置能力，但入口偏后台化。用户实际需要的是更直接的舆情监控闭环：

```text
创建监测任务 -> 配置主体和关键词 -> 搜索/接入公开或授权内容 -> 识别负面 -> 自动告警 -> 人工处理
```

Batch30 的目标是新增一个“监测任务中心”入口，把已有底座封装成更简单的业务体验。

本批交付的最小闭环：

- 用户创建监测任务，配置监测主体、主体别名、关键词、负面词、排除词、平台范围和扫描频率。
- 系统保存任务时只保存监测业务配置，运行时使用独立监测逻辑扫描已合法接入记录。
- 用户可以立即运行监测任务。
- 运行时扫描已接入的公开/授权记录，匹配“主体/别名 + 关键词/负面词”，负面命中自动生成预警。
- 前端提供“监测任务、监测结果、负面告警”三个简化 Tab。

## 2. 本批不做的范围

- 不接入真实微博、抖音、小红书、B 站、知乎等生产账号或 API Key。
- 不做绕登录、绕验证码、Cookie、账号池、代理池、浏览器指纹或签名参数。
- 不采集私信、通讯录、密码、非公开个人资料。
- 不实现真实公开网页抓取，只复用现有公开网页白名单占位和已有接入记录。
- 不引入新的大模型或外部情感分析服务。
- 不删除旧的检测任务、数据接入和预警中心页面。

## 3. 数据流和模块边界

本批采用“独立监测任务闭环 + 复用既有接入和预警底座”的方案。摸底后确认旧检测规则更适合高级配置，不足以自然表达“主体或别名 + 关键词 + 负面词”的组合条件，所以监测中心使用独立结果表承载更直观的 MVP 逻辑。

```text
campus_monitor_task
  -> 扫描 campus_ingest_record
  -> 写 campus_monitor_result
  -> 负面命中写 campus_alert(alert_source=monitor)
```

监测结果新增独立表，第一阶段内容来源仍是已经合法接入的 `campus_ingest_record`：

- `/campus/monitor/result/list` 返回监测结果。
- `/campus/monitor/alert/list` 返回 `alert_source=monitor` 的告警。

## 4. 数据库变更方案

新增 `V1.21__CampusMonitorTaskMvp.sql`：

- `campus_monitor_task`
  - `monitor_task_id`
  - `task_name`
  - `monitor_subject`
  - `subject_aliases`
  - `keywords`
  - `negative_words`
  - `exclude_words`
  - `platform_scope`
  - `scan_frequency_minutes`
  - `alert_mode`
  - `task_status`
  - `last_run_time`
  - `last_run_log_id`
  - `remark`
  - 审计字段

- `campus_monitor_result`
  - `monitor_result_id`
  - `monitor_task_id`
  - `ingest_record_id`
  - 内容摘要字段
  - 命中的主体、关键词、负面词
  - 情感、风险等级、风险分
  - `result_status`
  - `alert_id`
  - 审计字段

- `campus_monitor_run_log`
  - `run_log_id`
  - `monitor_task_id`
  - 运行状态、扫描数、命中数、负面数、告警数、错误信息

同时补充：

- 菜单：`/monitor`，名称“监测任务”。
- 接口权限：`/campus/monitor/**` 读写拆分。
- 字典：监测任务状态、告警模式。
- 演示任务：校园食品安全监测。

## 5. 后端接口和 Service 设计

新增命名空间：

```text
GET  /campus/monitor/task/list
POST /campus/monitor/task/save
POST /campus/monitor/task/update-status
POST /campus/monitor/task/delete
POST /campus/monitor/task/run

GET  /campus/monitor/result/list
POST /campus/monitor/result/alert
POST /campus/monitor/result/ignore

GET  /campus/monitor/alert/list
POST /campus/monitor/alert/handle
```

新增 Service：

- `CampusMonitorService`
  - `saveTask`
  - `updateTaskStatus`
  - `deleteTask`
  - `runTask`
  - `listResults`
  - `alertResult`
  - `ignoreResult`
  - `listAlerts`
  - `handleAlert`

核心策略：

- 保存监测任务只保存业务配置，不隐式创建高级检测任务。
- 运行时扫描已接入、已标准化的公开/授权记录。
- 命中条件为：内容包含监测主体或任一别名，并且包含关键词或负面词；排除词优先过滤。
- 负面判断为：命中负面词、原记录情感为负面、原记录风险等级非 `normal` 三者满足其一。
- 负面命中自动创建 `campus_alert`，告警原因写入内容摘要。
- 每个任务对同一接入记录只生成一条监测结果，重复运行幂等。

## 6. 前端页面和交互设计

新增 `campus-web/src/views/MonitorView.vue`，路由 `/monitor`。

页面结构：

- 顶部概览：启用任务数、今日命中、待处理告警。
- Tab 1：监测任务
  - 列表、创建/编辑、启停、立即运行。
  - 表单字段：任务名称、监测主体、别名、关键词、负面词、排除词、平台、多选频率、告警模式。
- Tab 2：监测结果
  - 展示命中标题、平台、命中词、风险、状态、时间。
  - 支持转预警、忽略。
- Tab 3：负面告警
  - 展示告警标题、命中词、风险、状态。
  - 支持处理、忽略。

入口：

- 侧栏新增“监测任务”，优先于“检测任务”和“数据接入”。
- 保留原高级页面，后续可以归为“高级配置”。

## 7. 权限、审计和合规约束

- 新增 `/campus/monitor/**` 接口权限。
- 管理员拥有全部监测权限。
- 处置员拥有读取、运行、告警处理权限。
- 查看员拥有读取权限。
- 创建、修改、删除、启停、运行、告警处理均写审计。
- 保存任务时拒绝包含 `apiKey/token/cookie/password/deviceId/fingerprint/signature` 等敏感字段的配置文本。

## 8. 失败、重试、幂等和去重

- 本批新增 `campus_monitor_run_log` 独立记录监测任务运行结果。
- 同一监测任务对同一接入记录只生成一条 `campus_monitor_result`，唯一约束为 `(monitor_task_id, ingest_record_id)`。
- 已告警的监测结果重复运行不再新增 `campus_alert`，只保持既有结果和告警关联。
- 本批不新增定时调度器，频率先保存为配置，立即运行先可用；后续再把 `active` 任务接入现有调度扫描器。

## 9. 验收步骤

1. 新建“校园食品安全监测”任务。
2. 配置主体、别名、关键词和负面词。
3. 点击“立即运行”。
4. 系统完成监测运行。
5. 在“监测结果”看到命中内容。
6. 负面命中自动进入“负面告警”。
7. 告警可处理或忽略。
8. 后端 Maven 构建通过。
9. 前端 `npm run build` 通过。

## 10. 风险和待确认问题

- 当前“搜索社媒”能力仍依赖合法的第三方 API、上级平台或学校授权数据源；本批只把监测任务业务入口和检测告警闭环做成。
- 如果要启用真实微博、抖音、小红书等数据源，需要单独确认授权、密钥、额度、频率和责任部门。
- 后续可做 Batch31：把 `active` 监测任务接入自动调度，并把监测任务和接入任务建立显式绑定。
