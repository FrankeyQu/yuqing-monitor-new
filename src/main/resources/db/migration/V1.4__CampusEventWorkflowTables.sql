-- 校园舆情系统事件与处置闭环表
-- Batch 5: event workflow and disposal tasks.

CREATE TABLE IF NOT EXISTS `campus_event` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `event_id` bigint NOT NULL COMMENT '事件业务ID',
    `event_title` varchar(255) NOT NULL COMMENT '事件标题',
    `event_type` varchar(64) DEFAULT NULL COMMENT '事件类型',
    `event_summary` text COMMENT '事件摘要',
    `first_publish_time` datetime DEFAULT NULL COMMENT '首发时间',
    `discover_time` datetime DEFAULT NULL COMMENT '发现时间',
    `risk_level` varchar(32) DEFAULT 'normal' COMMENT '风险等级',
    `impact_scope` varchar(512) DEFAULT NULL COMMENT '影响范围',
    `involved_department_id` bigint DEFAULT NULL COMMENT '涉及部门ID',
    `current_heat` int DEFAULT 0 COMMENT '当前热度',
    `event_status` varchar(32) DEFAULT 'pending_judge' COMMENT '事件状态',
    `disposal_requirement` varchar(1024) DEFAULT NULL COMMENT '处置要求',
    `archive_conclusion` text COMMENT '归档结论',
    `deleted` tinyint DEFAULT 0 COMMENT '逻辑删除(0否,1是)',
    `create_user_id` bigint DEFAULT NULL COMMENT '创建人',
    `update_user_id` bigint DEFAULT NULL COMMENT '更新人',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_campus_event_id` (`event_id`),
    KEY `idx_campus_event_status` (`event_status`, `deleted`),
    KEY `idx_campus_event_risk` (`risk_level`, `deleted`),
    KEY `idx_campus_event_discover_time` (`discover_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='校园舆情事件表';

CREATE TABLE IF NOT EXISTS `campus_event_clue` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `relation_id` bigint NOT NULL COMMENT '关系业务ID',
    `event_id` bigint NOT NULL COMMENT '事件业务ID',
    `clue_id` bigint NOT NULL COMMENT '线索业务ID',
    `deleted` tinyint DEFAULT 0 COMMENT '逻辑删除(0否,1是)',
    `create_user_id` bigint DEFAULT NULL COMMENT '创建人',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_campus_event_clue_id` (`relation_id`),
    UNIQUE KEY `uk_campus_event_clue_pair` (`event_id`, `clue_id`),
    KEY `idx_campus_event_clue_clue` (`clue_id`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='校园舆情事件线索关联表';

CREATE TABLE IF NOT EXISTS `campus_event_account` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `relation_id` bigint NOT NULL COMMENT '关系业务ID',
    `event_id` bigint NOT NULL COMMENT '事件业务ID',
    `account_id` bigint NOT NULL COMMENT '账号业务ID',
    `deleted` tinyint DEFAULT 0 COMMENT '逻辑删除(0否,1是)',
    `create_user_id` bigint DEFAULT NULL COMMENT '创建人',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_campus_event_account_id` (`relation_id`),
    UNIQUE KEY `uk_campus_event_account_pair` (`event_id`, `account_id`),
    KEY `idx_campus_event_account_account` (`account_id`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='校园舆情事件账号关联表';

CREATE TABLE IF NOT EXISTS `campus_disposal_task` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `disposal_task_id` bigint NOT NULL COMMENT '处置任务业务ID',
    `event_id` bigint NOT NULL COMMENT '事件业务ID',
    `task_title` varchar(255) NOT NULL COMMENT '任务标题',
    `assigned_department_id` bigint NOT NULL COMMENT '承办部门ID',
    `assigned_user_id` bigint DEFAULT NULL COMMENT '承办人用户ID',
    `disposal_requirement` varchar(1024) DEFAULT NULL COMMENT '处置要求',
    `due_time` datetime DEFAULT NULL COMMENT '办理时限',
    `task_status` varchar(32) DEFAULT 'pending' COMMENT '任务状态(pending|processing|returned|completed|confirmed)',
    `feedback_summary` varchar(1024) DEFAULT NULL COMMENT '反馈摘要',
    `deleted` tinyint DEFAULT 0 COMMENT '逻辑删除(0否,1是)',
    `create_user_id` bigint DEFAULT NULL COMMENT '创建人',
    `update_user_id` bigint DEFAULT NULL COMMENT '更新人',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_campus_disposal_task_id` (`disposal_task_id`),
    KEY `idx_campus_disposal_event` (`event_id`, `deleted`),
    KEY `idx_campus_disposal_department` (`assigned_department_id`, `task_status`),
    KEY `idx_campus_disposal_due` (`due_time`, `task_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='校园舆情处置任务表';

CREATE TABLE IF NOT EXISTS `campus_disposal_record` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `record_id` bigint NOT NULL COMMENT '反馈记录业务ID',
    `disposal_task_id` bigint NOT NULL COMMENT '处置任务业务ID',
    `event_id` bigint NOT NULL COMMENT '事件业务ID',
    `record_type` varchar(64) DEFAULT 'feedback' COMMENT '记录类型(feedback|return|confirm)',
    `record_content` text COMMENT '记录内容',
    `handler_user_id` bigint DEFAULT NULL COMMENT '处理人用户ID',
    `handler_name` varchar(128) DEFAULT NULL COMMENT '处理人名称',
    `handle_time` datetime DEFAULT NULL COMMENT '处理时间',
    `attachment_desc` varchar(1024) DEFAULT NULL COMMENT '附件说明',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_campus_disposal_record_id` (`record_id`),
    KEY `idx_campus_disposal_record_task` (`disposal_task_id`, `create_time`),
    KEY `idx_campus_disposal_record_event` (`event_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='校园舆情处置反馈记录表';
