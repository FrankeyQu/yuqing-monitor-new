-- Batch 23: ingest task scheduling and enhanced run logs.
-- Adds scheduling state, execution lock and log diagnostics without changing old migrations.

ALTER TABLE `campus_ingest_task`
    ADD COLUMN `schedule_enabled` tinyint DEFAULT 0 COMMENT '是否启用自动调度(1启用,0关闭)' AFTER `schedule_cron`,
    ADD COLUMN `schedule_lock_until` datetime DEFAULT NULL COMMENT '调度执行锁过期时间' AFTER `next_run_time`,
    ADD COLUMN `max_retry_count` int DEFAULT 0 COMMENT '最大失败重试次数' AFTER `schedule_lock_until`,
    ADD COLUMN `retry_interval_minutes` int DEFAULT 10 COMMENT '失败重试间隔分钟' AFTER `max_retry_count`,
    ADD COLUMN `consecutive_fail_count` int DEFAULT 0 COMMENT '连续失败次数' AFTER `retry_interval_minutes`,
    ADD COLUMN `current_retry_count` int DEFAULT 0 COMMENT '当前重试轮次' AFTER `consecutive_fail_count`,
    ADD COLUMN `last_error_type` varchar(64) DEFAULT NULL COMMENT '最近错误分类' AFTER `current_retry_count`,
    ADD KEY `idx_campus_ingest_task_schedule` (`schedule_enabled`, `task_status`, `next_run_time`, `deleted`),
    ADD KEY `idx_campus_ingest_task_lock` (`schedule_lock_until`);

ALTER TABLE `campus_ingest_run_log`
    ADD COLUMN `trigger_type` varchar(32) DEFAULT 'manual' COMMENT '触发类型(manual|schedule|retry)' AFTER `run_status`,
    ADD COLUMN `duration_ms` bigint DEFAULT NULL COMMENT '运行耗时毫秒' AFTER `end_time`,
    ADD COLUMN `error_type` varchar(64) DEFAULT NULL COMMENT '错误分类' AFTER `error_message`,
    ADD COLUMN `retry_count` int DEFAULT 0 COMMENT '当前重试序号' AFTER `error_type`,
    ADD COLUMN `scheduler_node` varchar(128) DEFAULT NULL COMMENT '调度执行节点' AFTER `retry_count`,
    ADD KEY `idx_campus_ingest_run_trigger` (`trigger_type`, `start_time`);
