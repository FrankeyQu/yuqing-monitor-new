-- Batch 34: monitor task display governance and auto-ingest observability.
-- Monitor data remains sourced from campus_ingest; this migration only stores
-- task-level display, auto-ingest and latest run summary flags.

ALTER TABLE `campus_monitor_task`
    ADD COLUMN `display_enabled` tinyint DEFAULT 1 COMMENT '前台监测信息是否展示(1展示,0隐藏)' AFTER `schedule_enabled`,
    ADD COLUMN `auto_ingest_enabled` tinyint DEFAULT 1 COMMENT '是否按平台范围自动维护接入任务(1启用,0关闭)' AFTER `display_enabled`,
    ADD COLUMN `last_collect_time` datetime DEFAULT NULL COMMENT '最近触发接入采集时间' AFTER `last_run_log_id`,
    ADD COLUMN `last_match_count` int DEFAULT 0 COMMENT '最近一次监测命中数量' AFTER `last_collect_time`,
    ADD COLUMN `last_error_message` varchar(1024) DEFAULT NULL COMMENT '最近运行或接入错误摘要' AFTER `last_match_count`,
    ADD COLUMN `ingest_capability_status` varchar(64) DEFAULT 'pending' COMMENT '接入能力状态(ready|partial|unsupported|failed|pending)' AFTER `last_error_message`,
    ADD KEY `idx_campus_monitor_task_display` (`display_enabled`, `deleted`),
    ADD KEY `idx_campus_monitor_task_auto_ingest` (`auto_ingest_enabled`, `deleted`);

UPDATE `campus_monitor_task`
SET `display_enabled` = 1
WHERE `display_enabled` IS NULL;

UPDATE `campus_monitor_task`
SET `auto_ingest_enabled` = 1
WHERE `auto_ingest_enabled` IS NULL;
