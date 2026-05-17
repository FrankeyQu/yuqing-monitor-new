-- 校园舆情报告归档表
-- Batch 7: report templates, report archive and event-report relations.

CREATE TABLE IF NOT EXISTS `campus_report_template` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `template_id` bigint NOT NULL COMMENT '模板业务ID',
    `template_name` varchar(255) NOT NULL COMMENT '模板名称',
    `report_type` varchar(64) NOT NULL COMMENT '报告类型(daily|weekly|monthly|special|event)',
    `template_content` mediumtext COMMENT '模板内容',
    `status` tinyint DEFAULT 1 COMMENT '状态(1启用,0停用)',
    `remark` varchar(1024) DEFAULT NULL COMMENT '备注',
    `deleted` tinyint DEFAULT 0 COMMENT '逻辑删除(0否,1是)',
    `create_user_id` bigint DEFAULT NULL COMMENT '创建人',
    `update_user_id` bigint DEFAULT NULL COMMENT '更新人',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_campus_report_template_id` (`template_id`),
    KEY `idx_campus_report_template_type` (`report_type`, `status`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='校园舆情报告模板表';

CREATE TABLE IF NOT EXISTS `campus_report` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `report_id` bigint NOT NULL COMMENT '报告业务ID',
    `report_title` varchar(255) NOT NULL COMMENT '报告标题',
    `report_type` varchar(64) NOT NULL COMMENT '报告类型(daily|weekly|monthly|special|event)',
    `report_status` varchar(32) DEFAULT 'draft' COMMENT '报告状态(draft|generated|archived|published)',
    `template_id` bigint DEFAULT NULL COMMENT '使用模板ID',
    `event_id` bigint DEFAULT NULL COMMENT '关联事件ID',
    `period_start_time` datetime DEFAULT NULL COMMENT '统计开始时间',
    `period_end_time` datetime DEFAULT NULL COMMENT '统计结束时间',
    `report_summary` varchar(2048) DEFAULT NULL COMMENT '报告摘要',
    `report_content` mediumtext COMMENT '报告内容',
    `report_format` varchar(32) DEFAULT 'markdown' COMMENT '报告格式(markdown|html|text)',
    `file_name` varchar(255) DEFAULT NULL COMMENT '导出文件名',
    `file_path` varchar(1024) DEFAULT NULL COMMENT '外部文件路径预留',
    `generated_by` bigint DEFAULT NULL COMMENT '生成人',
    `generate_time` datetime DEFAULT NULL COMMENT '生成时间',
    `archive_user_id` bigint DEFAULT NULL COMMENT '归档人',
    `archive_time` datetime DEFAULT NULL COMMENT '归档时间',
    `archive_opinion` varchar(1024) DEFAULT NULL COMMENT '归档意见',
    `deleted` tinyint DEFAULT 0 COMMENT '逻辑删除(0否,1是)',
    `create_user_id` bigint DEFAULT NULL COMMENT '创建人',
    `update_user_id` bigint DEFAULT NULL COMMENT '更新人',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_campus_report_id` (`report_id`),
    KEY `idx_campus_report_type_status` (`report_type`, `report_status`, `deleted`),
    KEY `idx_campus_report_event` (`event_id`),
    KEY `idx_campus_report_period` (`period_start_time`, `period_end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='校园舆情报告表';

CREATE TABLE IF NOT EXISTS `campus_report_event` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `relation_id` bigint NOT NULL COMMENT '关联业务ID',
    `report_id` bigint NOT NULL COMMENT '报告业务ID',
    `event_id` bigint NOT NULL COMMENT '事件业务ID',
    `deleted` tinyint DEFAULT 0 COMMENT '逻辑删除(0否,1是)',
    `create_user_id` bigint DEFAULT NULL COMMENT '创建人',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_campus_report_event_relation` (`relation_id`),
    KEY `idx_campus_report_event_report` (`report_id`, `event_id`, `deleted`),
    KEY `idx_campus_report_event_event` (`event_id`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='校园舆情报告事件关联表';

INSERT INTO `campus_dict_type` (`dict_type`, `dict_name`, `description`, `sort_no`)
VALUES
    ('report_type', '报告类型', '校园舆情报告类型', 130),
    ('report_status', '报告状态', '校园舆情报告状态', 140),
    ('report_format', '报告格式', '校园舆情报告导出格式', 150)
ON DUPLICATE KEY UPDATE
    `dict_name` = VALUES(`dict_name`),
    `description` = VALUES(`description`),
    `sort_no` = VALUES(`sort_no`),
    `update_time` = CURRENT_TIMESTAMP;

INSERT INTO `campus_dict_item` (`dict_type`, `item_code`, `item_name`, `item_value`, `sort_no`)
VALUES
    ('report_type', 'daily', '日报', 'daily', 10),
    ('report_type', 'weekly', '周报', 'weekly', 20),
    ('report_type', 'monthly', '月报', 'monthly', 30),
    ('report_type', 'special', '专报', 'special', 40),
    ('report_type', 'event', '事件报告', 'event', 50),
    ('report_status', 'draft', '草稿', 'draft', 10),
    ('report_status', 'generated', '已生成', 'generated', 20),
    ('report_status', 'archived', '已归档', 'archived', 30),
    ('report_status', 'published', '已发布', 'published', 40),
    ('report_format', 'markdown', 'Markdown', 'markdown', 10),
    ('report_format', 'html', 'HTML', 'html', 20),
    ('report_format', 'text', '纯文本', 'text', 30)
ON DUPLICATE KEY UPDATE
    `item_name` = VALUES(`item_name`),
    `item_value` = VALUES(`item_value`),
    `sort_no` = VALUES(`sort_no`),
    `update_time` = CURRENT_TIMESTAMP;
