-- Campus AI capability management.
-- Centralizes providers, models, feature bindings, prompt templates and AI call logs.

CREATE TABLE IF NOT EXISTS `campus_ai_provider` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `provider_id` bigint NOT NULL COMMENT '供应商业务ID',
    `provider_code` varchar(64) NOT NULL COMMENT '供应商编码',
    `provider_name` varchar(128) NOT NULL COMMENT '供应商名称',
    `provider_type` varchar(64) NOT NULL COMMENT '供应商类型(llm|web_search|content_extract|social_ingest|legacy_writer|legacy_nlp)',
    `base_url` varchar(512) DEFAULT NULL COMMENT '接入点地址',
    `auth_type` varchar(32) DEFAULT 'bearer' COMMENT '鉴权方式(none|bearer|header|custom)',
    `credential_ref` varchar(128) DEFAULT NULL COMMENT '密钥环境变量引用',
    `enabled` tinyint DEFAULT 1 COMMENT '是否启用(1启用,0停用)',
    `timeout_ms` int DEFAULT 30000 COMMENT '默认超时毫秒',
    `max_retries` int DEFAULT 0 COMMENT '最大重试次数',
    `daily_quota_limit` int DEFAULT NULL COMMENT '每日额度上限',
    `quota_used_today` int DEFAULT 0 COMMENT '今日已用额度',
    `quota_stat_date` date DEFAULT NULL COMMENT '额度统计日期',
    `remark` varchar(1024) DEFAULT NULL COMMENT '备注',
    `deleted` tinyint DEFAULT 0 COMMENT '逻辑删除(0否,1是)',
    `create_user_id` bigint DEFAULT NULL COMMENT '创建人',
    `update_user_id` bigint DEFAULT NULL COMMENT '更新人',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_campus_ai_provider_id` (`provider_id`),
    UNIQUE KEY `uk_campus_ai_provider_code` (`provider_code`),
    KEY `idx_campus_ai_provider_type` (`provider_type`, `enabled`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='校园AI供应商配置表';

CREATE TABLE IF NOT EXISTS `campus_ai_model` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `model_id` bigint NOT NULL COMMENT '模型业务ID',
    `provider_code` varchar(64) NOT NULL COMMENT '供应商编码',
    `model_code` varchar(128) NOT NULL COMMENT '模型编码',
    `model_name` varchar(128) NOT NULL COMMENT '模型名称',
    `context_length` int DEFAULT NULL COMMENT '上下文长度',
    `default_temperature` decimal(4,2) DEFAULT 0.20 COMMENT '默认温度',
    `default_max_tokens` int DEFAULT 4096 COMMENT '默认最大输出token',
    `support_stream` tinyint DEFAULT 1 COMMENT '是否支持流式输出',
    `enabled` tinyint DEFAULT 1 COMMENT '是否启用(1启用,0停用)',
    `remark` varchar(1024) DEFAULT NULL COMMENT '备注',
    `deleted` tinyint DEFAULT 0 COMMENT '逻辑删除(0否,1是)',
    `create_user_id` bigint DEFAULT NULL COMMENT '创建人',
    `update_user_id` bigint DEFAULT NULL COMMENT '更新人',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_campus_ai_model_id` (`model_id`),
    UNIQUE KEY `uk_campus_ai_model_code` (`provider_code`, `model_code`),
    KEY `idx_campus_ai_model_provider` (`provider_code`, `enabled`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='校园AI模型配置表';

CREATE TABLE IF NOT EXISTS `campus_ai_feature_binding` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `binding_id` bigint NOT NULL COMMENT '绑定业务ID',
    `feature_code` varchar(64) NOT NULL COMMENT '功能编码',
    `feature_name` varchar(128) NOT NULL COMMENT '功能名称',
    `feature_type` varchar(64) NOT NULL COMMENT '功能类型(llm|ingest|extract|rule|legacy)',
    `provider_code` varchar(64) DEFAULT NULL COMMENT '主供应商编码',
    `model_code` varchar(128) DEFAULT NULL COMMENT '主模型编码',
    `fallback_provider_code` varchar(64) DEFAULT NULL COMMENT '备用供应商编码',
    `fallback_model_code` varchar(128) DEFAULT NULL COMMENT '备用模型编码',
    `enabled` tinyint DEFAULT 1 COMMENT '是否启用(1启用,0停用)',
    `failure_strategy` varchar(64) DEFAULT 'fail' COMMENT '失败策略(fail|skip|fallback_rule|fallback_keywords|preserve_summary)',
    `timeout_ms` int DEFAULT NULL COMMENT '功能级超时毫秒',
    `daily_quota_limit` int DEFAULT NULL COMMENT '功能每日额度上限',
    `quota_used_today` int DEFAULT 0 COMMENT '今日已用额度',
    `quota_stat_date` date DEFAULT NULL COMMENT '额度统计日期',
    `log_prompt` tinyint DEFAULT 0 COMMENT '是否记录提示词快照',
    `remark` varchar(1024) DEFAULT NULL COMMENT '备注',
    `deleted` tinyint DEFAULT 0 COMMENT '逻辑删除(0否,1是)',
    `create_user_id` bigint DEFAULT NULL COMMENT '创建人',
    `update_user_id` bigint DEFAULT NULL COMMENT '更新人',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_campus_ai_binding_id` (`binding_id`),
    UNIQUE KEY `uk_campus_ai_feature_code` (`feature_code`),
    KEY `idx_campus_ai_feature_provider` (`provider_code`, `enabled`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='校园AI功能绑定表';

CREATE TABLE IF NOT EXISTS `campus_ai_prompt_template` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `template_id` bigint NOT NULL COMMENT '模板业务ID',
    `feature_code` varchar(64) NOT NULL COMMENT '功能编码',
    `template_name` varchar(128) NOT NULL COMMENT '模板名称',
    `template_version` varchar(32) DEFAULT 'v1' COMMENT '模板版本',
    `system_prompt` mediumtext COMMENT 'System Prompt',
    `user_prompt` mediumtext COMMENT 'User Prompt模板',
    `output_format` varchar(2048) DEFAULT NULL COMMENT '输出格式要求',
    `enabled` tinyint DEFAULT 1 COMMENT '是否启用(1启用,0停用)',
    `remark` varchar(1024) DEFAULT NULL COMMENT '备注',
    `deleted` tinyint DEFAULT 0 COMMENT '逻辑删除(0否,1是)',
    `create_user_id` bigint DEFAULT NULL COMMENT '创建人',
    `update_user_id` bigint DEFAULT NULL COMMENT '更新人',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_campus_ai_prompt_id` (`template_id`),
    UNIQUE KEY `uk_campus_ai_prompt_feature_version` (`feature_code`, `template_version`),
    KEY `idx_campus_ai_prompt_feature` (`feature_code`, `enabled`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='校园AI提示词模板表';

CREATE TABLE IF NOT EXISTS `campus_ai_call_log` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `call_id` bigint NOT NULL COMMENT '调用业务ID',
    `feature_code` varchar(64) DEFAULT NULL COMMENT '功能编码',
    `provider_code` varchar(64) DEFAULT NULL COMMENT '供应商编码',
    `model_code` varchar(128) DEFAULT NULL COMMENT '模型编码',
    `endpoint` varchar(512) DEFAULT NULL COMMENT '调用端点',
    `request_time` datetime NOT NULL COMMENT '请求时间',
    `duration_ms` bigint DEFAULT NULL COMMENT '耗时毫秒',
    `call_status` varchar(32) NOT NULL COMMENT '调用状态(success|failed)',
    `http_status` int DEFAULT NULL COMMENT 'HTTP状态码',
    `error_type` varchar(64) DEFAULT NULL COMMENT '错误类型',
    `error_message` varchar(2048) DEFAULT NULL COMMENT '错误信息',
    `prompt_tokens` int DEFAULT NULL COMMENT '提示词token',
    `completion_tokens` int DEFAULT NULL COMMENT '输出token',
    `total_tokens` int DEFAULT NULL COMMENT '总token',
    `quota_units` int DEFAULT 1 COMMENT '额度单位',
    `request_snapshot` mediumtext COMMENT '脱敏请求快照',
    `response_snapshot` mediumtext COMMENT '脱敏响应快照',
    `deleted` tinyint DEFAULT 0 COMMENT '逻辑删除(0否,1是)',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_campus_ai_call_id` (`call_id`),
    KEY `idx_campus_ai_call_feature` (`feature_code`, `request_time`),
    KEY `idx_campus_ai_call_provider` (`provider_code`, `request_time`),
    KEY `idx_campus_ai_call_status` (`call_status`, `request_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='校园AI调用日志表';

INSERT INTO `campus_ai_provider` (
    `provider_id`, `provider_code`, `provider_name`, `provider_type`, `base_url`,
    `auth_type`, `credential_ref`, `enabled`, `timeout_ms`, `max_retries`, `remark`, `deleted`
) VALUES
    (390001, 'deepseek', 'DeepSeek', 'llm', 'https://api.deepseek.com/v1/chat/completions', 'bearer', 'DEEPSEEK_API_KEY', 1, 180000, 0, '当前校园报告、研判和词云提取主模型供应商', 0),
    (390002, 'tikhub', 'TikHub', 'social_ingest', 'https://api.tikhub.io', 'bearer', 'TIKHUB_API_KEY', 1, 30000, 0, '抖音、小红书、微博、B站、知乎、微信公众号、快手公开内容接入', 0),
    (390003, 'baidu_qianfan', '百度千帆 AI Search', 'web_search', 'https://qianfan.baidubce.com/v2/ai_search/web_search', 'header', 'BAIDU_API_KEY', 1, 30000, 0, '新闻/网页搜索和教育专题公开网页发现', 0),
    (390004, 'jina_reader', 'Jina Reader', 'content_extract', 'https://r.jina.ai', 'bearer', 'JINA_READER_API_KEY', 1, 30000, 0, '百度结果和白名单公开网页正文提取，可配置为代理地址', 0),
    (390005, 'dashscope', 'DashScope/Qwen', 'llm', '', 'bearer', 'LLM_DASHCOPE_KEY', 0, 180000, 0, '旧专题舆情研判备用模型，P2 历史能力，仅登记不主用', 0),
    (390006, 'xie_writer', '写作宝', 'legacy_writer', '', 'custom', 'ACCOUNT_PUBLIC_XIE_SECRET_KEY', 0, 180000, 0, '旧系统智写报告能力，P2 历史能力，仅登记不主用', 0),
    (390007, 'legacy_nlp', '旧 NLP 服务', 'legacy_nlp', '', 'custom', 'NLP_SERVICE_PASSWORD', 0, 30000, 0, '旧 OCR/图片识别/分词能力，P2 历史能力，仅登记不主用', 0)
ON DUPLICATE KEY UPDATE
    `provider_name` = VALUES(`provider_name`),
    `provider_type` = VALUES(`provider_type`),
    `base_url` = VALUES(`base_url`),
    `auth_type` = VALUES(`auth_type`),
    `credential_ref` = VALUES(`credential_ref`),
    `enabled` = VALUES(`enabled`),
    `timeout_ms` = VALUES(`timeout_ms`),
    `max_retries` = VALUES(`max_retries`),
    `remark` = VALUES(`remark`),
    `deleted` = 0,
    `update_time` = CURRENT_TIMESTAMP;

INSERT INTO `campus_ai_model` (
    `model_id`, `provider_code`, `model_code`, `model_name`, `context_length`,
    `default_temperature`, `default_max_tokens`, `support_stream`, `enabled`, `remark`, `deleted`
) VALUES
    (391001, 'deepseek', 'deepseek-chat', 'DeepSeek Chat / V4 Pro兼容模型', 64000, 0.20, 4096, 1, 1, '默认保持现有兼容模型；如供应商开通 V4 Pro 编码，可在后台调整', 0),
    (391002, 'deepseek', 'deepseek-v4-pro', 'DeepSeek V4 Pro', 64000, 0.20, 4096, 1, 0, '预留模型编码，确认供应商实际 model code 后启用', 0),
    (391003, 'dashscope', 'qwen-plus', 'Qwen Plus', 32000, 0.20, 4096, 1, 0, '旧专题研判备用模型，默认不启用', 0)
ON DUPLICATE KEY UPDATE
    `model_name` = VALUES(`model_name`),
    `context_length` = VALUES(`context_length`),
    `default_temperature` = VALUES(`default_temperature`),
    `default_max_tokens` = VALUES(`default_max_tokens`),
    `support_stream` = VALUES(`support_stream`),
    `enabled` = VALUES(`enabled`),
    `remark` = VALUES(`remark`),
    `deleted` = 0,
    `update_time` = CURRENT_TIMESTAMP;

INSERT INTO `campus_ai_feature_binding` (
    `binding_id`, `feature_code`, `feature_name`, `feature_type`,
    `provider_code`, `model_code`, `enabled`, `failure_strategy`,
    `timeout_ms`, `daily_quota_limit`, `log_prompt`, `remark`, `deleted`
) VALUES
    (392001, 'report_generate', 'AI报告生成', 'llm', 'deepseek', 'deepseek-chat', 1, 'fail', 180000, NULL, 1, '报告归档和手动 AI 报告生成', 0),
    (392002, 'auto_report_generate', '自动报告生成', 'llm', 'deepseek', 'deepseek-chat', 1, 'fail', 180000, NULL, 1, '自动报告任务 AI 生成', 0),
    (392003, 'minority_judgment', '蒙维语AI研判', 'llm', 'deepseek', 'deepseek-chat', 1, 'fallback_rule', 180000, NULL, 1, '少数民族语言翻译、情感、主题、风险和摘要', 0),
    (392004, 'keyword_extract', '关键词提取', 'llm', 'deepseek', 'deepseek-chat', 1, 'fallback_keywords', 60000, NULL, 0, '从正文中提取结构化关键词，供词云和报告使用', 0),
    (392005, 'word_cloud_extract', '词云热词提取', 'llm', 'deepseek', 'deepseek-chat', 1, 'fallback_keywords', 60000, NULL, 0, '首页词云优先用 DeepSeek 提取热词，失败回退命中词统计', 0),
    (392006, 'social_ingest', '社媒公开内容接入', 'ingest', 'tikhub', NULL, 1, 'fail', 30000, NULL, 0, 'TikHub 社媒搜索和详情增强', 0),
    (392007, 'web_search', '网页/新闻搜索', 'ingest', 'baidu_qianfan', NULL, 1, 'preserve_summary', 30000, NULL, 0, '百度千帆 AI Search 发现网页和摘要', 0),
    (392008, 'content_extract', '公开网页正文提取', 'extract', 'jina_reader', NULL, 1, 'preserve_summary', 30000, NULL, 0, 'Jina Reader 正文增强', 0),
    (392009, 'local_rule_judgment', '本地规则研判', 'rule', NULL, NULL, 1, 'skip', NULL, NULL, 0, '主体词、关键词、负面词和敏感词规则，非外部AI', 0),
    (392010, 'xie_writer_legacy', '写作宝历史能力', 'legacy', 'xie_writer', NULL, 0, 'skip', 180000, NULL, 0, 'P2 历史能力，默认停用', 0),
    (392011, 'ocr_legacy', 'OCR历史能力', 'legacy', 'legacy_nlp', NULL, 0, 'skip', 30000, NULL, 0, 'P2 历史能力，默认停用', 0),
    (392012, 'nlp_lac_legacy', '分词历史能力', 'legacy', 'legacy_nlp', NULL, 0, 'skip', 30000, NULL, 0, 'P2 历史能力，当前词云不依赖旧分词', 0),
    (392013, 'dashscope_legacy', 'DashScope历史备用', 'legacy', 'dashscope', 'qwen-plus', 0, 'skip', 180000, NULL, 0, 'P2 历史备用模型，默认停用', 0)
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
    (393001, 'report_generate', 'AI报告生成默认模板', 'v1',
     '你是一个专业的舆情分析师，擅长撰写舆情报告。请严格按照用户要求输出 Markdown 报告。',
     '请根据以下舆情数据生成报告。报告类型：${reportType}。报告标题：${reportTitle}。统计周期：${periodStart} 至 ${periodEnd}。数据：${dataJson}',
     'Markdown，不要输出无关解释。', 1, '报告生成默认提示词，代码仍保留报告类型细分约束', 0),
    (393002, 'minority_judgment', '蒙维语AI研判默认模板', 'v1',
     '你是一个少数民族语言舆情分析专家。请分析内容并返回 JSON。',
     '标题：${title}\n内容：${content}\n请翻译为中文，判断情感、主题、风险等级、风险理由和摘要。',
     '{"translatedTitle":"...","translatedContent":"...","sentiment":"positive|negative|neutral","topic":"...","riskLevel":"urgent|major|concern|normal","riskReason":"...","summary":"..."}', 1, '少数民族语言自动研判默认提示词', 0),
    (393003, 'word_cloud_extract', '词云热词提取默认模板', 'v1',
     '你是舆情热词提取助手。请只提取能代表话题的短词，过滤虚词、泛词、平台词和无意义词。',
     '请从以下校园舆情文本中提取最多 30 个中文热词，并给出权重。文本：${text}',
     '[{"name":"热词","value":12}]', 1, '首页词云 DeepSeek 提取提示词', 0),
    (393004, 'keyword_extract', '关键词提取默认模板', 'v1',
     '你是舆情关键词提取助手。请输出结构化关键词。',
     '请从以下文本提取 5 到 10 个关键词。文本：${text}',
     '["关键词1","关键词2"]', 1, '单条内容关键词提取提示词', 0)
ON DUPLICATE KEY UPDATE
    `template_name` = VALUES(`template_name`),
    `system_prompt` = VALUES(`system_prompt`),
    `user_prompt` = VALUES(`user_prompt`),
    `output_format` = VALUES(`output_format`),
    `enabled` = VALUES(`enabled`),
    `remark` = VALUES(`remark`),
    `deleted` = 0,
    `update_time` = CURRENT_TIMESTAMP;

INSERT INTO `campus_permission_menu` (
    `menu_id`, `parent_id`, `menu_code`, `menu_name`, `menu_type`,
    `route_path`, `permission_code`, `icon`, `sort_no`, `visible`, `status`, `deleted`
) VALUES
    (180116, 0, 'ai_capability_admin', 'AI能力管理', 'menu', '/admin/settings/ai', 'campus:ai:view', 'BrainCircuit', 112, 1, 1, 0)
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
    (181532, 'campus:ai:read', 'AI能力只读', 'AI能力管理', 'GET', '/campus/ai/**', 1, 'AI供应商、模型、功能绑定、提示词和调用日志查询', 0),
    (181533, 'campus:ai:operate', 'AI能力维护', 'AI能力管理', 'POST', '/campus/ai/**', 1, 'AI供应商、模型、功能绑定、提示词和测试连接维护', 0)
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
    (211180116, 180001, 180116)
ON DUPLICATE KEY UPDATE
    `deleted` = 0,
    `update_time` = CURRENT_TIMESTAMP;

INSERT INTO `campus_role_api` (`relation_id`, `role_id`, `api_id`)
VALUES
    (211181532, 180001, 181532),
    (211181533, 180001, 181533)
ON DUPLICATE KEY UPDATE
    `deleted` = 0,
    `update_time` = CURRENT_TIMESTAMP;
