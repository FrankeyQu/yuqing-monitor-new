# Batch32 监测数据存储与并发收口实施方案

## 1. 背景

Batch30 已完成“监测任务中心 MVP”，Batch31 已补齐自动调度和接入任务绑定。当前用户继续提出三个运行期问题：

- 每天监测数据怎么存储和统计。
- 监测结果、运行日志保留多久。
- 多任务、多节点、多并发运行时如何避免重复扫描和重复告警。

本批次遵循用户最新约束：暂不改数据库结构。因此方案只使用现有表和现有字段，不新增 PG 表、不新增迁移脚本。

## 2. 实施边界

本批次实现：

- 使用现有 `campus_monitor_task.scan_frequency_minutes` 作为任务级监测频率。
- 使用现有 `campus_monitor_task.schedule_lock_until` 作为自动调度和手动运行的并发锁。
- 使用现有 `campus_monitor_result` 存储监测结果，并继续依赖唯一键 `(monitor_task_id, ingest_record_id)` 去重。
- 使用现有 `campus_monitor_run_log` 存储运行日志。
- 新增应用层清理逻辑，按配置清理历史监测结果和运行日志。
- 保持每日统计为查询派生，不新增日报汇总表。

本批次不实现：

- 不改表结构。
- 不迁移到 PostgreSQL。
- 不接真实社交媒体绕过式爬取。
- 不引入分布式任务平台。
- 不新增复杂 Cron 到单个监测任务，任务仍使用固定分钟频率。

## 3. 数据存储方案

### 3.1 明细数据

监测命中仍写入 `campus_monitor_result`：

- 一条接入记录在同一监测任务下最多生成一条监测结果。
- 结果保存标题、正文摘要、原文链接、平台、作者、发布时间、命中主体、关键词、负面词、情感、风险等级和风险分。
- 负面命中按告警模式写入 `campus_alert`，并在结果表记录 `alert_id`。

### 3.2 每日数据

不新增每日汇总表。每日数据通过现有查询口径派生：

- 今日监测结果数：`campus_monitor_result.create_time >= CURDATE()`。
- 大屏趋势：按 `create_time` 对近 7 天监测结果和监测告警做分组统计。
- 后续如果学校要求固定日报归档，再新增单独日报表或报表文件生成任务。

### 3.3 保留时长

使用全局配置控制保留时长：

| 配置项 | 默认值 | 说明 |
| --- | ---: | --- |
| `schedule.campus-monitor.result-retention-days` | 180 | 监测结果保留天数 |
| `schedule.campus-monitor.run-log-retention-days` | 90 | 运行日志保留天数 |
| `schedule.campus-monitor.cleanup-cron` | `0 30 2 * * ?` | 每天 02:30 清理 |
| `schedule.campus-monitor.cleanup-batch-size` | 1000 | 单批清理数量 |

清理策略：

- `campus_monitor_result` 使用逻辑删除，避免误伤仍需要追溯的业务记录。
- `campus_monitor_run_log` 现有表没有 `deleted` 字段，只能物理删除过期日志。
- 清理任务按批次执行，避免一次性大事务。
- `retention-days <= 0` 时跳过对应类型清理。

## 4. 监测频率方案

任务级频率继续使用 `scan_frequency_minutes`：

- 默认 60 分钟。
- 最小 5 分钟。
- 前端保存任务时继续传入分钟值。
- 自动调度每分钟扫描一次到期任务，但任务是否运行由 `next_run_time` 决定。

扫描窗口从“固定回看 168 小时”改为：

- 若任务有 `last_run_time`：从 `last_run_time - scan-overlap-minutes` 开始扫描。
- 若任务没有 `last_run_time`：从 `initial-scan-window-hours` 前开始扫描。
- 最终窗口受 `max-scan-window-hours` 限制，避免长期停机后一次性翻很久的历史库存。

新增配置：

| 配置项 | 默认值 | 说明 |
| --- | ---: | --- |
| `schedule.campus-monitor.scan-overlap-minutes` | 5 | 两次运行之间的重叠窗口，防止边界时间丢数据 |
| `schedule.campus-monitor.initial-scan-window-hours` | 24 | 首次运行回看窗口 |
| `schedule.campus-monitor.max-scan-window-hours` | 24 | 单次最大扫描窗口 |

## 5. 并发方案

### 5.1 调度并发

自动调度使用数据库行级更新争抢锁：

1. 调度器查询 `next_run_time <= now` 且未被锁定的任务。
2. 每个节点执行 `UPDATE campus_monitor_task SET schedule_lock_until = lockUntil ... WHERE schedule_lock_until IS NULL OR schedule_lock_until < now`。
3. 只有更新成功的节点可以运行该任务。

### 5.2 锁释放收口

原实现无条件释放 `schedule_lock_until`。本批次改为优先按本次 `lockUntil` 释放：

- 正常完成时，只有当前锁仍然没有被更新为后续节点的新锁，才推进 `next_run_time` 并清锁。
- 异常释放时，只有锁时间不晚于本次 `lockUntil` 才清锁。
- 如果本次运行已经超时，且另一个节点已获得更晚的新锁，旧节点不会误清新锁。

### 5.3 结果去重

继续保留两层去重：

- 运行前查询 `selectByTaskAndRecord`，避免常规重复写入。
- 插入时捕获唯一键冲突，避免并发窗口下两个节点同时插入导致整次运行失败。

### 5.4 多并发建议

部署多实例时建议：

- `lock-minutes` 设置为单次监测 P95 耗时的 2 倍以上。
- `batch-size` 根据数据库负载控制，默认 5，最大 20。
- 监测任务绑定接入任务，避免所有任务都扫描全量接入记录。
- 高风险学校试运行先从 30 或 60 分钟频率开始，再按数据量下降。

## 6. 当前实现落点

- `CampusMonitorServiceImpl`
  - 修正扫描窗口。
  - 增加历史清理能力。
  - 捕获结果唯一键并发冲突。
- `CampusMonitorScheduler`
  - 自动调度传递本次锁边界。
  - 增加每日历史清理任务。
- `CampusMonitorTaskMapper`
  - 增加按锁边界释放。
  - 调度完成和失败按锁边界更新。
- `CampusMonitorResultMapper`
  - 增加结果过期逻辑删除。
- `CampusMonitorRunLogMapper`
  - 增加运行日志过期删除。
- `config/application.properties`
  - 增加扫描窗口、清理和保留期配置。

## 7. 后续可选增强

如果后续允许改数据库，建议再做：

- 在 `campus_monitor_task` 增加任务级 `retention_days`。
- 在 `campus_monitor_run_log` 增加 `deleted` 字段，统一逻辑删除。
- 增加 `campus_monitor_daily_stat` 日汇总表，降低大屏查询压力。
- 迁移 PostgreSQL 时把 MySQL `LIMIT UPDATE/DELETE`、`GROUP_CONCAT`、`ON DUPLICATE KEY` 改为 PG 对应写法。
