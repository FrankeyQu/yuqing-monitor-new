-- Batch 25: link successful ingest runs to detection tasks.
-- Keeps linkage opt-in and records both ingest-side summary and detection run origin.

ALTER TABLE `campus_ingest_task`
    ADD COLUMN `auto_detect_enabled` tinyint DEFAULT 0 COMMENT '接入成功后是否自动触发检测(1启用,0关闭)' AFTER `last_error_type`,
    ADD COLUMN `detection_task_ids` varchar(1024) DEFAULT NULL COMMENT '自动检测任务业务ID列表，逗号分隔' AFTER `auto_detect_enabled`;

ALTER TABLE `campus_ingest_run_log`
    ADD COLUMN `detection_trigger_count` int DEFAULT 0 COMMENT '触发检测任务数量' AFTER `invalid_count`,
    ADD COLUMN `detection_hit_count` int DEFAULT 0 COMMENT '检测命中数量' AFTER `detection_trigger_count`,
    ADD COLUMN `detection_alert_count` int DEFAULT 0 COMMENT '检测预警数量' AFTER `detection_hit_count`,
    ADD COLUMN `detection_error_message` varchar(2048) DEFAULT NULL COMMENT '检测联动错误摘要' AFTER `detection_alert_count`;

ALTER TABLE `campus_detection_run_log`
    ADD COLUMN `trigger_type` varchar(32) DEFAULT 'manual' COMMENT '触发类型(manual|ingest_run)' AFTER `run_status`,
    ADD COLUMN `trigger_object_type` varchar(64) DEFAULT NULL COMMENT '触发对象类型' AFTER `trigger_type`,
    ADD COLUMN `trigger_object_id` bigint DEFAULT NULL COMMENT '触发对象业务ID' AFTER `trigger_object_type`,
    ADD KEY `idx_campus_detection_run_trigger` (`trigger_type`, `trigger_object_type`, `trigger_object_id`);
