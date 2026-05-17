-- Campus report targeted scope and analysis profile.
-- Adds explicit filters for daily/weekly/monthly reports so AI generation can build on scoped rule data.

ALTER TABLE `campus_report`
    ADD COLUMN `scope_type` varchar(32) DEFAULT 'all' COMMENT '统计范围类型(all|keyword|event|department|monitor_task|custom)' AFTER `generation_mode`,
    ADD COLUMN `scope_keywords` varchar(1024) DEFAULT NULL COMMENT '范围关键词，逗号/分号/空格分隔' AFTER `scope_type`,
    ADD COLUMN `exclude_keywords` varchar(1024) DEFAULT NULL COMMENT '排除关键词，逗号/分号/空格分隔' AFTER `scope_keywords`,
    ADD COLUMN `platform_scope` varchar(512) DEFAULT NULL COMMENT '平台范围，逗号/分号/空格分隔' AFTER `exclude_keywords`,
    ADD COLUMN `risk_levels` varchar(256) DEFAULT NULL COMMENT '风险等级范围，逗号/分号/空格分隔' AFTER `platform_scope`,
    ADD COLUMN `department_scope` varchar(512) DEFAULT NULL COMMENT '部门ID范围，逗号/分号/空格分隔' AFTER `risk_levels`,
    ADD COLUMN `monitor_task_ids` varchar(512) DEFAULT NULL COMMENT '监测任务ID范围，逗号/分号/空格分隔' AFTER `department_scope`,
    ADD COLUMN `analysis_profile` varchar(32) DEFAULT 'brief' COMMENT '分析档位(brief|risk|disposal)' AFTER `monitor_task_ids`,
    ADD KEY `idx_campus_report_scope` (`scope_type`, `analysis_profile`, `deleted`);

ALTER TABLE `campus_report_job`
    ADD COLUMN `scope_type` varchar(32) DEFAULT 'all' COMMENT '统计范围类型(all|keyword|event|department|monitor_task|custom)' AFTER `generation_mode`,
    ADD COLUMN `scope_keywords` varchar(1024) DEFAULT NULL COMMENT '范围关键词，逗号/分号/空格分隔' AFTER `scope_type`,
    ADD COLUMN `exclude_keywords` varchar(1024) DEFAULT NULL COMMENT '排除关键词，逗号/分号/空格分隔' AFTER `scope_keywords`,
    ADD COLUMN `platform_scope` varchar(512) DEFAULT NULL COMMENT '平台范围，逗号/分号/空格分隔' AFTER `exclude_keywords`,
    ADD COLUMN `risk_levels` varchar(256) DEFAULT NULL COMMENT '风险等级范围，逗号/分号/空格分隔' AFTER `platform_scope`,
    ADD COLUMN `department_scope` varchar(512) DEFAULT NULL COMMENT '部门ID范围，逗号/分号/空格分隔' AFTER `risk_levels`,
    ADD COLUMN `monitor_task_ids` varchar(512) DEFAULT NULL COMMENT '监测任务ID范围，逗号/分号/空格分隔' AFTER `department_scope`,
    ADD COLUMN `analysis_profile` varchar(32) DEFAULT 'brief' COMMENT '分析档位(brief|risk|disposal)' AFTER `monitor_task_ids`,
    ADD KEY `idx_campus_report_job_scope` (`scope_type`, `analysis_profile`, `deleted`);

UPDATE `campus_report`
SET `scope_type` = 'all',
    `analysis_profile` = 'brief',
    `update_time` = CURRENT_TIMESTAMP
WHERE `deleted` = 0
  AND (`scope_type` IS NULL OR `scope_type` = '' OR `analysis_profile` IS NULL OR `analysis_profile` = '');

UPDATE `campus_report_job`
SET `scope_type` = 'all',
    `analysis_profile` = 'brief',
    `update_time` = CURRENT_TIMESTAMP
WHERE `deleted` = 0
  AND (`scope_type` IS NULL OR `scope_type` = '' OR `analysis_profile` IS NULL OR `analysis_profile` = '');

INSERT INTO `campus_dict_type` (`dict_type`, `dict_name`, `description`, `sort_no`)
VALUES
    ('report_scope_type', '报告统计范围类型', '校园报告统计范围配置', 271),
    ('report_analysis_profile', '报告分析档位', '校园报告概览/风险/处置分析档位', 272)
ON DUPLICATE KEY UPDATE
    `dict_name` = VALUES(`dict_name`),
    `description` = VALUES(`description`),
    `sort_no` = VALUES(`sort_no`);

INSERT INTO `campus_dict_item` (`dict_type`, `item_code`, `item_name`, `item_value`, `sort_no`, `status`)
VALUES
    ('report_scope_type', 'all', '全量舆情', 'all', 1, 1),
    ('report_scope_type', 'keyword', '关键词范围', 'keyword', 2, 1),
    ('report_scope_type', 'event', '事件范围', 'event', 3, 1),
    ('report_scope_type', 'department', '部门范围', 'department', 4, 1),
    ('report_scope_type', 'monitor_task', '监测任务范围', 'monitor_task', 5, 1),
    ('report_scope_type', 'custom', '自定义范围', 'custom', 6, 1),
    ('report_analysis_profile', 'brief', '概览简报', 'brief', 1, 1),
    ('report_analysis_profile', 'risk', '风险研判', 'risk', 2, 1),
    ('report_analysis_profile', 'disposal', '处置建议', 'disposal', 3, 1)
ON DUPLICATE KEY UPDATE
    `item_name` = VALUES(`item_name`),
    `item_value` = VALUES(`item_value`),
    `sort_no` = VALUES(`sort_no`),
    `status` = VALUES(`status`);
