-- 校园报告 AI 生成模式增强
-- 为报告和自动报告任务补充生成模式、AI 生成审计字段。

ALTER TABLE `campus_report`
    ADD COLUMN `generation_mode` varchar(32) DEFAULT 'template' COMMENT '生成模式(template|ai)' AFTER `report_status`,
    ADD COLUMN `ai_model` varchar(128) DEFAULT NULL COMMENT 'AI生成模型' AFTER `file_path`,
    ADD COLUMN `ai_prompt_snapshot` mediumtext COMMENT 'AI生成输入快照' AFTER `ai_model`;

ALTER TABLE `campus_report_job`
    ADD COLUMN `generation_mode` varchar(32) DEFAULT 'template' COMMENT '生成模式(template|ai)' AFTER `report_type`;

ALTER TABLE `campus_report_generation_log`
    ADD COLUMN `generation_mode` varchar(32) DEFAULT NULL COMMENT '生成模式(template|ai)' AFTER `report_id`,
    ADD COLUMN `duration_ms` bigint DEFAULT NULL COMMENT '生成耗时毫秒' AFTER `end_time`;

INSERT INTO `campus_dict_type` (`dict_type`, `dict_name`, `description`, `sort_no`)
VALUES
    ('report_generation_mode', '报告生成模式', '校园报告传统规则或AI生成模式', 270)
ON DUPLICATE KEY UPDATE
    `dict_name` = VALUES(`dict_name`),
    `description` = VALUES(`description`),
    `sort_no` = VALUES(`sort_no`),
    `update_time` = CURRENT_TIMESTAMP;

INSERT INTO `campus_dict_item` (`dict_type`, `item_code`, `item_name`, `item_value`, `sort_no`)
VALUES
    ('report_generation_mode', 'template', '传统规则', 'template', 10),
    ('report_generation_mode', 'ai', 'AI智能', 'ai', 20)
ON DUPLICATE KEY UPDATE
    `item_name` = VALUES(`item_name`),
    `item_value` = VALUES(`item_value`),
    `sort_no` = VALUES(`sort_no`),
    `update_time` = CURRENT_TIMESTAMP;
