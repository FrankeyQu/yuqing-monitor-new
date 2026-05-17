-- Batch 41: blueprint completion fields for relevance, topic, evidence, and report templates.

ALTER TABLE `campus_monitor_result`
    ADD COLUMN `school_relevance_score` int DEFAULT 0 COMMENT '学校相关性评分(0-100)' AFTER `risk_score`,
    ADD COLUMN `school_relevance_reason` varchar(1024) DEFAULT NULL COMMENT '学校相关性判断依据' AFTER `school_relevance_score`,
    ADD COLUMN `matched_school_terms` varchar(512) DEFAULT NULL COMMENT '命中的学校主体/校园语境词' AFTER `school_relevance_reason`,
    ADD COLUMN `excluded_reason` varchar(512) DEFAULT NULL COMMENT '排除原因' AFTER `matched_school_terms`,
    ADD COLUMN `topic_category` varchar(64) DEFAULT NULL COMMENT '校园事件主题分类' AFTER `excluded_reason`,
    ADD COLUMN `topic_sub_category` varchar(64) DEFAULT NULL COMMENT '校园事件主题子类/标签' AFTER `topic_category`,
    ADD COLUMN `topic_reason` varchar(1024) DEFAULT NULL COMMENT '主题分类依据' AFTER `topic_sub_category`;

ALTER TABLE `campus_clue`
    ADD COLUMN `school_relevance_score` int DEFAULT 0 COMMENT '学校相关性评分(0-100)' AFTER `risk_level`,
    ADD COLUMN `school_relevance_reason` varchar(1024) DEFAULT NULL COMMENT '学校相关性判断依据' AFTER `school_relevance_score`,
    ADD COLUMN `matched_school_terms` varchar(512) DEFAULT NULL COMMENT '命中的学校主体/校园语境词' AFTER `school_relevance_reason`,
    ADD COLUMN `excluded_reason` varchar(512) DEFAULT NULL COMMENT '排除原因' AFTER `matched_school_terms`,
    ADD COLUMN `topic_category` varchar(64) DEFAULT NULL COMMENT '校园事件主题分类' AFTER `excluded_reason`,
    ADD COLUMN `topic_sub_category` varchar(64) DEFAULT NULL COMMENT '校园事件主题子类/标签' AFTER `topic_category`,
    ADD COLUMN `topic_reason` varchar(1024) DEFAULT NULL COMMENT '主题分类依据' AFTER `topic_sub_category`;

ALTER TABLE `campus_alert`
    ADD COLUMN `evidence_json` text COMMENT '结构化预警依据JSON' AFTER `matched_keywords`;

INSERT INTO `campus_report_template`
    (`template_id`, `template_name`, `report_type`, `template_content`, `status`, `deleted`, `create_user_id`, `update_user_id`)
VALUES
    (410000000000000001, '校园舆情日报模板', 'daily',
     '# ${reportTitle}\n\n## 一、概况\n${overview}\n\n| 指标 | 数值 |\n|------|------|\n| 监测文章总数 | ${totalCount} |\n| 负面文章数 | ${negativeCount} |\n| 中性文章数 | ${neutralCount} |\n| 正面文章数 | ${positiveCount} |\n\n## 二、走势\n${trendTable}\n\n## 三、媒体与平台\n${mediaTable}\n\n${platformRanking}\n\n## 四、情感与热词\n${sentimentTable}\n\n${keywordTable}\n\n## 五、热点文章\n${hotArticles}\n\n## 六、处置与复盘\n${governanceTable}\n',
     1, 0, 0, 0),
    (410000000000000002, '校园事件复盘模板', 'event_review',
     '# ${reportTitle}\n\n## 一、事件概况\n- 事件：${eventTitle}\n- 风险等级：${riskLevel}\n- 当前状态：${eventStatus}\n\n${eventSummary}\n\n## 二、传播走势\n${trendTable}\n\n## 三、来源分布\n${mediaTable}\n\n## 四、情感与关键词\n${sentimentTable}\n\n${keywordTable}\n\n## 五、相关内容\n${hotArticles}\n\n## 六、处置复盘\n${governanceTable}\n',
     1, 0, 0, 0)
ON DUPLICATE KEY UPDATE
    `template_name` = VALUES(`template_name`),
    `report_type` = VALUES(`report_type`),
    `template_content` = VALUES(`template_content`),
    `status` = VALUES(`status`),
    `deleted` = 0,
    `update_user_id` = VALUES(`update_user_id`),
    `update_time` = CURRENT_TIMESTAMP;
