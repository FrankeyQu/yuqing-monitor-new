-- 校园舆情检测引擎表
-- Batch 11: detection topics, rules, tasks, hits and run logs.

CREATE TABLE IF NOT EXISTS `campus_detection_topic` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `topic_id` bigint NOT NULL COMMENT '检测主题业务ID',
    `topic_name` varchar(255) NOT NULL COMMENT '主题名称',
    `topic_category` varchar(64) DEFAULT NULL COMMENT '主题类别',
    `keywords` text COMMENT '主题关键词',
    `exclude_words` text COMMENT '主题排除词',
    `platform_scope` varchar(512) DEFAULT NULL COMMENT '平台范围，逗号分隔',
    `source_scope` varchar(512) DEFAULT NULL COMMENT '数据来源范围，逗号分隔',
    `risk_level` varchar(32) DEFAULT 'concern' COMMENT '默认风险等级',
    `responsible_department_id` bigint DEFAULT NULL COMMENT '责任部门',
    `enabled` tinyint DEFAULT 1 COMMENT '是否启用(1启用,0停用)',
    `description` varchar(1024) DEFAULT NULL COMMENT '主题说明',
    `deleted` tinyint DEFAULT 0 COMMENT '逻辑删除(0否,1是)',
    `create_user_id` bigint DEFAULT NULL COMMENT '创建人',
    `update_user_id` bigint DEFAULT NULL COMMENT '更新人',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_campus_detection_topic_id` (`topic_id`),
    KEY `idx_campus_detection_topic_status` (`topic_category`, `enabled`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='校园舆情检测主题表';

CREATE TABLE IF NOT EXISTS `campus_detection_rule` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `rule_id` bigint NOT NULL COMMENT '检测规则业务ID',
    `topic_id` bigint NOT NULL COMMENT '检测主题业务ID',
    `rule_name` varchar(255) NOT NULL COMMENT '规则名称',
    `rule_type` varchar(64) NOT NULL COMMENT '规则类型(keyword_any|keyword_all|exact|regex|risk_level)',
    `rule_condition` text COMMENT '规则条件',
    `exclude_words` text COMMENT '规则排除词',
    `risk_level` varchar(32) DEFAULT 'concern' COMMENT '命中风险等级',
    `enabled` tinyint DEFAULT 1 COMMENT '是否启用(1启用,0停用)',
    `sort_no` int DEFAULT 0 COMMENT '排序号',
    `description` varchar(1024) DEFAULT NULL COMMENT '规则说明',
    `deleted` tinyint DEFAULT 0 COMMENT '逻辑删除(0否,1是)',
    `create_user_id` bigint DEFAULT NULL COMMENT '创建人',
    `update_user_id` bigint DEFAULT NULL COMMENT '更新人',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_campus_detection_rule_id` (`rule_id`),
    KEY `idx_campus_detection_rule_topic` (`topic_id`, `enabled`, `deleted`),
    KEY `idx_campus_detection_rule_type` (`rule_type`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='校园舆情检测规则表';

CREATE TABLE IF NOT EXISTS `campus_detection_task` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `detection_task_id` bigint NOT NULL COMMENT '检测任务业务ID',
    `topic_id` bigint NOT NULL COMMENT '检测主题业务ID',
    `task_name` varchar(255) NOT NULL COMMENT '任务名称',
    `object_types` varchar(255) DEFAULT 'ingest_record,clue,account_content' COMMENT '检测对象类型',
    `task_status` varchar(32) DEFAULT 'paused' COMMENT '任务状态(active|paused|disabled)',
    `scan_window_hours` int DEFAULT 24 COMMENT '扫描时间窗口(小时)',
    `auto_alert` tinyint DEFAULT 1 COMMENT '命中后是否自动生成预警(1是,0否)',
    `last_run_time` datetime DEFAULT NULL COMMENT '最近运行时间',
    `next_run_time` datetime DEFAULT NULL COMMENT '下次运行时间',
    `description` varchar(1024) DEFAULT NULL COMMENT '任务说明',
    `deleted` tinyint DEFAULT 0 COMMENT '逻辑删除(0否,1是)',
    `create_user_id` bigint DEFAULT NULL COMMENT '创建人',
    `update_user_id` bigint DEFAULT NULL COMMENT '更新人',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_campus_detection_task_id` (`detection_task_id`),
    KEY `idx_campus_detection_task_topic` (`topic_id`, `task_status`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='校园舆情检测任务表';

CREATE TABLE IF NOT EXISTS `campus_detection_hit` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `hit_id` bigint NOT NULL COMMENT '检测命中业务ID',
    `detection_task_id` bigint NOT NULL COMMENT '检测任务业务ID',
    `topic_id` bigint NOT NULL COMMENT '检测主题业务ID',
    `rule_id` bigint DEFAULT NULL COMMENT '检测规则业务ID',
    `object_type` varchar(64) NOT NULL COMMENT '命中对象类型(ingest_record|clue|account_content)',
    `object_id` bigint NOT NULL COMMENT '命中对象业务ID',
    `object_title` varchar(512) DEFAULT NULL COMMENT '命中对象标题',
    `platform` varchar(64) DEFAULT NULL COMMENT '平台',
    `matched_keywords` varchar(1024) DEFAULT NULL COMMENT '命中关键词',
    `risk_level` varchar(32) DEFAULT 'concern' COMMENT '风险等级',
    `hit_content` mediumtext COMMENT '命中内容摘要',
    `hit_status` varchar(32) DEFAULT 'pending' COMMENT '命中状态(pending|alerted|ignored|converted)',
    `alert_id` bigint DEFAULT NULL COMMENT '关联预警ID',
    `clue_id` bigint DEFAULT NULL COMMENT '关联线索ID',
    `deleted` tinyint DEFAULT 0 COMMENT '逻辑删除(0否,1是)',
    `create_user_id` bigint DEFAULT NULL COMMENT '创建人',
    `update_user_id` bigint DEFAULT NULL COMMENT '更新人',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_campus_detection_hit_id` (`hit_id`),
    KEY `idx_campus_detection_hit_task` (`detection_task_id`, `hit_status`, `deleted`),
    KEY `idx_campus_detection_hit_object` (`object_type`, `object_id`, `deleted`),
    KEY `idx_campus_detection_hit_risk` (`risk_level`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='校园舆情检测命中表';

CREATE TABLE IF NOT EXISTS `campus_detection_run_log` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `run_log_id` bigint NOT NULL COMMENT '运行日志业务ID',
    `detection_task_id` bigint NOT NULL COMMENT '检测任务业务ID',
    `run_status` varchar(32) DEFAULT 'running' COMMENT '运行状态(running|success|failed)',
    `start_time` datetime DEFAULT NULL COMMENT '开始时间',
    `end_time` datetime DEFAULT NULL COMMENT '结束时间',
    `scanned_count` int DEFAULT 0 COMMENT '扫描数量',
    `hit_count` int DEFAULT 0 COMMENT '命中数量',
    `alert_count` int DEFAULT 0 COMMENT '生成预警数量',
    `error_message` varchar(2048) DEFAULT NULL COMMENT '错误信息',
    `create_user_id` bigint DEFAULT NULL COMMENT '创建人',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_campus_detection_run_log_id` (`run_log_id`),
    KEY `idx_campus_detection_run_task` (`detection_task_id`, `start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='校园舆情检测运行日志表';

INSERT INTO `campus_dict_type` (`dict_type`, `dict_name`, `description`, `sort_no`)
VALUES
    ('detection_rule_type', '检测规则类型', '校园舆情检测规则类型', 260),
    ('detection_task_status', '检测任务状态', '校园舆情检测任务状态', 270),
    ('detection_object_type', '检测对象类型', '校园舆情检测对象类型', 280),
    ('detection_hit_status', '检测命中状态', '校园舆情检测命中处理状态', 290),
    ('detection_run_status', '检测运行状态', '校园舆情检测运行状态', 300)
ON DUPLICATE KEY UPDATE
    `dict_name` = VALUES(`dict_name`),
    `description` = VALUES(`description`),
    `sort_no` = VALUES(`sort_no`),
    `update_time` = CURRENT_TIMESTAMP;

INSERT INTO `campus_dict_item` (`dict_type`, `item_code`, `item_name`, `item_value`, `sort_no`)
VALUES
    ('detection_rule_type', 'keyword_any', '任一关键词', 'keyword_any', 10),
    ('detection_rule_type', 'keyword_all', '全部关键词', 'keyword_all', 20),
    ('detection_rule_type', 'exact', '精确匹配', 'exact', 30),
    ('detection_rule_type', 'regex', '正则匹配', 'regex', 40),
    ('detection_rule_type', 'risk_level', '风险等级', 'risk_level', 50),
    ('detection_task_status', 'active', '启用', 'active', 10),
    ('detection_task_status', 'paused', '暂停', 'paused', 20),
    ('detection_task_status', 'disabled', '禁用', 'disabled', 30),
    ('detection_object_type', 'ingest_record', '接入记录', 'ingest_record', 10),
    ('detection_object_type', 'clue', '线索', 'clue', 20),
    ('detection_object_type', 'account_content', '账号公开动态', 'account_content', 30),
    ('detection_hit_status', 'pending', '待研判', 'pending', 10),
    ('detection_hit_status', 'alerted', '已转预警', 'alerted', 20),
    ('detection_hit_status', 'ignored', '已忽略', 'ignored', 30),
    ('detection_hit_status', 'converted', '已转线索', 'converted', 40),
    ('detection_run_status', 'running', '运行中', 'running', 10),
    ('detection_run_status', 'success', '成功', 'success', 20),
    ('detection_run_status', 'failed', '失败', 'failed', 30),
    ('alert_source', 'detection', '检测命中', 'detection', 50)
ON DUPLICATE KEY UPDATE
    `item_name` = VALUES(`item_name`),
    `item_value` = VALUES(`item_value`),
    `sort_no` = VALUES(`sort_no`),
    `update_time` = CURRENT_TIMESTAMP;
