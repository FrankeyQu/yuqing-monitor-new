-- 校园舆情系统线索库表
-- Batch 3: clue library minimal workflow.

CREATE TABLE IF NOT EXISTS `campus_clue` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `clue_id` bigint NOT NULL COMMENT '线索业务ID',
    `clue_title` varchar(255) NOT NULL COMMENT '线索标题',
    `clue_content` text COMMENT '线索内容',
    `clue_source` varchar(64) DEFAULT NULL COMMENT '线索来源',
    `source_platform` varchar(64) DEFAULT NULL COMMENT '来源平台',
    `original_url` varchar(1024) DEFAULT NULL COMMENT '原文链接',
    `publish_time` datetime DEFAULT NULL COMMENT '发布时间',
    `discover_time` datetime DEFAULT NULL COMMENT '发现时间',
    `involved_department_id` bigint DEFAULT NULL COMMENT '涉及部门ID',
    `involved_account` varchar(512) DEFAULT NULL COMMENT '涉及账号描述',
    `keywords` varchar(512) DEFAULT NULL COMMENT '命中关键词',
    `risk_level` varchar(32) DEFAULT 'normal' COMMENT '风险等级',
    `sentiment` varchar(32) DEFAULT NULL COMMENT '情感倾向',
    `clue_status` varchar(32) DEFAULT 'pending_judge' COMMENT '线索状态(pending_judge|judged|converted|archived)',
    `judge_opinion` text COMMENT '研判意见',
    `judge_user_id` bigint DEFAULT NULL COMMENT '研判人',
    `judge_time` datetime DEFAULT NULL COMMENT '研判时间',
    `event_id` bigint DEFAULT NULL COMMENT '关联事件ID',
    `duplicate_key` varchar(128) DEFAULT NULL COMMENT '去重键',
    `remark` varchar(1024) DEFAULT NULL COMMENT '备注',
    `deleted` tinyint DEFAULT 0 COMMENT '逻辑删除(0否,1是)',
    `create_user_id` bigint DEFAULT NULL COMMENT '创建人',
    `update_user_id` bigint DEFAULT NULL COMMENT '更新人',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_campus_clue_id` (`clue_id`),
    KEY `idx_campus_clue_status` (`clue_status`, `deleted`),
    KEY `idx_campus_clue_risk` (`risk_level`, `deleted`),
    KEY `idx_campus_clue_source` (`clue_source`, `source_platform`),
    KEY `idx_campus_clue_discover_time` (`discover_time`),
    KEY `idx_campus_clue_duplicate` (`duplicate_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='校园舆情线索表';

CREATE TABLE IF NOT EXISTS `campus_clue_attachment` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `attachment_id` bigint NOT NULL COMMENT '附件业务ID',
    `clue_id` bigint NOT NULL COMMENT '线索业务ID',
    `file_name` varchar(255) NOT NULL COMMENT '文件名',
    `file_path` varchar(1024) NOT NULL COMMENT '文件路径',
    `file_type` varchar(64) DEFAULT NULL COMMENT '文件类型',
    `file_size` bigint DEFAULT NULL COMMENT '文件大小',
    `deleted` tinyint DEFAULT 0 COMMENT '逻辑删除(0否,1是)',
    `create_user_id` bigint DEFAULT NULL COMMENT '创建人',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_campus_clue_attachment_id` (`attachment_id`),
    KEY `idx_campus_clue_attachment_clue` (`clue_id`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='校园舆情线索附件表';

CREATE TABLE IF NOT EXISTS `campus_clue_operation_log` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `log_id` bigint NOT NULL COMMENT '日志业务ID',
    `clue_id` bigint NOT NULL COMMENT '线索业务ID',
    `operation_type` varchar(64) NOT NULL COMMENT '操作类型',
    `operation_content` varchar(1024) DEFAULT NULL COMMENT '操作说明',
    `before_value` text COMMENT '变更前摘要',
    `after_value` text COMMENT '变更后摘要',
    `operator_user_id` bigint DEFAULT NULL COMMENT '操作人',
    `operator_name` varchar(128) DEFAULT NULL COMMENT '操作人名称',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_campus_clue_log_id` (`log_id`),
    KEY `idx_campus_clue_log_clue` (`clue_id`, `create_time`),
    KEY `idx_campus_clue_log_operator` (`operator_user_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='校园舆情线索操作日志表';
