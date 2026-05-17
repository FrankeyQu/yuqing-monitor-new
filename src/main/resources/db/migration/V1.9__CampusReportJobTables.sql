-- 校园舆情自动报告任务表
-- Batch 10: scheduled report jobs and generation logs.

CREATE TABLE IF NOT EXISTS `campus_report_job` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `report_job_id` bigint NOT NULL COMMENT '自动报告任务业务ID',
    `job_name` varchar(255) NOT NULL COMMENT '任务名称',
    `report_type` varchar(64) NOT NULL COMMENT '报告类型(daily|weekly|monthly|special|event)',
    `template_id` bigint DEFAULT NULL COMMENT '报告模板ID',
    `period_rule` varchar(64) DEFAULT 'daily' COMMENT '统计周期规则(daily|weekly|monthly)',
    `schedule_cron` varchar(128) DEFAULT NULL COMMENT '计划表达式预留',
    `output_format` varchar(32) DEFAULT 'markdown' COMMENT '输出格式(markdown|html|text)',
    `job_status` varchar(32) DEFAULT 'paused' COMMENT '任务状态(active|paused|disabled)',
    `last_run_time` datetime DEFAULT NULL COMMENT '最近运行时间',
    `next_run_time` datetime DEFAULT NULL COMMENT '下次运行时间',
    `reviewer_user_id` bigint DEFAULT NULL COMMENT '默认审核人',
    `description` varchar(1024) DEFAULT NULL COMMENT '任务说明',
    `deleted` tinyint DEFAULT 0 COMMENT '逻辑删除(0否,1是)',
    `create_user_id` bigint DEFAULT NULL COMMENT '创建人',
    `update_user_id` bigint DEFAULT NULL COMMENT '更新人',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_campus_report_job_id` (`report_job_id`),
    KEY `idx_campus_report_job_status` (`job_status`, `deleted`),
    KEY `idx_campus_report_job_type` (`report_type`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='校园舆情自动报告任务表';

CREATE TABLE IF NOT EXISTS `campus_report_generation_log` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `generation_log_id` bigint NOT NULL COMMENT '生成日志业务ID',
    `report_job_id` bigint NOT NULL COMMENT '自动报告任务业务ID',
    `report_id` bigint DEFAULT NULL COMMENT '生成报告业务ID',
    `run_status` varchar(32) DEFAULT 'running' COMMENT '运行状态(running|success|failed)',
    `start_time` datetime DEFAULT NULL COMMENT '开始时间',
    `end_time` datetime DEFAULT NULL COMMENT '结束时间',
    `error_message` varchar(2048) DEFAULT NULL COMMENT '错误信息',
    `create_user_id` bigint DEFAULT NULL COMMENT '创建人',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_campus_report_generation_log_id` (`generation_log_id`),
    KEY `idx_campus_report_generation_job` (`report_job_id`, `start_time`),
    KEY `idx_campus_report_generation_report` (`report_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='校园舆情自动报告生成日志表';

INSERT INTO `campus_dict_type` (`dict_type`, `dict_name`, `description`, `sort_no`)
VALUES
    ('report_job_status', '自动报告任务状态', '校园舆情自动报告任务状态', 250),
    ('report_period_rule', '报告周期规则', '校园舆情自动报告统计周期规则', 260)
ON DUPLICATE KEY UPDATE
    `dict_name` = VALUES(`dict_name`),
    `description` = VALUES(`description`),
    `sort_no` = VALUES(`sort_no`),
    `update_time` = CURRENT_TIMESTAMP;

INSERT INTO `campus_dict_item` (`dict_type`, `item_code`, `item_name`, `item_value`, `sort_no`)
VALUES
    ('report_job_status', 'active', '启用', 'active', 10),
    ('report_job_status', 'paused', '暂停', 'paused', 20),
    ('report_job_status', 'disabled', '禁用', 'disabled', 30),
    ('report_period_rule', 'daily', '日报周期', 'daily', 10),
    ('report_period_rule', 'weekly', '周报周期', 'weekly', 20),
    ('report_period_rule', 'monthly', '月报周期', 'monthly', 30)
ON DUPLICATE KEY UPDATE
    `item_name` = VALUES(`item_name`),
    `item_value` = VALUES(`item_value`),
    `sort_no` = VALUES(`sort_no`),
    `update_time` = CURRENT_TIMESTAMP;
