-- Batch 26: API credential reference governance, task quota and API call audit logs.
-- Credentials remain external environment references; this migration stores references and call diagnostics only.

ALTER TABLE `campus_ingest_task`
    ADD COLUMN `daily_quota_limit` int DEFAULT 0 COMMENT '单任务每日外部API调用额度，0表示不限额' AFTER `detection_task_ids`,
    ADD COLUMN `daily_quota_used` int DEFAULT 0 COMMENT '当前额度日期已使用次数' AFTER `daily_quota_limit`,
    ADD COLUMN `quota_stat_date` date DEFAULT NULL COMMENT '额度统计日期' AFTER `daily_quota_used`,
    ADD COLUMN `auto_pause_after_fail_count` int DEFAULT 0 COMMENT '调度连续失败达到该数量后自动暂停，0表示不自动暂停' AFTER `quota_stat_date`,
    ADD COLUMN `governance_remark` varchar(1024) DEFAULT NULL COMMENT '接入治理说明' AFTER `auto_pause_after_fail_count`,
    ADD KEY `idx_campus_ingest_task_quota` (`quota_stat_date`, `daily_quota_limit`, `deleted`);

CREATE TABLE IF NOT EXISTS `campus_ingest_api_call_log` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `call_id` bigint NOT NULL COMMENT '调用日志业务ID',
    `run_id` bigint DEFAULT NULL COMMENT '接入运行日志业务ID',
    `task_id` bigint DEFAULT NULL COMMENT '接入任务业务ID',
    `source_id` bigint DEFAULT NULL COMMENT '接入来源业务ID',
    `provider` varchar(64) NOT NULL COMMENT '第三方供应商',
    `endpoint_key` varchar(128) DEFAULT NULL COMMENT '端点白名单Key',
    `credential_ref` varchar(128) DEFAULT NULL COMMENT '密钥引用名，不保存密钥值',
    `request_time` datetime DEFAULT NULL COMMENT '调用开始时间',
    `duration_ms` bigint DEFAULT NULL COMMENT '调用耗时毫秒',
    `call_status` varchar(32) NOT NULL COMMENT '调用状态(success|failed)',
    `http_status` int DEFAULT NULL COMMENT 'HTTP状态码',
    `error_type` varchar(64) DEFAULT NULL COMMENT '错误分类',
    `error_message` varchar(2048) DEFAULT NULL COMMENT '脱敏错误摘要',
    `cost_units` int DEFAULT 0 COMMENT '本次消耗额度单位',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_campus_ingest_api_call_id` (`call_id`),
    KEY `idx_campus_ingest_api_call_task` (`task_id`, `request_time`),
    KEY `idx_campus_ingest_api_call_run` (`run_id`),
    KEY `idx_campus_ingest_api_call_endpoint` (`provider`, `endpoint_key`, `request_time`),
    KEY `idx_campus_ingest_api_call_status` (`call_status`, `request_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='校园舆情第三方API调用审计日志表';
