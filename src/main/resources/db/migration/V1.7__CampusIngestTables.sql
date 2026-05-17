-- 校园舆情公开/授权数据接入表
-- Batch 8: source registry, ingest tasks, normalized records and run logs.

CREATE TABLE IF NOT EXISTS `campus_ingest_source` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `source_id` bigint NOT NULL COMMENT '来源业务ID',
    `source_name` varchar(255) NOT NULL COMMENT '来源名称',
    `source_type` varchar(64) NOT NULL COMMENT '来源类型(manual|api|rss|public_web|upper_transfer)',
    `platform` varchar(64) DEFAULT NULL COMMENT '平台',
    `access_endpoint` varchar(1024) DEFAULT NULL COMMENT '访问端点或来源说明',
    `authorization_basis` varchar(1024) NOT NULL COMMENT '授权或来源依据',
    `authorization_scope` varchar(1024) NOT NULL COMMENT '授权范围',
    `responsible_department_id` bigint DEFAULT NULL COMMENT '责任部门',
    `enabled` tinyint DEFAULT 1 COMMENT '是否启用(1启用,0停用)',
    `remark` varchar(1024) DEFAULT NULL COMMENT '备注',
    `deleted` tinyint DEFAULT 0 COMMENT '逻辑删除(0否,1是)',
    `create_user_id` bigint DEFAULT NULL COMMENT '创建人',
    `update_user_id` bigint DEFAULT NULL COMMENT '更新人',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_campus_ingest_source_id` (`source_id`),
    KEY `idx_campus_ingest_source_type` (`source_type`, `enabled`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='校园舆情数据接入来源表';

CREATE TABLE IF NOT EXISTS `campus_ingest_task` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `task_id` bigint NOT NULL COMMENT '任务业务ID',
    `source_id` bigint NOT NULL COMMENT '来源业务ID',
    `task_name` varchar(255) NOT NULL COMMENT '任务名称',
    `target_type` varchar(64) NOT NULL COMMENT '目标类型(clue|account_content)',
    `adapter_type` varchar(64) DEFAULT 'manual_push' COMMENT '适配器类型(manual_push|api_pull|rss_pull|file_import)',
    `schedule_cron` varchar(128) DEFAULT NULL COMMENT '计划表达式预留',
    `fetch_config` text COMMENT '接入配置(JSON)',
    `task_status` varchar(32) DEFAULT 'paused' COMMENT '任务状态(active|paused|disabled)',
    `last_run_time` datetime DEFAULT NULL COMMENT '最近运行时间',
    `next_run_time` datetime DEFAULT NULL COMMENT '下次运行时间',
    `authorization_scope` varchar(1024) NOT NULL COMMENT '任务授权范围',
    `retention_days` int DEFAULT 180 COMMENT '原始记录保留天数',
    `deleted` tinyint DEFAULT 0 COMMENT '逻辑删除(0否,1是)',
    `create_user_id` bigint DEFAULT NULL COMMENT '创建人',
    `update_user_id` bigint DEFAULT NULL COMMENT '更新人',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_campus_ingest_task_id` (`task_id`),
    KEY `idx_campus_ingest_task_source` (`source_id`, `task_status`, `deleted`),
    KEY `idx_campus_ingest_task_target` (`target_type`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='校园舆情数据接入任务表';

CREATE TABLE IF NOT EXISTS `campus_ingest_record` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `record_id` bigint NOT NULL COMMENT '接入记录业务ID',
    `source_id` bigint NOT NULL COMMENT '来源业务ID',
    `task_id` bigint DEFAULT NULL COMMENT '任务业务ID',
    `external_id` varchar(255) DEFAULT NULL COMMENT '外部数据ID',
    `platform` varchar(64) DEFAULT NULL COMMENT '平台',
    `content_type` varchar(64) DEFAULT NULL COMMENT '内容类型',
    `title` varchar(512) DEFAULT NULL COMMENT '标题',
    `content` mediumtext COMMENT '正文',
    `original_url` varchar(1024) DEFAULT NULL COMMENT '原始链接',
    `publish_time` datetime DEFAULT NULL COMMENT '发布时间',
    `author_name` varchar(255) DEFAULT NULL COMMENT '公开作者或账号名',
    `account_id` bigint DEFAULT NULL COMMENT '关联重点账号ID',
    `account_task_id` bigint DEFAULT NULL COMMENT '关联关注任务ID',
    `keywords` varchar(512) DEFAULT NULL COMMENT '关键词',
    `risk_level` varchar(32) DEFAULT 'normal' COMMENT '风险等级',
    `sentiment` varchar(32) DEFAULT NULL COMMENT '情感倾向',
    `raw_data` mediumtext COMMENT '原始数据',
    `normalized_status` varchar(32) DEFAULT 'pending' COMMENT '标准化状态(pending|converted|ignored|failed)',
    `target_type` varchar(64) DEFAULT NULL COMMENT '转换目标类型',
    `target_id` bigint DEFAULT NULL COMMENT '转换目标业务ID',
    `error_message` varchar(1024) DEFAULT NULL COMMENT '错误信息',
    `deleted` tinyint DEFAULT 0 COMMENT '逻辑删除(0否,1是)',
    `create_user_id` bigint DEFAULT NULL COMMENT '创建人',
    `update_user_id` bigint DEFAULT NULL COMMENT '更新人',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_campus_ingest_record_id` (`record_id`),
    UNIQUE KEY `uk_campus_ingest_record_external` (`source_id`, `external_id`),
    KEY `idx_campus_ingest_record_task` (`task_id`, `normalized_status`, `deleted`),
    KEY `idx_campus_ingest_record_target` (`target_type`, `target_id`),
    KEY `idx_campus_ingest_record_publish` (`publish_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='校园舆情数据接入标准化记录表';

CREATE TABLE IF NOT EXISTS `campus_ingest_run_log` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `run_id` bigint NOT NULL COMMENT '运行日志业务ID',
    `task_id` bigint NOT NULL COMMENT '任务业务ID',
    `run_status` varchar(32) DEFAULT 'running' COMMENT '运行状态(running|success|failed)',
    `start_time` datetime DEFAULT NULL COMMENT '开始时间',
    `end_time` datetime DEFAULT NULL COMMENT '结束时间',
    `fetched_count` int DEFAULT 0 COMMENT '拉取数量',
    `success_count` int DEFAULT 0 COMMENT '成功数量',
    `fail_count` int DEFAULT 0 COMMENT '失败数量',
    `error_message` varchar(2048) DEFAULT NULL COMMENT '错误信息',
    `create_user_id` bigint DEFAULT NULL COMMENT '创建人',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_campus_ingest_run_id` (`run_id`),
    KEY `idx_campus_ingest_run_task` (`task_id`, `start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='校园舆情数据接入运行日志表';

INSERT INTO `campus_dict_type` (`dict_type`, `dict_name`, `description`, `sort_no`)
VALUES
    ('ingest_source_type', '接入来源类型', '校园舆情数据接入来源类型', 160),
    ('ingest_target_type', '接入目标类型', '校园舆情数据接入目标类型', 170),
    ('ingest_task_status', '接入任务状态', '校园舆情数据接入任务状态', 180),
    ('ingest_record_status', '接入记录状态', '校园舆情数据接入记录状态', 190),
    ('ingest_adapter_type', '接入适配器类型', '校园舆情数据接入适配器类型', 200)
ON DUPLICATE KEY UPDATE
    `dict_name` = VALUES(`dict_name`),
    `description` = VALUES(`description`),
    `sort_no` = VALUES(`sort_no`),
    `update_time` = CURRENT_TIMESTAMP;

INSERT INTO `campus_dict_item` (`dict_type`, `item_code`, `item_name`, `item_value`, `sort_no`)
VALUES
    ('ingest_source_type', 'manual', '人工录入', 'manual', 10),
    ('ingest_source_type', 'api', '授权接口', 'api', 20),
    ('ingest_source_type', 'rss', '公开RSS', 'rss', 30),
    ('ingest_source_type', 'public_web', '公开网页', 'public_web', 40),
    ('ingest_source_type', 'upper_transfer', '上级移交', 'upper_transfer', 50),
    ('ingest_target_type', 'clue', '线索库', 'clue', 10),
    ('ingest_target_type', 'account_content', '重点账号公开动态', 'account_content', 20),
    ('ingest_task_status', 'active', '启用', 'active', 10),
    ('ingest_task_status', 'paused', '暂停', 'paused', 20),
    ('ingest_task_status', 'disabled', '禁用', 'disabled', 30),
    ('ingest_record_status', 'pending', '待转换', 'pending', 10),
    ('ingest_record_status', 'converted', '已转换', 'converted', 20),
    ('ingest_record_status', 'ignored', '已忽略', 'ignored', 30),
    ('ingest_record_status', 'failed', '失败', 'failed', 40),
    ('ingest_adapter_type', 'manual_push', '人工推送', 'manual_push', 10),
    ('ingest_adapter_type', 'api_pull', '授权接口拉取', 'api_pull', 20),
    ('ingest_adapter_type', 'rss_pull', '公开RSS拉取', 'rss_pull', 30),
    ('ingest_adapter_type', 'file_import', '文件导入', 'file_import', 40)
ON DUPLICATE KEY UPDATE
    `item_name` = VALUES(`item_name`),
    `item_value` = VALUES(`item_value`),
    `sort_no` = VALUES(`sort_no`),
    `update_time` = CURRENT_TIMESTAMP;
