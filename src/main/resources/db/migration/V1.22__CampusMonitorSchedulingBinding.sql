-- Batch 31: monitor task scheduling and ingest task binding.
-- This only schedules monitor scans over existing authorized/ingested records.

ALTER TABLE `campus_monitor_task`
    ADD COLUMN `schedule_enabled` tinyint DEFAULT 0 COMMENT '是否启用自动扫描(1启用,0关闭)' AFTER `scan_frequency_minutes`,
    ADD COLUMN `next_run_time` datetime DEFAULT NULL COMMENT '下次自动运行时间' AFTER `last_run_log_id`,
    ADD COLUMN `schedule_lock_until` datetime DEFAULT NULL COMMENT '调度或手动运行锁过期时间' AFTER `next_run_time`,
    ADD KEY `idx_campus_monitor_task_schedule` (`schedule_enabled`, `task_status`, `next_run_time`, `deleted`),
    ADD KEY `idx_campus_monitor_task_lock` (`schedule_lock_until`);

ALTER TABLE `campus_monitor_run_log`
    ADD COLUMN `trigger_type` varchar(32) DEFAULT 'manual' COMMENT '触发类型(manual|schedule)' AFTER `run_status`,
    ADD COLUMN `scheduler_node` varchar(128) DEFAULT NULL COMMENT '调度执行节点' AFTER `error_message`,
    ADD KEY `idx_campus_monitor_run_trigger` (`trigger_type`, `start_time`);

CREATE TABLE IF NOT EXISTS `campus_monitor_ingest_task_relation` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `relation_id` bigint NOT NULL COMMENT '关系业务ID',
    `monitor_task_id` bigint NOT NULL COMMENT '监测任务业务ID',
    `ingest_task_id` bigint NOT NULL COMMENT '接入任务业务ID',
    `deleted` tinyint DEFAULT 0 COMMENT '逻辑删除',
    `create_user_id` bigint DEFAULT NULL COMMENT '创建人',
    `update_user_id` bigint DEFAULT NULL COMMENT '更新人',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_campus_monitor_ingest_relation_id` (`relation_id`),
    UNIQUE KEY `uk_campus_monitor_ingest_pair` (`monitor_task_id`, `ingest_task_id`),
    KEY `idx_campus_monitor_ingest_task` (`ingest_task_id`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='监测任务与接入任务绑定关系表';

INSERT INTO `campus_monitor_ingest_task_relation` (
    `relation_id`, `monitor_task_id`, `ingest_task_id`, `deleted`, `create_user_id`, `update_user_id`
)
SELECT 201001200101, 201001, 200101, 0, 1, 1
FROM DUAL
WHERE EXISTS (SELECT 1 FROM `campus_monitor_task` WHERE `monitor_task_id` = 201001 AND `deleted` = 0)
  AND EXISTS (SELECT 1 FROM `campus_ingest_task` WHERE `task_id` = 200101 AND `deleted` = 0)
ON DUPLICATE KEY UPDATE
    `deleted` = 0,
    `update_user_id` = VALUES(`update_user_id`),
    `update_time` = CURRENT_TIMESTAMP;

UPDATE `campus_monitor_task`
SET `schedule_enabled` = 1,
    `next_run_time` = IFNULL(`next_run_time`, NOW()),
    `update_time` = CURRENT_TIMESTAMP
WHERE `monitor_task_id` = 201001
  AND `deleted` = 0;
