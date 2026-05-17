# Batch31 监测任务自动调度与接入绑定实施方案

## 1. 背景和目标

Batch30 已经完成“监测任务中心 MVP”：用户可以配置监测主体、关键词、负面词，并手动运行任务，负面命中会进入预警中心。

Batch31 的目标是补齐两个关键能力：

- 自动调度：启用的监测任务可以按扫描频率自动运行。
- 接入绑定：监测任务可以绑定一个或多个数据接入任务，只扫描这些任务产生的合法接入记录。

本批最小闭环：

```text
监测任务启用自动扫描
  -> 设置扫描频率和绑定接入任务
  -> 调度器发现到期任务
  -> 加锁运行监测
  -> 只扫描绑定接入任务的记录
  -> 生成监测结果和负面告警
  -> 写运行日志和下次运行时间
```

## 2. 本批不做的范围

- 不接入真实微博、抖音、小红书、B 站、知乎等生产账号或 API Key。
- 不实现绕登录、绕验证码、Cookie、代理池、账号池、浏览器指纹或签名参数。
- 不改变现有接入调度器的业务逻辑。
- 不把监测任务强制绑定接入任务；未绑定时仍兼容 Batch30 的全量已接入记录扫描。
- 不实现复杂 Cron 表达式，监测任务继续使用 `scan_frequency_minutes` 作为固定间隔。

## 3. 数据流和模块边界

```text
CampusMonitorScheduler
  -> CampusMonitorService.listDueTasks
  -> acquireScheduleLock
  -> runScheduledTask
  -> campus_monitor_run_log
  -> campus_monitor_result
  -> campus_alert(alert_source=monitor)
```

接入绑定关系：

```text
campus_monitor_task
  -> campus_monitor_ingest_task_relation
  -> campus_ingest_task
  -> campus_ingest_record.task_id
```

边界说明：

- 接入任务负责采集、标准化、去重、治理和额度。
- 监测任务负责业务匹配、负面判断和告警。
- 绑定只过滤扫描范围，不触发接入任务，不绕过接入合规边界。

## 4. 数据库变更方案

新增 `V1.22__CampusMonitorSchedulingBinding.sql`：

- `campus_monitor_task` 增加：
  - `schedule_enabled`：是否启用自动扫描。
  - `next_run_time`：下次自动运行时间。
  - `schedule_lock_until`：调度/手动运行锁。

- `campus_monitor_run_log` 增加：
  - `trigger_type`：`manual` 或 `schedule`。
  - `scheduler_node`：调度节点名称。

- 新增 `campus_monitor_ingest_task_relation`：
  - `relation_id`
  - `monitor_task_id`
  - `ingest_task_id`
  - 审计字段和逻辑删除字段
  - 唯一约束 `(monitor_task_id, ingest_task_id)`

## 5. 后端接口和 Service 设计

复用已有保存接口：

```text
POST /campus/monitor/task/save
```

`CampusMonitorTask` 新增字段：

- `scheduleEnabled`
- `nextRunTime`
- `scheduleLockUntil`
- `ingestTaskIds`
- `ingestTaskNames`

新增 Service 方法：

- `listDueTasks(Date now, Integer limit)`
- `acquireScheduleLock(Long monitorTaskId, Date now, Date lockUntil)`
- `releaseScheduleLock(Long monitorTaskId)`
- `runScheduledTask(Long monitorTaskId, String schedulerNode)`

运行策略：

- 手动运行和自动运行共用同一套扫描逻辑。
- 手动运行会获取执行锁，避免与自动调度并发。
- 自动运行只选择 `task_status='active'`、`schedule_enabled=1`、`next_run_time<=now` 的任务。
- 自动运行完成后按 `scan_frequency_minutes` 写入下一次运行时间。
- 自动运行失败也记录失败日志并释放锁，下一次运行时间仍按固定频率推进。

## 6. 前端页面和交互设计

在 `/monitor` 监测任务页新增：

- 列表展示：
  - 自动扫描开关状态
  - 下次运行时间
  - 绑定接入任务

- 新增/编辑表单：
  - 自动扫描开关
  - 接入任务多选

接入任务选择项来自既有：

```text
GET /campus/ingest/task/list
```

只作为绑定选择，不在监测页创建或运行接入任务。

## 7. 权限、审计和合规约束

- 继续复用 `/campus/monitor/**` 权限。
- 保存绑定关系属于“保存监测任务”的一部分，继续写审计。
- 调度器默认关闭，通过 `schedule.campus-monitor.open=1` 显式开启。
- 绑定接入任务时只引用已有接入任务 ID，不保存密钥、Cookie、Token、设备指纹或签名参数。

## 8. 失败、重试、幂等和去重

- 调度锁使用 `schedule_lock_until`，防止同一任务并发执行。
- 监测结果继续使用 `(monitor_task_id, ingest_record_id)` 幂等。
- 已告警结果重复运行不再创建重复告警。
- 本批不做失败重试队列；失败任务按固定频率进入下一次调度。

## 9. 验收步骤

1. 保存监测任务，开启自动扫描并绑定接入任务。
2. 确认任务列表展示自动扫描、绑定任务和下次运行时间。
3. 手动运行任务，确认只扫描绑定接入任务的记录。
4. 临时开启 `schedule.campus-monitor.open=1`，把任务下次运行时间置为当前时间之前。
5. 调度器自动运行任务并写入运行日志。
6. 后端 `.\mvnw.cmd -DskipTests package` 通过。
7. 前端 `npm run build` 通过。

## 10. 风险和待确认问题

- 自动调度默认关闭，生产启用前需要确认服务器资源、任务频率和值班责任人。
- 真实社媒搜索仍依赖合法第三方 API、上级平台或学校授权数据源，必须单独确认授权和密钥。
- 未绑定接入任务时仍扫描全部已接入记录，这是为了兼容 Batch30；后续可以按学校要求改成必须绑定。
