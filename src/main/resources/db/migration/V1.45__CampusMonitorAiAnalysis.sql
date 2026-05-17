-- Campus monitor AI task diagnosis and monitor result analysis.

ALTER TABLE `campus_monitor_result`
    ADD COLUMN `ai_summary` varchar(255) DEFAULT NULL COMMENT 'AI一句话摘要' AFTER `sentiment`,
    ADD COLUMN `ai_hit_recommendation` varchar(32) DEFAULT NULL COMMENT 'AI命中建议(hit|not_hit|uncertain)' AFTER `ai_summary`,
    ADD COLUMN `ai_hit_reason` varchar(512) DEFAULT NULL COMMENT 'AI命中建议理由' AFTER `ai_hit_recommendation`,
    ADD COLUMN `ai_confidence` int DEFAULT NULL COMMENT 'AI置信度(0-100)' AFTER `ai_hit_reason`,
    ADD COLUMN `ai_analysis_time` datetime DEFAULT NULL COMMENT 'AI分析时间' AFTER `ai_confidence`,
    ADD COLUMN `ai_provider_code` varchar(64) DEFAULT NULL COMMENT 'AI供应商编码' AFTER `ai_analysis_time`,
    ADD COLUMN `ai_model_code` varchar(64) DEFAULT NULL COMMENT 'AI模型编码' AFTER `ai_provider_code`,
    ADD KEY `idx_campus_monitor_result_ai_hit` (`ai_hit_recommendation`, `ai_analysis_time`);

INSERT INTO `campus_ai_feature_binding` (
    `binding_id`, `feature_code`, `feature_name`, `feature_type`,
    `provider_code`, `model_code`, `enabled`, `failure_strategy`,
    `timeout_ms`, `daily_quota_limit`, `log_prompt`, `remark`, `deleted`
) VALUES
    (392014, 'monitor_result_analysis', '监测命中AI分析', 'llm', 'deepseek', 'deepseek-chat', 1, 'skip', 180000, NULL, 1, '监测命中情感、摘要、命中建议、学校相关性和主题分类', 0),
    (392015, 'monitor_task_diagnosis', '监测任务AI体检', 'llm', 'deepseek', 'deepseek-chat', 1, 'skip', 180000, NULL, 1, '监测任务配置体检和优化建议', 0)
ON DUPLICATE KEY UPDATE
    `feature_name` = VALUES(`feature_name`),
    `feature_type` = VALUES(`feature_type`),
    `provider_code` = VALUES(`provider_code`),
    `model_code` = VALUES(`model_code`),
    `enabled` = VALUES(`enabled`),
    `failure_strategy` = VALUES(`failure_strategy`),
    `timeout_ms` = VALUES(`timeout_ms`),
    `daily_quota_limit` = VALUES(`daily_quota_limit`),
    `log_prompt` = VALUES(`log_prompt`),
    `remark` = VALUES(`remark`),
    `deleted` = 0,
    `update_time` = CURRENT_TIMESTAMP;

INSERT INTO `campus_ai_prompt_template` (
    `template_id`, `feature_code`, `template_name`, `template_version`,
    `system_prompt`, `user_prompt`, `output_format`, `enabled`, `remark`, `deleted`
) VALUES
    (393005, 'monitor_result_analysis', '监测命中AI分析默认模板', 'v1',
     '你是校园舆情监测分析助手。请只返回JSON，不要输出解释。情感只能是positive、neutral、negative、none；命中建议只能是hit、not_hit、uncertain。',
     '请分析以下监测命中列表。任务信息：${taskJson}。命中列表：${itemsJson}。逐条判断情感、一句话摘要、是否应该算作该任务命中、学校相关性、主题分类和理由。',
     '{"results":[{"monitorResultId":"...","sentiment":"positive|neutral|negative|none","summary":"50字内","shouldHit":"hit|not_hit|uncertain","hitReason":"80字内","confidence":0,"schoolRelevanceScore":0,"matchedSchoolTerms":"...","topicCategory":"...","topicSubCategory":"...","topicReason":"..."}]}',
     1, '监测信息页手动批量AI分析默认提示词', 0),
    (393006, 'monitor_task_diagnosis', '监测任务AI体检默认模板', 'v1',
     '你是校园舆情监测任务配置顾问。请只返回JSON，不要展示具体采集内容。',
     '请体检以下监测任务配置和近期统计，只给配置建议，不修改配置。任务：${taskJson}。近期统计：${statsJson}。',
     '{"summary":"...","keywordSuggestions":["..."],"negativeWordSuggestions":["..."],"excludeWordSuggestions":["..."],"platformSuggestions":["..."],"frequencySuggestion":"...","alertModeSuggestion":"...","risks":["..."],"suggestions":["..."]}',
     1, '监测任务页AI体检默认提示词', 0)
ON DUPLICATE KEY UPDATE
    `template_name` = VALUES(`template_name`),
    `system_prompt` = VALUES(`system_prompt`),
    `user_prompt` = VALUES(`user_prompt`),
    `output_format` = VALUES(`output_format`),
    `enabled` = VALUES(`enabled`),
    `remark` = VALUES(`remark`),
    `deleted` = 0,
    `update_time` = CURRENT_TIMESTAMP;
