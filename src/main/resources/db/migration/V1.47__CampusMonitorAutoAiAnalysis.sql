-- Track automatic and manual AI analysis progress for campus monitor results.

ALTER TABLE `campus_monitor_result`
    ADD COLUMN `ai_analysis_status` varchar(32) NOT NULL DEFAULT 'none' COMMENT 'AI分析状态(none|pending|processing|done|failed)' AFTER `ai_model_code`,
    ADD COLUMN `ai_analysis_trigger` varchar(32) DEFAULT NULL COMMENT 'AI分析触发方式(auto|manual)' AFTER `ai_analysis_status`,
    ADD COLUMN `ai_analysis_error` varchar(512) DEFAULT NULL COMMENT 'AI分析失败原因' AFTER `ai_analysis_trigger`,
    ADD COLUMN `ai_last_attempt_time` datetime DEFAULT NULL COMMENT 'AI最近尝试时间' AFTER `ai_analysis_error`,
    ADD KEY `idx_campus_monitor_result_ai_status` (`ai_analysis_status`, `deleted`, `create_time`);
