# Batch 23 接入任务调度与运行日志实施方案 V0.2

## 1. 背景

Batch21 已完成接入任务手动运行闭环，Batch22 已完成 TikHub 安全适配器。当前系统已经可以：

```text
接入来源 -> 接入任务 -> 手动运行 -> 适配器拉取/占位 -> 标准化记录 -> 运行日志
```

Batch23 的目标是把“人工点运行”升级为“可控自动运行”，同时把运行日志补成后续排障、审计、额度统计、失败重试的基础。

## 2. 本批目标

- 支持启用中的接入任务按计划自动运行。
- 复用 `campus_ingest_task.schedule_cron` 和 `next_run_time`。
- 新增最小调度锁，避免同一任务被重复触发。
- 新增运行日志字段：触发类型、耗时、错误类型、重试次数、调度节点。
- 支持失败后有限重试和连续失败保护。
- 手动运行和自动运行共用同一套 `CampusIngestService.runTask` 内核。
- 前端仍不做完整调度配置页，只保证已有任务页能看到 `scheduleCron`、`nextRunTime`、运行日志。

## 3. 本批明确不做

- 不引入 Quartz、XXL-Job、ElasticJob 等外部调度平台。
- 不做分布式任务中心。
- 不做真实 TikHub 高频调度验收。
- 不绕过 Batch22 的 endpoint allowlist、密钥引用、合规校验。
- 不做复杂 cron UI 生成器。
- 不自动打开所有历史任务的调度。
- 不清库、不改旧迁移。

## 4. 主线程技术决策

Batch23 使用 Spring 已启用的 `@Scheduled` 能力做本地扫描器：

```text
CampusIngestScheduler
  -> 每 60 秒扫描 due tasks
  -> DB 条件更新抢占调度锁
  -> 调用 CampusIngestService.runScheduledTask
  -> 写 campus_ingest_run_log
  -> 根据 cron / retry 策略刷新 next_run_time
```

原因：

- 当前云电脑无 Docker，本地服务单体运行，外部调度平台会增加部署复杂度。
- 项目已经有 `@EnableScheduling`，不需要引入新依赖。
- 数据库锁足以覆盖本批单体/轻量多实例场景。
- 后续如果要拆独立 ingest-service，可把扫描器替换成消息队列或任务平台，Service 内核不变。

## 5. 数据库方案

新增迁移：

```text
src/main/resources/db/migration/V1.14__CampusIngestSchedulingEnhancement.sql
```

### 5.1 `campus_ingest_task` 增强

新增字段：

| 字段 | 类型 | 用途 |
| --- | --- | --- |
| `schedule_enabled` | tinyint | 是否允许自动调度，默认 0 |
| `schedule_lock_until` | datetime | 调度锁过期时间 |
| `max_retry_count` | int | 单次失败后的最大重试次数，默认 0 |
| `retry_interval_minutes` | int | 失败重试间隔，默认 10 |
| `consecutive_fail_count` | int | 连续失败次数 |
| `current_retry_count` | int | 当前重试轮次 |
| `last_error_type` | varchar(64) | 最近错误分类 |

索引：

```text
idx_campus_ingest_task_schedule(schedule_enabled, task_status, next_run_time, deleted)
idx_campus_ingest_task_lock(schedule_lock_until)
```

### 5.2 `campus_ingest_run_log` 增强

新增字段：

| 字段 | 类型 | 用途 |
| --- | --- | --- |
| `trigger_type` | varchar(32) | manual / schedule / retry |
| `duration_ms` | bigint | 运行耗时 |
| `error_type` | varchar(64) | credential_missing / adapter_unsupported / validation_error / request_failed / normalize_failed / unknown |
| `retry_count` | int | 当前重试序号 |
| `scheduler_node` | varchar(128) | 执行节点名 |

默认兼容：

- 旧手动运行记录 `trigger_type` 默认 `manual`。
- 老接口不传新字段时不影响 insert。

## 6. 后端设计

### 6.1 新增类

```text
service/campus/ingest/scheduler/CampusIngestScheduler.java
service/campus/ingest/scheduler/CampusIngestSchedulePolicy.java
service/campus/ingest/scheduler/CampusIngestRunContext.java
```

职责：

- `CampusIngestScheduler`：定时扫描 due tasks、抢锁、触发执行。
- `CampusIngestSchedulePolicy`：校验 cron、计算 nextRunTime、计算 retryRunTime。
- `CampusIngestRunContext`：携带 triggerType、retryCount、schedulerNode、startMillis。

### 6.2 Service 调整

`CampusIngestService` 增加：

```java
CampusIngestRunLog runScheduledTask(Long taskId, String triggerType, Integer retryCount, String schedulerNode);
List<CampusIngestTask> listDueTasks(Date now, Integer limit);
boolean acquireScheduleLock(Long taskId, Date now, Date lockUntil);
void releaseScheduleLock(Long taskId);
```

`runTask(Long taskId, Long operatorUserId)` 保持外部兼容，内部转为：

```text
runTaskInternal(taskId, operatorUserId, triggerType=manual, retryCount=0, schedulerNode=null)
```

### 6.3 DAO/Mapper 调整

`CampusIngestTaskDao` 新增：

- `listDueTasks(now, limit)`
- `acquireScheduleLock(taskId, now, lockUntil)`
- `releaseScheduleLock(taskId)`
- `updateNextRunTime(taskId, nextRunTime, updateUserId)`
- `markScheduleSuccess(taskId, nextRunTime, updateUserId)`
- `markScheduleFailure(taskId, nextRunTime, errorType, updateUserId)`
- `acquireExecutionLock(taskId, now, lockUntil, updateUserId)`，手动运行也复用同一把执行锁，避免与自动调度撞车。

`CampusIngestRunLogDao` 新增：

- insert 支持新字段。
- finish 支持 `durationMs`、`errorType`。

## 7. Cron 和频率策略

Batch23 支持 Spring 5.x 的 `CronSequenceGenerator` 和 6 位 cron 表达式，例如：

```text
0 */5 * * * ?
0 0/30 * * * ?
0 0 8,12,18 * * ?
```

规则：

- `schedule_enabled=1` 且 `task_status=active` 且 `schedule_cron` 非空才自动运行。
- 保存任务时仅在 `schedule_enabled=1` 时强制校验 cron 合法性，避免历史任务脏数据影响普通保存。
- 最小调度间隔限制为 5 分钟；小于 5 分钟的表达式拒绝保存。
- 调度扫描器每分钟扫描一次，但每轮最多执行 5 个任务。
- TikHub 等第三方 API 任务在未配置真实 Key 时仍会失败，不外呼。
- 不新增 `@EnableScheduling`，项目已有调度启用和线程池配置。
- 新调度器通过 `schedule.campus-ingest.open` 控制，默认关闭。

## 8. 失败和重试策略

错误分类：

| 分类 | 典型错误 |
| --- | --- |
| `credential_missing` | `TikHub credential is not configured` |
| `adapter_unsupported` | endpoint 未实现 / adapter 不支持 |
| `validation_error` | 任务状态、授权范围、来源禁用 |
| `request_failed` | HTTP 非 2xx、超时、网络失败 |
| `normalize_failed` | 响应映射或入库失败 |
| `unknown` | 其他异常 |

策略：

- 手动运行不自动重试。
- 自动运行失败后，如果 `current_retry_count < max_retry_count`，按 `retry_interval_minutes` 写入下一次 `next_run_time`，并递增 `current_retry_count`，下一次触发 `trigger_type=retry`。
- 超过重试次数后，按 cron 计算下一轮正式调度时间。
- 成功后清空 `current_retry_count`、`consecutive_fail_count` 和 `last_error_type`。
- 连续失败只记数，不自动停用任务；是否停用留给 Batch26/28 的告警和前端管理。

## 9. 权限、审计和合规

- 手动运行继续走 Controller 审计。
- 自动运行没有 HTTP Request，不写 `campus_audit_log`，但必须完整写 `campus_ingest_run_log`。
- 自动运行操作者使用系统用户 ID `0`。
- 调度器不得修改或绕过来源授权依据、任务授权范围校验。
- 调度器不得补填真实 Key。
- 调度器不得触发未启用、无授权范围、无来源依据、无 endpoint allowlist 的任务。

## 10. 前端最小策略

Batch23 暂不做完整调度配置页，只保持后端字段兼容：

- `CampusIngestTask` 类型补充新增调度字段。
- 任务列表可显示 `scheduleCron`、`nextRunTime` 和 `lastRunTime`。
- 运行日志类型补充 `triggerType`、`durationMs`、`errorType`、`retryCount`、`schedulerNode`。
- 后端新增字段统一 camelCase，`/campus/ingest/run/list` 继续返回数组，不改为分页。
- 前端保存任务必须白名单提交用户可编辑字段，不回传 `scheduleLockUntil`、`consecutiveFailCount`、`currentRetryCount`、`lastErrorType` 等运行态字段。

完整的调度开关、频率选择器、错误告警和重试配置留给 Batch28。

## 11. 验收步骤

后端：

- `.\mvnw.cmd -DskipTests compile`
- `.\mvnw.cmd -DskipTests package`
- 启动本地后端，确认 Flyway `V1.14` 迁移成功。

接口/调度：

- 创建 `manual_push` 调度任务，`schedule_enabled=1`，cron 为 5 分钟以上表达式。
- 手动把 `next_run_time` 调整到当前时间以前。
- 等待调度扫描器触发，确认运行日志 `trigger_type=schedule`。
- 确认 `duration_ms`、`scheduler_node`、`next_run_time` 被写入。
- 创建无 `TIKHUB_API_KEY` 的 TikHub 调度任务，确认失败日志 `error_type=credential_missing`，且不外呼。

前端：

- 如有前端类型/页面改动，执行 `campus-web npm run build`。

## 12. 风险和待打磨点

- Spring `CronSequenceGenerator` 对复杂 cron 的兼容性需要实测。
- 当前项目已有多个旧定时任务，Batch23 新调度器必须可通过配置开关关闭。
- 本地扫描器不是长期分布式调度方案，后续部署多实例时仍应保留 DB 锁或替换为统一任务平台。
- 自动任务一旦接入真实 Key，会产生费用和平台请求，真实启用前必须确认授权、频率、关键词和额度。

## 13. 主线程 V0.2 决策

Batch23 先实现“本地扫描器 + DB 锁 + 增强运行日志”的最小闭环。  
先用 `manual_push` 和无 Key TikHub 任务做验收，不进行真实外部调度调用。

本批采纳只读摸底结果：

- 不新增 `@EnableScheduling`。
- 使用 `CronSequenceGenerator`，不用 Spring 5.3+ 的 `CronExpression`。
- 新增 `current_retry_count`。
- 调度锁定义为执行锁，手动运行和自动调度共用。
- 调度器默认关闭，通过 `schedule.campus-ingest.open=1` 显式开启。
- 前端只读展示增强字段，并白名单保存任务。

## 14. 实施结果

状态：Done。

已实现：

- 新增 `V1.14__CampusIngestSchedulingEnhancement.sql`。
- `campus_ingest_task` 已支持 `schedule_enabled`、`schedule_lock_until`、`max_retry_count`、`retry_interval_minutes`、`consecutive_fail_count`、`current_retry_count`、`last_error_type`。
- `campus_ingest_run_log` 已支持 `trigger_type`、`duration_ms`、`error_type`、`retry_count`、`scheduler_node`。
- 新增 `CampusIngestScheduler`、`CampusIngestSchedulePolicy`、`CampusIngestRunContext`。
- `CampusIngestServiceImpl` 已支持执行锁、自动调度、失败重试状态、错误分类、耗时统计和调度节点记录。
- `campus-web/src/views/IngestView.vue` 已最小展示调度字段和增强运行日志字段，任务保存使用白名单 payload。

验证：

- `.\mvnw.cmd -DskipTests compile` 通过。
- `campus-web` `npm run build` 通过。
- `.\mvnw.cmd -DskipTests package` 通过。
- 本地启动后 Flyway 成功迁移到 `V1.14`。
- `manual_push` 调度任务可被扫描器触发，运行日志写入 `triggerType=schedule`、耗时和调度节点，并刷新 `next_run_time`。
- 无 `TIKHUB_API_KEY` 的 TikHub 调度任务失败为 `credential_missing`，未产生外部请求和敏感日志。
- Batch23 验证任务已关闭 `schedule_enabled`，避免继续自动执行。

遗留风险：

- 旧系统 `AnalysisPTQuartz` 仍可能产生本地日志噪声，试运行前需关闭或配置。
- 当前调度器是单体本地扫描器，不是长期分布式任务平台；多实例部署需要继续保留 DB 锁或接入统一调度。
- 真实外部 API 调度启用前必须确认 Key、授权范围、频率、额度和责任人。
