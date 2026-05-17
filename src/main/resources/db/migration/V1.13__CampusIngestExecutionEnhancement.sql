-- Batch 21: ingest execution enhancement.
-- Adds nullable execution trace and idempotency columns without changing old migrations.

ALTER TABLE `campus_ingest_record`
    ADD COLUMN `run_id` bigint DEFAULT NULL COMMENT '接入运行日志业务ID' AFTER `record_id`,
    ADD COLUMN `content_hash` varchar(128) DEFAULT NULL COMMENT '内容幂等哈希' AFTER `external_id`,
    ADD KEY `idx_campus_ingest_record_run` (`run_id`),
    ADD UNIQUE KEY `uk_campus_ingest_record_hash` (`source_id`, `content_hash`);

INSERT INTO `campus_dict_item` (`dict_type`, `item_code`, `item_name`, `item_value`, `sort_no`)
VALUES
    ('ingest_adapter_type', 'third_party_api', '第三方接口占位', 'third_party_api', 50)
ON DUPLICATE KEY UPDATE
    `item_name` = VALUES(`item_name`),
    `item_value` = VALUES(`item_value`),
    `sort_no` = VALUES(`sort_no`),
    `update_time` = CURRENT_TIMESTAMP;
