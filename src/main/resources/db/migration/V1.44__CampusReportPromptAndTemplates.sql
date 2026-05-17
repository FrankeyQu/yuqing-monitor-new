-- Campus report prompt fields, event-scoped auto reports and higher-education scenario templates.

ALTER TABLE `campus_report`
    ADD COLUMN `ai_user_prompt` mediumtext COMMENT '用户补充AI生成要求' AFTER `ai_model`;

ALTER TABLE `campus_report_job`
    ADD COLUMN `event_id` bigint DEFAULT NULL COMMENT '关联事件ID' AFTER `template_id`,
    ADD COLUMN `ai_user_prompt` mediumtext COMMENT '自动报告AI生成要求' AFTER `event_id`,
    ADD KEY `idx_campus_report_job_event` (`event_id`, `deleted`);

INSERT INTO `campus_permission_menu` (
    `menu_id`, `parent_id`, `menu_code`, `menu_name`, `menu_type`,
    `route_path`, `permission_code`, `icon`, `sort_no`, `visible`, `status`, `deleted`
) VALUES
    (180117, 0, 'report_templates', '报告模板', 'menu', '/report-templates', 'campus:report:view', 'FileText', 95, 1, 1, 0)
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

INSERT INTO `campus_role_menu` (`relation_id`, `role_id`, `menu_id`)
VALUES
    (211180117, 180001, 180117),
    (212180117, 180002, 180117),
    (213180117, 180003, 180117)
ON DUPLICATE KEY UPDATE
    `deleted` = 0,
    `update_time` = CURRENT_TIMESTAMP;

INSERT INTO `campus_report_template`
    (`template_id`, `template_name`, `report_type`, `template_content`, `status`, `remark`, `deleted`, `create_user_id`, `update_user_id`)
VALUES
    (410000000000000101, '高校舆情日报（综合态势）', 'daily',
     '# ${reportTitle}\n\n## 一、今日综合态势\n${overview}\n\n| 指标 | 数值 |\n|------|------|\n| 监测文章总数 | ${totalCount} |\n| 负面文章数 | ${negativeCount} |\n| 中性文章数 | ${neutralCount} |\n| 正面文章数 | ${positiveCount} |\n\n## 二、风险提醒\n${sentimentTable}\n\n${keywordTable}\n\n## 三、传播走势\n${trendTable}\n\n## 四、平台分布\n${mediaTable}\n\n${platformRanking}\n\n## 五、热点内容\n${hotArticles}\n\n## 六、处置建议\n${governanceTable}\n',
     1, '面向高校宣传、网信和学工部门的每日综合舆情简报。', 0, 0, 0),
    (410000000000000102, '高校舆情周报（趋势对比）', 'weekly',
     '# ${reportTitle}\n\n## 一、本周舆情概览\n${overview}\n\n## 二、趋势变化\n${trendTable}\n\n## 三、主题与热词演变\n${keywordTable}\n\n## 四、媒体与平台关注\n${mediaTable}\n\n${platformRanking}\n\n## 五、重点内容回顾\n${hotArticles}\n\n## 六、风险研判与下周关注\n${sentimentTable}\n\n${governanceTable}\n',
     1, '用于周度汇报，强调趋势变化、热点复盘和下周关注。', 0, 0, 0),
    (410000000000000103, '高校舆情月报（风险复盘）', 'monthly',
     '# ${reportTitle}\n\n## 一、月度总体态势\n${overview}\n\n## 二、月度传播趋势\n${trendTable}\n\n## 三、风险结构与情感分布\n${sentimentTable}\n\n## 四、平台与渠道分布\n${mediaTable}\n\n${platformRanking}\n\n## 五、核心议题与热点内容\n${keywordTable}\n\n${hotArticles}\n\n## 六、治理复盘\n${governanceTable}\n\n## 七、下月关注方向\n请结合本月高频议题、负面线索和处置闭环情况提出下月关注建议。\n',
     1, '用于月度复盘，强调治理成效和后续关注方向。', 0, 0, 0),
    (410000000000000104, '重大突发事件专报', 'special',
     '# ${reportTitle}\n\n## 一、事件概况\n- 事件：${eventTitle}\n- 当前状态：${eventStatus}\n- 风险等级：${riskLevel}\n\n${eventSummary}\n\n## 二、传播扩散情况\n${trendTable}\n\n${mediaTable}\n\n## 三、舆论焦点\n${keywordTable}\n\n${hotArticles}\n\n## 四、风险研判\n${sentimentTable}\n\n## 五、处置口径与建议\n${governanceTable}\n',
     1, '用于突发事件、重大负面或校内敏感议题专报。', 0, 0, 0),
    (410000000000000105, '招生就业专题报告', 'special',
     '# ${reportTitle}\n\n## 一、专题概况\n${overview}\n\n## 二、招生就业相关传播趋势\n${trendTable}\n\n## 三、关注议题与关键词\n${keywordTable}\n\n## 四、平台分布与重点内容\n${mediaTable}\n\n${hotArticles}\n\n## 五、声誉风险与回应建议\n${sentimentTable}\n\n${governanceTable}\n',
     1, '面向招生、就业、培养质量和院校声誉相关舆情。', 0, 0, 0),
    (410000000000000106, '后勤服务专题报告', 'special',
     '# ${reportTitle}\n\n## 一、后勤服务舆情概况\n${overview}\n\n## 二、食堂、宿舍与校园服务走势\n${trendTable}\n\n## 三、投诉建议与高频问题\n${keywordTable}\n\n## 四、平台反馈与典型内容\n${mediaTable}\n\n${hotArticles}\n\n## 五、服务改进与处置跟踪\n${sentimentTable}\n\n${governanceTable}\n',
     1, '面向食堂、宿舍、校园服务、投诉建议等后勤服务场景。', 0, 0, 0),
    (410000000000000107, '学生安全与心理风险关注报告', 'special',
     '# ${reportTitle}\n\n## 一、安全与心理风险概览\n${overview}\n\n## 二、相关线索趋势\n${trendTable}\n\n## 三、风险话题与关键词\n${keywordTable}\n\n## 四、重点内容与传播渠道\n${mediaTable}\n\n${hotArticles}\n\n## 五、风险等级与处置建议\n${sentimentTable}\n\n${governanceTable}\n',
     1, '用于学生安全、异常情绪、心理风险和协同处置场景。', 0, 0, 0)
ON DUPLICATE KEY UPDATE
    `template_name` = VALUES(`template_name`),
    `report_type` = VALUES(`report_type`),
    `template_content` = VALUES(`template_content`),
    `status` = VALUES(`status`),
    `remark` = VALUES(`remark`),
    `deleted` = 0,
    `update_user_id` = VALUES(`update_user_id`),
    `update_time` = CURRENT_TIMESTAMP;
