# Batch 25 接入后自动检测联动实施方案 V0.2

## 1. 背景

Batch24 已经完成接入记录标准化、rawData 脱敏、源内去重和运行日志诊断。当前系统具备：

```text
接入任务运行 -> 标准化记录入库 -> duplicate/invalid/fail 统计
检测任务手动运行 -> 扫描 ingest_record/clue/account_content -> 生成检测命中/预警
```

Batch25 目标是把两条链路接起来：当接入任务本次运行有新记录入库时，按配置触发检测任务，只检测本次 `run_id` 产生的接入记录，减少全量扫描和重复命中。

## 2. 本批目标

- 接入任务可配置是否在运行成功后自动触发检测。
- 接入任务可配置关联的检测任务 ID 列表。
- 自动检测只扫描本次接入运行写入的 `campus_ingest_record.run_id`。
- 检测运行日志标记触发来源为 `ingest_run`。
- 接入运行日志记录检测触发数量、命中数量、预警数量和联动错误信息。
- 检测命中仍使用现有去重逻辑，不重复生成同一任务、同一对象、同一规则、同一关键词命中。

## 3. 本批明确不做

- 不接入消息队列。
- 不做分布式检测调度。
- 不自动创建检测主题、检测规则或检测任务。
- 不自动下结论，不自动转事件。
- 不跳过人工研判、预警处置和事件闭环。
- 不接真实外部 API，不扩大采集范围。
- 不做跨平台相似内容聚合。

## 4. 主线程技术决策

Batch25 使用同步轻量联动，不引入 MQ：

```text
CampusIngestServiceImpl.runTaskInternal
  -> 成功写入接入记录
  -> finish ingest run log
  -> CampusIngestDetectionLinkageService
  -> CampusDetectionService.runIngestRecordTask(detectionTaskId, ingestRunId, systemUser)
  -> update ingest run detection summary
```

原因：

- 当前云电脑和本地部署无 Docker / MQ，先保持单体可运行。
- 检测服务已有命中去重和自动预警能力。
- 只扫描本次 `run_id` 可控，不需要全量检测。
- 后续如需异步化，可把 `CampusIngestDetectionLinkageService` 替换成队列生产者。

## 5. 数据库方案

新增迁移：

```text
src/main/resources/db/migration/V1.16__CampusIngestDetectionLinkage.sql
```

### 5.1 `campus_ingest_task` 增强

| 字段 | 类型 | 用途 |
| --- | --- | --- |
| `auto_detect_enabled` | tinyint | 是否运行成功后触发检测，默认 0 |
| `detection_task_ids` | varchar(1024) | 逗号分隔的检测任务业务 ID，空值不触发检测 |

### 5.2 `campus_ingest_run_log` 增强

| 字段 | 类型 | 用途 |
| --- | --- | --- |
| `detection_trigger_count` | int | 本次触发检测任务数量 |
| `detection_hit_count` | int | 检测命中数量 |
| `detection_alert_count` | int | 自动预警数量 |
| `detection_error_message` | varchar(2048) | 检测联动错误摘要 |

### 5.3 `campus_detection_run_log` 增强

| 字段 | 类型 | 用途 |
| --- | --- | --- |
| `trigger_type` | varchar(32) | manual / ingest_run |
| `trigger_object_type` | varchar(64) | ingest_run |
| `trigger_object_id` | bigint | 接入运行日志业务 ID |

## 6. 后端设计

### 6.1 检测服务

`CampusDetectionService` 新增：

```java
CampusDetectionRunLog runIngestRecordTask(Long detectionTaskId,
                                          Long ingestRunId,
                                          Long operatorUserId);
```

实现策略：

- 复用现有 `runTask` 内部流程。
- `manual` 模式保持原逻辑：按扫描窗口扫描 ingest_record/clue/account_content。
- `ingest_run` 模式只扫描 `campus_ingest_record.run_id = ingestRunId` 的接入记录。
- 检测任务必须不是 disabled，主题必须启用。
- 检测任务 `objectTypes` 必须包含 `ingest_record`。

`CampusIngestRecordDao` 新增：

- `listForDetectionByRunId(runId)`

`CampusDetectionTaskDao` 新增：

- `listActiveIngestRecordTasks()`

### 6.2 接入联动服务

新增：

```text
src/main/java/com/stonedt/intelligence/service/campus/ingest/linkage/
  CampusIngestDetectionLinkageService.java
  CampusIngestDetectionLinkageResult.java
```

职责：

- 判断接入任务是否启用自动检测。
- 解析 `detectionTaskIds`。
- 只触发 `detectionTaskIds` 明确绑定的 active 且包含 ingest_record 的任务。
- 未配置 `detectionTaskIds` 时不触发检测，避免过宽扫描。
- 捕获单个检测任务失败，不回滚接入数据。
- 汇总 trigger/hit/alert/error 到接入运行日志。

### 6.3 接入服务

`CampusIngestServiceImpl.runTaskInternal` 成功入库后：

- `successCount > 0` 且 `autoDetectEnabled=1` 才触发联动。
- 联动失败不把接入运行改成 failed，但写 `detection_error_message`。
- 手动运行、调度运行都可触发，前提是任务配置启用。

## 7. 前端最小策略

Batch25 只做类型和运行日志展示：

- `CampusIngestTask` 增加 `autoDetectEnabled`、`detectionTaskIds`。
- `CampusIngestRunLog` 增加 `detectionTriggerCount`、`detectionHitCount`、`detectionAlertCount`、`detectionErrorMessage`。
- 运行日志弹窗增加“检测任务”“检测命中”“检测预警”列。
- 接入任务配置 UI 暂不新增开关，完整配置留给 Batch28；验证可通过接口或 SQL 设置字段。

## 8. 合规和人工研判边界

- 自动检测只是辅助命中，不是事件结论。
- 自动预警仍是待处理状态，必须人工研判。
- 不自动将命中转线索、转事件或分派处置。
- 不扩大检测对象范围，只检测本次运行合法入库的公开/授权记录。
- 检测任务、主题、规则仍由有权限人员配置。

## 9. 验收步骤

后端：

- `.\mvnw.cmd -DskipTests compile`
- 新增/更新单测：验证 `runIngestRecordTask` 只扫描指定 `run_id`。
- `.\mvnw.cmd -DskipTests package`

数据库：

- 启动本地服务，确认 Flyway 迁移到 `V1.16`。
- 检查新增字段存在。

联动：

- 创建检测主题、规则、检测任务，任务包含 `ingest_record` 且 active。
- 创建接入任务，设置 `auto_detect_enabled=1` 和明确的 `detection_task_ids`。
- 运行接入任务写入测试记录后，确认检测运行日志 `trigger_type=ingest_run`。
- 确认接入运行日志写入检测触发、命中和预警数量。
- 再次运行同样记录时命中不重复。

前端：

- `campus-web npm run build`

## 10. 风险和待打磨点

- 同步检测会增加接入任务耗时；本批先保持轻量，Batch26/后续可异步化。
- 未配置 `detection_task_ids` 时不触发检测；学校试运行前必须显式配置任务白名单。
- 检测任务默认 objectTypes 包含多类对象，`ingest_run` 模式必须只扫描本次接入记录，不能扫 clue/account_content。
- 自动预警数量可能增加，前端和处置流程需要运维上设定规则阈值。

## 11. 子线程摸底结论与主线程 V0.2 决策

主线程采纳：

- 后端只读结果建议新增 `runIngestRecordTask`，内部复用检测匹配逻辑，但 ingest-run 模式必须只扫 `run_id`，不扫 clue/account_content。
- 前端只读结果建议 Batch25 只做类型和运行日志展示，不做检测任务选择器和配置开关。
- 合规只读结果建议空 `detection_task_ids` 不应触发全部 active 任务，避免误报和超范围检测。

主线程最终决策：

```text
Batch25 = 显式绑定检测任务 + run_id 精确扫描 + 检测运行来源标记 + 接入运行日志检测汇总
auto_detect_enabled=1 但 detection_task_ids 为空时不触发
联动失败不回滚接入成功状态
不自动定性、不自动转事件、不跳过人工研判
```

## 12. 实施结果

状态：Done。

已实现：

- 新增 `V1.16__CampusIngestDetectionLinkage.sql`。
- `campus_ingest_task` 已支持 `auto_detect_enabled` 和 `detection_task_ids`。
- `campus_ingest_run_log` 已支持 `detection_trigger_count`、`detection_hit_count`、`detection_alert_count`、`detection_error_message`。
- `campus_detection_run_log` 已支持 `trigger_type`、`trigger_object_type`、`trigger_object_id`。
- `CampusDetectionService` 新增 `runIngestRecordTask`，ingest-run 模式只扫描本次 `run_id` 的接入记录。
- 新增 `CampusIngestDetectionLinkageService`，接入任务成功且显式绑定检测任务后触发联动，失败只写入检测联动错误，不回滚接入成功状态。
- 前端类型和运行日志弹窗已展示检测触发、命中、预警数量。
- 新增 `CampusIngestDetectionLinkageServiceTest`，验证显式绑定才触发、空绑定不触发、非 active 检测任务不执行。

验证：

- `.\mvnw.cmd "-DskipTests=false" "-Dmaven.test.skip=false" "-Dtest=CampusIngestDetectionLinkageServiceTest,TikhubResponseMapperTest,CampusIngestRecordNormalizerTest,CampusIngestDedupResultTest" test` 通过，10 个用例成功。
- `.\mvnw.cmd -DskipTests compile` 通过。
- `.\mvnw.cmd -DskipTests package` 通过。
- `campus-web` `npm run build` 通过。
- 本地服务启动成功，Flyway 从 `V1.15` 迁移到 `V1.16`。
- 数据库已确认 `detection_task_ids`、`detection_error_message`、`trigger_object_id` 等字段存在。

遗留风险：

- Batch25 仍是同步联动，接入任务耗时会增加；异步化和额度保护进入 Batch26/后续治理。
- 前端暂不开放自动检测配置，完整配置留给 Batch28。
- 检测任务规则过宽时会产生较多预警，试运行前必须配置明确检测任务白名单。
