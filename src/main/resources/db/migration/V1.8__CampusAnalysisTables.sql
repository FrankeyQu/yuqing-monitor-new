-- 校园舆情辅助研判表
-- Batch 9: analysis tasks and assistive analysis results.

CREATE TABLE IF NOT EXISTS `campus_analysis_task` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `analysis_task_id` bigint NOT NULL COMMENT '分析任务业务ID',
    `object_type` varchar(64) NOT NULL COMMENT '对象类型(clue|event|account_content)',
    `object_id` bigint NOT NULL COMMENT '对象业务ID',
    `analysis_type` varchar(64) NOT NULL COMMENT '分析类型(sentiment|risk|summary|keywords|comprehensive)',
    `task_status` varchar(32) DEFAULT 'pending' COMMENT '任务状态(pending|running|completed|failed)',
    `request_payload` mediumtext COMMENT '请求参数',
    `model_provider` varchar(64) DEFAULT 'local_heuristic' COMMENT '模型提供方',
    `model_name` varchar(128) DEFAULT 'local_heuristic_v1' COMMENT '模型名称',
    `error_message` varchar(2048) DEFAULT NULL COMMENT '错误信息',
    `deleted` tinyint DEFAULT 0 COMMENT '逻辑删除(0否,1是)',
    `create_user_id` bigint DEFAULT NULL COMMENT '创建人',
    `update_user_id` bigint DEFAULT NULL COMMENT '更新人',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_campus_analysis_task_id` (`analysis_task_id`),
    KEY `idx_campus_analysis_task_object` (`object_type`, `object_id`, `deleted`),
    KEY `idx_campus_analysis_task_status` (`task_status`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='校园舆情辅助研判任务表';

CREATE TABLE IF NOT EXISTS `campus_analysis_result` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `analysis_result_id` bigint NOT NULL COMMENT '分析结果业务ID',
    `analysis_task_id` bigint NOT NULL COMMENT '分析任务业务ID',
    `object_type` varchar(64) NOT NULL COMMENT '对象类型(clue|event|account_content)',
    `object_id` bigint NOT NULL COMMENT '对象业务ID',
    `analysis_type` varchar(64) NOT NULL COMMENT '分析类型',
    `sentiment` varchar(32) DEFAULT NULL COMMENT '情感倾向',
    `suggested_risk_level` varchar(32) DEFAULT NULL COMMENT '建议风险等级',
    `summary` text COMMENT '摘要',
    `keywords` varchar(1024) DEFAULT NULL COMMENT '关键词',
    `similar_object_ids` varchar(1024) DEFAULT NULL COMMENT '相似对象ID',
    `confidence` decimal(5,2) DEFAULT NULL COMMENT '置信度',
    `result_payload` mediumtext COMMENT '结果详情(JSON)',
    `assistive_label` varchar(255) DEFAULT '仅供辅助研判' COMMENT '辅助研判标识',
    `adoption_status` varchar(32) DEFAULT 'pending' COMMENT '采纳状态(pending|adopted|rejected)',
    `reviewer_user_id` bigint DEFAULT NULL COMMENT '复核人',
    `review_time` datetime DEFAULT NULL COMMENT '复核时间',
    `review_opinion` varchar(1024) DEFAULT NULL COMMENT '复核意见',
    `deleted` tinyint DEFAULT 0 COMMENT '逻辑删除(0否,1是)',
    `create_user_id` bigint DEFAULT NULL COMMENT '创建人',
    `update_user_id` bigint DEFAULT NULL COMMENT '更新人',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_campus_analysis_result_id` (`analysis_result_id`),
    KEY `idx_campus_analysis_result_task` (`analysis_task_id`, `deleted`),
    KEY `idx_campus_analysis_result_object` (`object_type`, `object_id`, `deleted`),
    KEY `idx_campus_analysis_result_adoption` (`adoption_status`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='校园舆情辅助研判结果表';

INSERT INTO `campus_dict_type` (`dict_type`, `dict_name`, `description`, `sort_no`)
VALUES
    ('analysis_object_type', '分析对象类型', '校园舆情辅助研判对象类型', 210),
    ('analysis_type', '分析类型', '校园舆情辅助研判类型', 220),
    ('analysis_task_status', '分析任务状态', '校园舆情辅助研判任务状态', 230),
    ('analysis_adoption_status', '分析采纳状态', '校园舆情辅助研判采纳状态', 240)
ON DUPLICATE KEY UPDATE
    `dict_name` = VALUES(`dict_name`),
    `description` = VALUES(`description`),
    `sort_no` = VALUES(`sort_no`),
    `update_time` = CURRENT_TIMESTAMP;

INSERT INTO `campus_dict_item` (`dict_type`, `item_code`, `item_name`, `item_value`, `sort_no`)
VALUES
    ('analysis_object_type', 'clue', '线索', 'clue', 10),
    ('analysis_object_type', 'event', '事件', 'event', 20),
    ('analysis_object_type', 'account_content', '账号公开动态', 'account_content', 30),
    ('analysis_type', 'sentiment', '情感分析', 'sentiment', 10),
    ('analysis_type', 'risk', '风险建议', 'risk', 20),
    ('analysis_type', 'summary', '摘要生成', 'summary', 30),
    ('analysis_type', 'keywords', '关键词提取', 'keywords', 40),
    ('analysis_type', 'comprehensive', '综合研判', 'comprehensive', 50),
    ('analysis_task_status', 'pending', '待运行', 'pending', 10),
    ('analysis_task_status', 'running', '运行中', 'running', 20),
    ('analysis_task_status', 'completed', '已完成', 'completed', 30),
    ('analysis_task_status', 'failed', '失败', 'failed', 40),
    ('analysis_adoption_status', 'pending', '待复核', 'pending', 10),
    ('analysis_adoption_status', 'adopted', '已采纳', 'adopted', 20),
    ('analysis_adoption_status', 'rejected', '已驳回', 'rejected', 30)
ON DUPLICATE KEY UPDATE
    `item_name` = VALUES(`item_name`),
    `item_value` = VALUES(`item_value`),
    `sort_no` = VALUES(`sort_no`),
    `update_time` = CURRENT_TIMESTAMP;
