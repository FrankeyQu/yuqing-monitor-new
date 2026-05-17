-- P0-P2: monitor display, multilingual keywords, interaction metrics, watch targets,
-- lexicon dictionary seeds and education intelligence support.

ALTER TABLE `campus_monitor_task`
    ADD COLUMN `keywords_i18n` text COMMENT '多语言关键词JSON，如 {"zh":"...","mongolian":"...","uyghur":"..."}' AFTER `keywords`,
    ADD COLUMN `negative_words_i18n` text COMMENT '多语言负面词JSON' AFTER `negative_words`,
    ADD COLUMN `exclude_words_i18n` text COMMENT '多语言排除词JSON' AFTER `exclude_words`;

ALTER TABLE `campus_monitor_result`
    ADD COLUMN `language` varchar(16) DEFAULT NULL COMMENT '语言(zh/mongolian/uyghur)' AFTER `publish_time`,
    ADD COLUMN `clue_id` bigint DEFAULT NULL COMMENT '转入线索ID' AFTER `alert_id`,
    ADD COLUMN `like_count` bigint DEFAULT NULL COMMENT '点赞数' AFTER `clue_id`,
    ADD COLUMN `comment_count` bigint DEFAULT NULL COMMENT '评论数' AFTER `like_count`,
    ADD COLUMN `share_count` bigint DEFAULT NULL COMMENT '转发/分享数' AFTER `comment_count`,
    ADD COLUMN `collect_count` bigint DEFAULT NULL COMMENT '收藏数' AFTER `share_count`,
    ADD COLUMN `view_count` bigint DEFAULT NULL COMMENT '播放/浏览数' AFTER `collect_count`,
    ADD KEY `idx_campus_monitor_result_language` (`language`, `deleted`),
    ADD KEY `idx_campus_monitor_result_clue` (`clue_id`);

ALTER TABLE `campus_ingest_record`
    ADD COLUMN `like_count` bigint DEFAULT NULL COMMENT '点赞数' AFTER `language`,
    ADD COLUMN `comment_count` bigint DEFAULT NULL COMMENT '评论数' AFTER `like_count`,
    ADD COLUMN `share_count` bigint DEFAULT NULL COMMENT '转发/分享数' AFTER `comment_count`,
    ADD COLUMN `collect_count` bigint DEFAULT NULL COMMENT '收藏数' AFTER `share_count`,
    ADD COLUMN `view_count` bigint DEFAULT NULL COMMENT '播放/浏览数' AFTER `collect_count`;

ALTER TABLE `campus_account_content`
    ADD COLUMN `like_count` bigint DEFAULT NULL COMMENT '点赞数' AFTER `keywords`,
    ADD COLUMN `comment_count` bigint DEFAULT NULL COMMENT '评论数' AFTER `like_count`,
    ADD COLUMN `share_count` bigint DEFAULT NULL COMMENT '转发/分享数' AFTER `comment_count`,
    ADD COLUMN `collect_count` bigint DEFAULT NULL COMMENT '收藏数' AFTER `share_count`,
    ADD COLUMN `view_count` bigint DEFAULT NULL COMMENT '播放/浏览数' AFTER `collect_count`;

CREATE TABLE IF NOT EXISTS `campus_monitor_watch_target` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `target_id` bigint NOT NULL COMMENT '任务监控目标业务ID',
    `monitor_task_id` bigint NOT NULL COMMENT '监测任务业务ID',
    `target_type` varchar(32) NOT NULL COMMENT '目标类型(account|link)',
    `platform` varchar(64) DEFAULT NULL COMMENT '平台',
    `account_id` bigint DEFAULT NULL COMMENT '重点账号ID',
    `account_name` varchar(255) DEFAULT NULL COMMENT '账号名称',
    `account_uid` varchar(255) DEFAULT NULL COMMENT '平台账号ID',
    `link_url` varchar(1024) DEFAULT NULL COMMENT '指定链接或主页链接',
    `source_object_type` varchar(64) DEFAULT NULL COMMENT '来源对象类型(result|record|dpi|manual)',
    `source_object_id` bigint DEFAULT NULL COMMENT '来源对象ID',
    `authorization_scope` varchar(1024) DEFAULT NULL COMMENT '授权/来源范围说明',
    `keyword_scope` varchar(1024) DEFAULT NULL COMMENT '目标内补充关键词',
    `target_status` varchar(32) DEFAULT 'active' COMMENT '状态(active|paused)',
    `remark` varchar(1024) DEFAULT NULL COMMENT '备注',
    `deleted` tinyint DEFAULT 0 COMMENT '逻辑删除',
    `create_user_id` bigint DEFAULT NULL COMMENT '创建人',
    `update_user_id` bigint DEFAULT NULL COMMENT '更新人',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_campus_monitor_watch_target_id` (`target_id`),
    KEY `idx_campus_monitor_watch_task` (`monitor_task_id`, `target_status`, `deleted`),
    KEY `idx_campus_monitor_watch_account` (`account_id`, `deleted`),
    KEY `idx_campus_monitor_watch_platform_uid` (`platform`, `account_uid`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='监测任务重点账号和链接目标表';

CREATE TABLE IF NOT EXISTS `campus_school_subject` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `school_id` bigint NOT NULL COMMENT '学校业务ID',
    `school_name` varchar(255) NOT NULL COMMENT '学校名称',
    `school_aliases` text COMMENT '学校别名，逗号分隔',
    `region` varchar(128) DEFAULT NULL COMMENT '地区',
    `education_stage` varchar(64) DEFAULT NULL COMMENT '学段',
    `school_type` varchar(64) DEFAULT NULL COMMENT '学校类型',
    `status` tinyint DEFAULT 1 COMMENT '状态(1启用,0停用)',
    `remark` varchar(1024) DEFAULT NULL COMMENT '备注',
    `deleted` tinyint DEFAULT 0 COMMENT '逻辑删除',
    `create_user_id` bigint DEFAULT NULL COMMENT '创建人',
    `update_user_id` bigint DEFAULT NULL COMMENT '更新人',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_campus_school_subject_id` (`school_id`),
    KEY `idx_campus_school_subject_name` (`school_name`, `deleted`),
    KEY `idx_campus_school_subject_region` (`region`, `education_stage`, `status`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='教育专题学校主体表';

INSERT INTO `campus_dict_type` (`dict_type`, `dict_name`, `description`, `sort_no`, `status`, `deleted`)
VALUES
    ('campus_negative_word', '校园负面词库', '监测扫描和研判辅助使用的负面词', 340, 1, 0),
    ('campus_positive_word', '校园正面词库', '研判辅助使用的正面词', 341, 1, 0),
    ('campus_risk_word', '校园风险词库', '监测扫描和风险识别辅助词', 342, 1, 0),
    ('campus_education_news_word', '教育重点新闻词库', '本地区教育新闻专题检索词', 343, 1, 0),
    ('campus_policy_word', '教育重点政策词库', '教育政策专题检索词', 344, 1, 0),
    ('campus_admission_policy_word', '招生政策词库', '招生、报名、录取、学区等专题检索词', 345, 1, 0)
ON DUPLICATE KEY UPDATE
    `dict_name` = VALUES(`dict_name`),
    `description` = VALUES(`description`),
    `sort_no` = VALUES(`sort_no`),
    `status` = VALUES(`status`),
    `deleted` = 0,
    `update_time` = CURRENT_TIMESTAMP;

INSERT INTO `campus_dict_item` (`dict_type`, `item_code`, `item_name`, `item_value`, `description`, `sort_no`, `status`, `deleted`)
VALUES
    ('campus_negative_word', 'complaint', '投诉', '投诉', 'zh', 10, 1, 0),
    ('campus_negative_word', 'exposure', '曝光', '曝光', 'zh', 20, 1, 0),
    ('campus_negative_word', 'rights_protection', '维权', '维权', 'zh', 30, 1, 0),
    ('campus_negative_word', 'bullying', '校园欺凌', '校园欺凌', 'zh', 40, 1, 0),
    ('campus_negative_word', 'food_safety', '食品安全问题', '食品安全', 'zh', 50, 1, 0),
    ('campus_positive_word', 'commend', '表彰', '表彰', 'zh', 10, 1, 0),
    ('campus_positive_word', 'excellent', '优秀', '优秀', 'zh', 20, 1, 0),
    ('campus_risk_word', 'safety_incident', '安全事故', '安全事故', 'zh', 10, 1, 0),
    ('campus_risk_word', 'online_violence', '网暴', '网暴', 'zh', 20, 1, 0),
    ('campus_education_news_word', 'education', '教育', '教育', 'zh', 10, 1, 0),
    ('campus_education_news_word', 'school', '学校', '学校', 'zh', 20, 1, 0),
    ('campus_policy_word', 'policy', '政策', '政策', 'zh', 10, 1, 0),
    ('campus_policy_word', 'notice', '通知', '通知', 'zh', 20, 1, 0),
    ('campus_admission_policy_word', 'admission', '招生', '招生', 'zh', 10, 1, 0),
    ('campus_admission_policy_word', 'enrollment', '报名', '报名', 'zh', 20, 1, 0),
    ('campus_admission_policy_word', 'school_district', '学区', '学区', 'zh', 30, 1, 0),
    ('monitor_result_status', 'converted', '已转线索', 'converted', '监测命中已转入线索库', 50, 1, 0)
ON DUPLICATE KEY UPDATE
    `item_name` = VALUES(`item_name`),
    `item_value` = VALUES(`item_value`),
    `description` = VALUES(`description`),
    `sort_no` = VALUES(`sort_no`),
    `status` = VALUES(`status`),
    `deleted` = 0,
    `update_time` = CURRENT_TIMESTAMP;

INSERT INTO `campus_permission_menu` (
    `menu_id`, `parent_id`, `menu_code`, `menu_name`, `menu_type`,
    `route_path`, `permission_code`, `icon`, `sort_no`, `visible`, `status`, `deleted`
) VALUES
    (180114, 0, 'education_intel', '教育专题', 'menu', '/admin/education', 'campus:education:view', 'BookOpen', 19, 1, 1, 0)
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
    (181530, 'campus:education:read', '教育专题只读', '教育专题', 'GET', '/campus/education/**', 1, '教育专题和学校声量排名只读', 0),
    (181531, 'campus:education:operate', '教育专题操作', '教育专题', 'POST', '/campus/education/**', 1, '学校主体维护和百度接入任务创建', 0)
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
    (211180114, 180001, 180114),
    (212180114, 180002, 180114),
    (213180114, 180003, 180114)
ON DUPLICATE KEY UPDATE
    `deleted` = 0,
    `update_time` = CURRENT_TIMESTAMP;

INSERT INTO `campus_role_api` (`relation_id`, `role_id`, `api_id`)
VALUES
    (211181530, 180002, 181530),
    (211181531, 180002, 181531),
    (212181530, 180003, 181530)
ON DUPLICATE KEY UPDATE
    `deleted` = 0,
    `update_time` = CURRENT_TIMESTAMP;
