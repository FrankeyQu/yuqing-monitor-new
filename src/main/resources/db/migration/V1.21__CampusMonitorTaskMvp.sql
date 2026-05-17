-- Batch 30: simple monitor task center MVP.
-- This adds a direct "subject + keyword + negative word" monitoring loop
-- on top of authorized/ingested records and existing campus_alert handling.

CREATE TABLE IF NOT EXISTS `campus_monitor_task` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `monitor_task_id` bigint NOT NULL COMMENT '监测任务业务ID',
    `task_name` varchar(255) NOT NULL COMMENT '任务名称',
    `monitor_subject` varchar(255) NOT NULL COMMENT '监测主体',
    `subject_aliases` text COMMENT '主体别名，逗号分隔',
    `keywords` text COMMENT '监测关键词，逗号分隔',
    `negative_words` text COMMENT '负面词，逗号分隔',
    `exclude_words` text COMMENT '排除词，逗号分隔',
    `platform_scope` varchar(512) DEFAULT NULL COMMENT '平台范围，逗号分隔',
    `scan_frequency_minutes` int DEFAULT 60 COMMENT '扫描频率分钟数',
    `alert_mode` varchar(32) DEFAULT 'negative_only' COMMENT '告警模式(negative_only|all_hits|manual)',
    `task_status` varchar(32) DEFAULT 'paused' COMMENT '任务状态(active|paused|disabled)',
    `last_run_time` datetime DEFAULT NULL COMMENT '最近运行时间',
    `last_run_log_id` bigint DEFAULT NULL COMMENT '最近运行日志业务ID',
    `remark` varchar(1024) DEFAULT NULL COMMENT '备注',
    `deleted` tinyint DEFAULT 0 COMMENT '逻辑删除',
    `create_user_id` bigint DEFAULT NULL COMMENT '创建人',
    `update_user_id` bigint DEFAULT NULL COMMENT '更新人',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_campus_monitor_task_id` (`monitor_task_id`),
    KEY `idx_campus_monitor_task_status` (`task_status`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='校园舆情监测任务表';

CREATE TABLE IF NOT EXISTS `campus_monitor_result` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `monitor_result_id` bigint NOT NULL COMMENT '监测结果业务ID',
    `monitor_task_id` bigint NOT NULL COMMENT '监测任务业务ID',
    `ingest_record_id` bigint NOT NULL COMMENT '接入记录业务ID',
    `title` varchar(512) DEFAULT NULL COMMENT '标题',
    `content` mediumtext COMMENT '内容摘要',
    `original_url` varchar(1024) DEFAULT NULL COMMENT '原文链接',
    `platform` varchar(64) DEFAULT NULL COMMENT '平台',
    `author_name` varchar(255) DEFAULT NULL COMMENT '作者',
    `publish_time` datetime DEFAULT NULL COMMENT '发布时间',
    `matched_subjects` varchar(512) DEFAULT NULL COMMENT '命中主体或别名',
    `matched_keywords` varchar(1024) DEFAULT NULL COMMENT '命中关键词',
    `matched_negative_words` varchar(1024) DEFAULT NULL COMMENT '命中负面词',
    `sentiment` varchar(32) DEFAULT NULL COMMENT '情感',
    `risk_level` varchar(32) DEFAULT 'normal' COMMENT '风险等级',
    `risk_score` int DEFAULT 0 COMMENT '风险分',
    `result_status` varchar(32) DEFAULT 'pending' COMMENT '结果状态(pending|alerted|ignored|handled)',
    `alert_id` bigint DEFAULT NULL COMMENT '关联预警ID',
    `deleted` tinyint DEFAULT 0 COMMENT '逻辑删除',
    `create_user_id` bigint DEFAULT NULL COMMENT '创建人',
    `update_user_id` bigint DEFAULT NULL COMMENT '更新人',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_campus_monitor_result_id` (`monitor_result_id`),
    UNIQUE KEY `uk_campus_monitor_result_record` (`monitor_task_id`, `ingest_record_id`),
    KEY `idx_campus_monitor_result_task` (`monitor_task_id`, `result_status`, `deleted`),
    KEY `idx_campus_monitor_result_risk` (`risk_level`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='校园舆情监测结果表';

CREATE TABLE IF NOT EXISTS `campus_monitor_run_log` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `run_log_id` bigint NOT NULL COMMENT '运行日志业务ID',
    `monitor_task_id` bigint NOT NULL COMMENT '监测任务业务ID',
    `run_status` varchar(32) DEFAULT 'running' COMMENT '运行状态(running|success|failed)',
    `start_time` datetime DEFAULT NULL COMMENT '开始时间',
    `end_time` datetime DEFAULT NULL COMMENT '结束时间',
    `scanned_count` int DEFAULT 0 COMMENT '扫描数量',
    `match_count` int DEFAULT 0 COMMENT '命中数量',
    `negative_count` int DEFAULT 0 COMMENT '负面数量',
    `alert_count` int DEFAULT 0 COMMENT '告警数量',
    `error_message` varchar(2048) DEFAULT NULL COMMENT '错误信息',
    `create_user_id` bigint DEFAULT NULL COMMENT '创建人',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_campus_monitor_run_log_id` (`run_log_id`),
    KEY `idx_campus_monitor_run_task` (`monitor_task_id`, `start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='校园舆情监测运行日志表';

INSERT INTO `campus_dict_type` (`dict_type`, `dict_name`, `description`, `sort_no`, `status`, `deleted`)
VALUES
    ('monitor_task_status', '监测任务状态', '监测任务启停状态', 320, 1, 0),
    ('monitor_alert_mode', '监测告警模式', '监测任务告警触发方式', 321, 1, 0),
    ('monitor_result_status', '监测结果状态', '监测结果处理状态', 322, 1, 0),
    ('monitor_run_status', '监测运行状态', '监测任务运行状态', 323, 1, 0)
ON DUPLICATE KEY UPDATE
    `dict_name` = VALUES(`dict_name`),
    `description` = VALUES(`description`),
    `sort_no` = VALUES(`sort_no`),
    `status` = VALUES(`status`),
    `deleted` = 0,
    `update_time` = CURRENT_TIMESTAMP;

INSERT INTO `campus_dict_item` (`dict_type`, `item_code`, `item_name`, `item_value`, `sort_no`, `status`, `deleted`)
VALUES
    ('monitor_task_status', 'active', '启用', 'active', 10, 1, 0),
    ('monitor_task_status', 'paused', '暂停', 'paused', 20, 1, 0),
    ('monitor_task_status', 'disabled', '禁用', 'disabled', 30, 1, 0),
    ('monitor_alert_mode', 'negative_only', '仅负面告警', 'negative_only', 10, 1, 0),
    ('monitor_alert_mode', 'all_hits', '所有命中告警', 'all_hits', 20, 1, 0),
    ('monitor_alert_mode', 'manual', '人工转告警', 'manual', 30, 1, 0),
    ('monitor_result_status', 'pending', '待研判', 'pending', 10, 1, 0),
    ('monitor_result_status', 'alerted', '已预警', 'alerted', 20, 1, 0),
    ('monitor_result_status', 'ignored', '已忽略', 'ignored', 30, 1, 0),
    ('monitor_result_status', 'handled', '已处理', 'handled', 40, 1, 0),
    ('monitor_run_status', 'running', '运行中', 'running', 10, 1, 0),
    ('monitor_run_status', 'success', '成功', 'success', 20, 1, 0),
    ('monitor_run_status', 'failed', '失败', 'failed', 30, 1, 0),
    ('alert_source', 'monitor', '监测任务', 'monitor', 60, 1, 0)
ON DUPLICATE KEY UPDATE
    `item_name` = VALUES(`item_name`),
    `item_value` = VALUES(`item_value`),
    `sort_no` = VALUES(`sort_no`),
    `status` = VALUES(`status`),
    `deleted` = 0,
    `update_time` = CURRENT_TIMESTAMP;

INSERT INTO `campus_permission_menu` (
    `menu_id`, `parent_id`, `menu_code`, `menu_name`, `menu_type`,
    `route_path`, `permission_code`, `icon`, `sort_no`, `visible`, `status`, `deleted`
) VALUES
    (180113, 0, 'monitor', '监测任务', 'menu', '/monitor', 'campus:monitor:view', 'Radar', 18, 1, 1, 0)
ON DUPLICATE KEY UPDATE
    `menu_name` = VALUES(`menu_name`),
    `route_path` = VALUES(`route_path`),
    `permission_code` = VALUES(`permission_code`),
    `icon` = VALUES(`icon`),
    `sort_no` = VALUES(`sort_no`),
    `visible` = VALUES(`visible`),
    `status` = VALUES(`status`),
    `deleted` = 0,
    `update_time` = CURRENT_TIMESTAMP;

INSERT INTO `campus_permission_api` (
    `api_id`, `api_code`, `api_name`, `module_name`,
    `request_method`, `request_path`, `status`, `remark`, `deleted`
) VALUES
    (181528, 'campus:monitor:read', '监测任务只读', '监测任务', 'GET', '/campus/monitor/**', 1, '监测任务、结果和告警只读', 0),
    (181529, 'campus:monitor:operate', '监测任务操作', '监测任务', 'POST', '/campus/monitor/**', 1, '监测任务维护、运行、结果和告警处理', 0)
ON DUPLICATE KEY UPDATE
    `api_code` = VALUES(`api_code`),
    `api_name` = VALUES(`api_name`),
    `module_name` = VALUES(`module_name`),
    `request_method` = VALUES(`request_method`),
    `request_path` = VALUES(`request_path`),
    `status` = VALUES(`status`),
    `remark` = VALUES(`remark`),
    `deleted` = 0,
    `update_time` = CURRENT_TIMESTAMP;

INSERT INTO `campus_role_menu` (`relation_id`, `role_id`, `menu_id`)
VALUES
    (211180113, 180001, 180113),
    (212180113, 180002, 180113),
    (213180113, 180003, 180113)
ON DUPLICATE KEY UPDATE
    `deleted` = 0,
    `update_time` = CURRENT_TIMESTAMP;

INSERT INTO `campus_role_api` (`relation_id`, `role_id`, `api_id`)
VALUES
    (211181528, 180002, 181528),
    (211181529, 180002, 181529),
    (212181528, 180003, 181528)
ON DUPLICATE KEY UPDATE
    `deleted` = 0,
    `update_time` = CURRENT_TIMESTAMP;

INSERT INTO `campus_monitor_task` (
    `monitor_task_id`, `task_name`, `monitor_subject`, `subject_aliases`,
    `keywords`, `negative_words`, `exclude_words`, `platform_scope`,
    `scan_frequency_minutes`, `alert_mode`, `task_status`, `remark`,
    `deleted`, `create_user_id`, `update_user_id`
) VALUES (
    201001, '校园食品安全监测', '学校', '试运行学校,某校,某某大学',
    '食堂,食品安全,后勤,学生讨论', '投诉,曝光,维权,食品安全,未经证实,问题',
    '招聘,广告,招生', '*',
    60, 'negative_only', 'active', 'Batch30 演示监测任务：扫描已合法接入的公开或授权记录',
    0, 1, 1
) ON DUPLICATE KEY UPDATE
    `task_name` = VALUES(`task_name`),
    `monitor_subject` = VALUES(`monitor_subject`),
    `subject_aliases` = VALUES(`subject_aliases`),
    `keywords` = VALUES(`keywords`),
    `negative_words` = VALUES(`negative_words`),
    `exclude_words` = VALUES(`exclude_words`),
    `platform_scope` = VALUES(`platform_scope`),
    `scan_frequency_minutes` = VALUES(`scan_frequency_minutes`),
    `alert_mode` = VALUES(`alert_mode`),
    `task_status` = VALUES(`task_status`),
    `remark` = VALUES(`remark`),
    `deleted` = 0,
    `update_time` = CURRENT_TIMESTAMP;
