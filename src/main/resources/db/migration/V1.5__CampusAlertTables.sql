-- 校园舆情系统预警规则与统计基础表
-- Batch 6: sensitive words, alert rules and alert records.

CREATE TABLE IF NOT EXISTS `campus_sensitive_word` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `word_id` bigint NOT NULL COMMENT '敏感词业务ID',
    `word_text` varchar(255) NOT NULL COMMENT '敏感词',
    `word_category` varchar(64) DEFAULT NULL COMMENT '敏感词类别',
    `risk_level` varchar(32) DEFAULT 'normal' COMMENT '风险等级',
    `match_type` varchar(32) DEFAULT 'contains' COMMENT '匹配方式(contains|exact|regex)',
    `status` tinyint DEFAULT 1 COMMENT '状态(1启用,0停用)',
    `remark` varchar(512) DEFAULT NULL COMMENT '备注',
    `deleted` tinyint DEFAULT 0 COMMENT '逻辑删除(0否,1是)',
    `create_user_id` bigint DEFAULT NULL COMMENT '创建人',
    `update_user_id` bigint DEFAULT NULL COMMENT '更新人',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_campus_sensitive_word_id` (`word_id`),
    UNIQUE KEY `uk_campus_sensitive_word_text` (`word_text`, `word_category`),
    KEY `idx_campus_sensitive_word_status` (`status`, `deleted`),
    KEY `idx_campus_sensitive_word_risk` (`risk_level`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='校园舆情敏感词表';

CREATE TABLE IF NOT EXISTS `campus_alert_rule` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `rule_id` bigint NOT NULL COMMENT '规则业务ID',
    `rule_name` varchar(255) NOT NULL COMMENT '规则名称',
    `rule_type` varchar(64) NOT NULL COMMENT '规则类型(sensitive_word|keyword|account_content|clue_risk)',
    `rule_condition` text COMMENT '规则条件(JSON或文本)',
    `risk_level` varchar(32) DEFAULT 'normal' COMMENT '风险等级',
    `enabled` tinyint DEFAULT 1 COMMENT '是否启用(1启用,0停用)',
    `description` varchar(1024) DEFAULT NULL COMMENT '规则说明',
    `deleted` tinyint DEFAULT 0 COMMENT '逻辑删除(0否,1是)',
    `create_user_id` bigint DEFAULT NULL COMMENT '创建人',
    `update_user_id` bigint DEFAULT NULL COMMENT '更新人',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_campus_alert_rule_id` (`rule_id`),
    KEY `idx_campus_alert_rule_type` (`rule_type`, `enabled`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='校园舆情预警规则表';

CREATE TABLE IF NOT EXISTS `campus_alert` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `alert_id` bigint NOT NULL COMMENT '预警业务ID',
    `alert_title` varchar(255) NOT NULL COMMENT '预警标题',
    `alert_content` text COMMENT '预警内容',
    `alert_source` varchar(64) NOT NULL COMMENT '预警来源(clue|account_content|event|manual)',
    `source_object_id` bigint DEFAULT NULL COMMENT '来源对象业务ID',
    `rule_id` bigint DEFAULT NULL COMMENT '命中规则ID',
    `risk_level` varchar(32) DEFAULT 'normal' COMMENT '风险等级',
    `matched_keywords` varchar(512) DEFAULT NULL COMMENT '命中关键词',
    `alert_status` varchar(32) DEFAULT 'pending' COMMENT '预警状态(pending|handled|ignored)',
    `handle_opinion` varchar(1024) DEFAULT NULL COMMENT '处理意见',
    `handler_user_id` bigint DEFAULT NULL COMMENT '处理人',
    `handle_time` datetime DEFAULT NULL COMMENT '处理时间',
    `deleted` tinyint DEFAULT 0 COMMENT '逻辑删除(0否,1是)',
    `create_user_id` bigint DEFAULT NULL COMMENT '创建人',
    `update_user_id` bigint DEFAULT NULL COMMENT '更新人',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_campus_alert_id` (`alert_id`),
    KEY `idx_campus_alert_source` (`alert_source`, `source_object_id`),
    KEY `idx_campus_alert_rule` (`rule_id`),
    KEY `idx_campus_alert_status` (`alert_status`, `risk_level`, `deleted`),
    KEY `idx_campus_alert_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='校园舆情预警记录表';

INSERT INTO `campus_dict_type` (`dict_type`, `dict_name`, `description`, `sort_no`)
VALUES
    ('alert_status', '预警状态', '校园舆情预警处理状态', 100),
    ('alert_rule_type', '预警规则类型', '校园舆情预警规则类型', 110),
    ('alert_source', '预警来源', '校园舆情预警来源对象', 120)
ON DUPLICATE KEY UPDATE
    `dict_name` = VALUES(`dict_name`),
    `description` = VALUES(`description`),
    `sort_no` = VALUES(`sort_no`),
    `update_time` = CURRENT_TIMESTAMP;

INSERT INTO `campus_dict_item` (`dict_type`, `item_code`, `item_name`, `item_value`, `sort_no`)
VALUES
    ('alert_status', 'pending', '待处理', 'pending', 10),
    ('alert_status', 'handled', '已处理', 'handled', 20),
    ('alert_status', 'ignored', '已忽略', 'ignored', 30),
    ('alert_rule_type', 'sensitive_word', '敏感词命中', 'sensitive_word', 10),
    ('alert_rule_type', 'keyword', '关键词命中', 'keyword', 20),
    ('alert_rule_type', 'account_content', '重点账号动态', 'account_content', 30),
    ('alert_rule_type', 'clue_risk', '线索风险等级', 'clue_risk', 40),
    ('alert_source', 'clue', '线索', 'clue', 10),
    ('alert_source', 'account_content', '账号公开动态', 'account_content', 20),
    ('alert_source', 'event', '舆情事件', 'event', 30),
    ('alert_source', 'manual', '人工创建', 'manual', 40)
ON DUPLICATE KEY UPDATE
    `item_name` = VALUES(`item_name`),
    `item_value` = VALUES(`item_value`),
    `sort_no` = VALUES(`sort_no`),
    `update_time` = CURRENT_TIMESTAMP;
