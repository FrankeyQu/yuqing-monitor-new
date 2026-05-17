SET NAMES utf8mb4;
USE stonedt_portal;

-- 校园演示数据：只使用模拟场景、模拟账号、公开/授权来源说明。

INSERT INTO campus_department (
    department_id, parent_id, department_name, department_code, department_type,
    leader_user_id, contact_phone, sort_no, status, deleted, create_user_id, update_user_id
) VALUES
    (200101, 0, '网信办', 'WXB', 'office', NULL, '0991-0000001', 10, 1, 0, 1, 1),
    (200102, 0, '宣传部', 'XCB', 'office', NULL, '0991-0000002', 20, 1, 0, 1, 1),
    (200103, 0, '学工部', 'XGB', 'office', NULL, '0991-0000003', 30, 1, 0, 1, 1),
    (200104, 0, '保卫处', 'BWC', 'office', NULL, '0991-0000004', 40, 1, 0, 1, 1),
    (200105, 0, '后勤管理处', 'HQGLC', 'office', NULL, '0991-0000005', 50, 1, 0, 1, 1)
ON DUPLICATE KEY UPDATE
    department_name = VALUES(department_name),
    department_code = VALUES(department_code),
    department_type = VALUES(department_type),
    contact_phone = VALUES(contact_phone),
    sort_no = VALUES(sort_no),
    status = VALUES(status),
    deleted = 0,
    update_time = CURRENT_TIMESTAMP;

INSERT INTO campus_ingest_source (
    source_id, source_name, source_type, platform, access_endpoint,
    authorization_basis, authorization_scope, responsible_department_id,
    enabled, remark, deleted, create_user_id, update_user_id
) VALUES (
    200201, '校园公开网络信息演示源', 'manual', '公开网页',
    '演示数据由校内授权人员人工录入',
    '网信办演示任务 DEMO-2026-001',
    '仅限公开网页、上级移交和学校业务中依法取得的信息；不包含私信、密码、通讯录和非公开个人信息',
    200101, 1, 'Batch20 演示来源', 0, 1, 1
) ON DUPLICATE KEY UPDATE
    source_name = VALUES(source_name),
    authorization_basis = VALUES(authorization_basis),
    authorization_scope = VALUES(authorization_scope),
    enabled = 1,
    deleted = 0,
    update_time = CURRENT_TIMESTAMP;

INSERT INTO campus_ingest_task (
    task_id, source_id, task_name, target_type, adapter_type, schedule_cron,
    fetch_config, task_status, authorization_scope, retention_days,
    deleted, create_user_id, update_user_id
) VALUES (
    200202, 200201, '校园公开信息人工推送演示任务', 'clue', 'manual_push', NULL,
    '{"demo":true,"scope":"public_or_authorized"}', 'active',
    '仅处理公开、授权、上级移交和学校业务数据', 180,
    0, 1, 1
) ON DUPLICATE KEY UPDATE
    task_name = VALUES(task_name),
    task_status = VALUES(task_status),
    authorization_scope = VALUES(authorization_scope),
    deleted = 0,
    update_time = CURRENT_TIMESTAMP;

INSERT INTO campus_ingest_record (
    record_id, source_id, task_id, external_id, platform, content_type,
    title, content, original_url, publish_time, author_name, keywords,
    risk_level, sentiment, raw_data, normalized_status, target_type, target_id,
    deleted, create_user_id, update_user_id
) VALUES (
    200203, 200201, 200202, 'DEMO-INGEST-001', '公开网页', 'post',
    '网传某校食堂食品安全问题引发学生集中讨论',
    '公开网页出现关于学校食堂食品安全的讨论，部分内容含有未经证实表述，建议网信办会同后勤部门核验并回应。',
    'https://example.edu/demo/public-opinion/food-safety', NOW(), '公开网页模拟账号',
    '食堂,食品安全,学生讨论', 'major', 'negative',
    '{"demo":true,"source":"public_web"}', 'pending', NULL, NULL,
    0, 1, 1
) ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    content = VALUES(content),
    keywords = VALUES(keywords),
    risk_level = VALUES(risk_level),
    normalized_status = VALUES(normalized_status),
    deleted = 0,
    update_time = CURRENT_TIMESTAMP;

INSERT INTO campus_detection_topic (
    topic_id, topic_name, topic_category, keywords, exclude_words,
    platform_scope, source_scope, risk_level, responsible_department_id,
    enabled, description, deleted, create_user_id, update_user_id
) VALUES (
    200301, '校园食品安全', '校园安全', '食堂,食品安全,后勤,投诉', '招聘,广告',
    '公开网页,微博,论坛', '公开/授权/上级移交', 'major', 200105,
    1, '演示主题：发现食品安全相关公开舆情', 0, 1, 1
) ON DUPLICATE KEY UPDATE
    topic_name = VALUES(topic_name),
    keywords = VALUES(keywords),
    enabled = 1,
    deleted = 0,
    update_time = CURRENT_TIMESTAMP;

INSERT INTO campus_detection_rule (
    rule_id, topic_id, rule_name, rule_type, rule_condition,
    exclude_words, risk_level, enabled, sort_no, description,
    deleted, create_user_id, update_user_id
) VALUES (
    200302, 200301, '食品安全关键词命中', 'keyword_any',
    '食堂,食品安全,学生讨论', '招聘,广告', 'major', 1, 10,
    '任一关键词命中即进入预警复核', 0, 1, 1
) ON DUPLICATE KEY UPDATE
    rule_name = VALUES(rule_name),
    rule_condition = VALUES(rule_condition),
    enabled = 1,
    deleted = 0,
    update_time = CURRENT_TIMESTAMP;

INSERT INTO campus_detection_task (
    detection_task_id, topic_id, task_name, object_types, task_status,
    scan_window_hours, auto_alert, last_run_time, description,
    deleted, create_user_id, update_user_id
) VALUES (
    200303, 200301, '食品安全公开舆情检测任务', 'ingest_record,clue,account_content',
    'active', 168, 1, NOW(), 'Batch20 演示检测任务', 0, 1, 1
) ON DUPLICATE KEY UPDATE
    task_name = VALUES(task_name),
    task_status = VALUES(task_status),
    auto_alert = VALUES(auto_alert),
    last_run_time = NOW(),
    deleted = 0,
    update_time = CURRENT_TIMESTAMP;

INSERT INTO campus_detection_run_log (
    run_log_id, detection_task_id, run_status, start_time, end_time,
    scanned_count, hit_count, alert_count, error_message, create_user_id
) VALUES (
    200304, 200303, 'success', DATE_SUB(NOW(), INTERVAL 2 MINUTE), NOW(),
    1, 1, 1, NULL, 1
) ON DUPLICATE KEY UPDATE
    run_status = VALUES(run_status),
    start_time = VALUES(start_time),
    end_time = VALUES(end_time),
    scanned_count = VALUES(scanned_count),
    hit_count = VALUES(hit_count),
    alert_count = VALUES(alert_count);

INSERT INTO campus_monitor_ingest_task_relation (
    relation_id, monitor_task_id, ingest_task_id, deleted, create_user_id, update_user_id
) VALUES (
    201001200202, 201001, 200202, 0, 1, 1
) ON DUPLICATE KEY UPDATE
    deleted = 0,
    update_user_id = VALUES(update_user_id),
    update_time = CURRENT_TIMESTAMP;

INSERT INTO campus_alert (
    alert_id, alert_title, alert_content, alert_source, source_object_id,
    rule_id, risk_level, matched_keywords, alert_status, handle_opinion,
    deleted, create_user_id, update_user_id
) VALUES (
    200401, '检测命中：校园食品安全公开舆情', '公开网页出现食堂食品安全相关讨论，建议核验事实并组织回应。',
    'detection', 200203, 200302, 'major', '食堂,食品安全,学生讨论',
    'pending', NULL, 0, 1, 1
) ON DUPLICATE KEY UPDATE
    alert_title = VALUES(alert_title),
    alert_status = VALUES(alert_status),
    matched_keywords = VALUES(matched_keywords),
    deleted = 0,
    update_time = CURRENT_TIMESTAMP;

INSERT INTO campus_detection_hit (
    hit_id, detection_task_id, topic_id, rule_id, object_type, object_id,
    object_title, platform, matched_keywords, risk_level, hit_content,
    hit_status, alert_id, clue_id, deleted, create_user_id, update_user_id
) VALUES (
    200305, 200303, 200301, 200302, 'ingest_record', 200203,
    '网传某校食堂食品安全问题引发学生集中讨论', '公开网页',
    '食堂,食品安全,学生讨论', 'major',
    '公开网页出现关于学校食堂食品安全的讨论，建议后勤部门核验。',
    'alerted', 200401, 200501, 0, 1, 1
) ON DUPLICATE KEY UPDATE
    object_title = VALUES(object_title),
    matched_keywords = VALUES(matched_keywords),
    hit_status = VALUES(hit_status),
    alert_id = VALUES(alert_id),
    clue_id = VALUES(clue_id),
    deleted = 0,
    update_time = CURRENT_TIMESTAMP;

INSERT INTO campus_clue (
    clue_id, clue_title, clue_content, clue_source, source_platform,
    original_url, publish_time, discover_time, involved_department_id,
    involved_account, keywords, risk_level, sentiment, clue_status,
    judge_opinion, event_id, duplicate_key, remark, deleted, create_user_id, update_user_id
) VALUES (
    200501, '校园食品安全相关公开舆情线索', '由检测命中转入线索库，内容涉及食堂食品安全讨论，需后勤管理处核验。',
    'detection', '公开网页', 'https://example.edu/demo/public-opinion/food-safety',
    NOW(), NOW(), 200105, '公开网页模拟账号', '食堂,食品安全,学生讨论',
    'major', 'negative', 'converted', '已核实为需跟进线索，转事件处置。', 200601,
    'DEMO-FOOD-SAFETY-001', 'Batch20 演示线索', 0, 1, 1
) ON DUPLICATE KEY UPDATE
    clue_title = VALUES(clue_title),
    clue_status = VALUES(clue_status),
    event_id = VALUES(event_id),
    deleted = 0,
    update_time = CURRENT_TIMESTAMP;

INSERT INTO campus_event (
    event_id, event_title, event_type, event_summary, first_publish_time,
    discover_time, risk_level, impact_scope, involved_department_id,
    current_heat, event_status, disposal_requirement, archive_conclusion,
    deleted, create_user_id, update_user_id
) VALUES (
    200601, '校园食品安全公开舆情处置事件', '校园安全',
    '围绕食堂食品安全的公开讨论热度上升，需要核验、回应和处置反馈。',
    DATE_SUB(NOW(), INTERVAL 1 HOUR), NOW(), 'major', '学生群体、后勤服务',
    200105, 78, 'handling', '后勤管理处核验情况，宣传部准备公开回应口径。',
    NULL, 0, 1, 1
) ON DUPLICATE KEY UPDATE
    event_title = VALUES(event_title),
    event_status = VALUES(event_status),
    current_heat = VALUES(current_heat),
    deleted = 0,
    update_time = CURRENT_TIMESTAMP;

INSERT IGNORE INTO campus_event_clue (
    relation_id, event_id, clue_id, deleted, create_user_id
) VALUES (
    200602, 200601, 200501, 0, 1
);

INSERT INTO campus_disposal_task (
    disposal_task_id, event_id, task_title, assigned_department_id,
    assigned_user_id, disposal_requirement, due_time, task_status,
    feedback_summary, deleted, create_user_id, update_user_id
) VALUES (
    200603, 200601, '核验食堂食品安全情况并反馈', 200105,
    NULL, '后勤管理处核验窗口、供应链、留样和现场管理情况，形成反馈。',
    DATE_ADD(NOW(), INTERVAL 1 DAY), 'processing',
    '已安排现场核验，初步未发现系统性食品安全问题。', 0, 1, 1
) ON DUPLICATE KEY UPDATE
    task_title = VALUES(task_title),
    task_status = VALUES(task_status),
    feedback_summary = VALUES(feedback_summary),
    deleted = 0,
    update_time = CURRENT_TIMESTAMP;

INSERT INTO campus_disposal_record (
    record_id, disposal_task_id, event_id, record_type, record_content,
    handler_user_id, handler_name, handle_time, attachment_desc
) VALUES (
    200604, 200603, 200601, 'feedback',
    '后勤管理处已完成现场核验，建议发布事实说明并继续关注后续讨论。',
    1, '演示处置员', NOW(), '现场核验记录演示附件'
) ON DUPLICATE KEY UPDATE
    record_content = VALUES(record_content),
    handle_time = VALUES(handle_time),
    attachment_desc = VALUES(attachment_desc);

INSERT INTO campus_analysis_task (
    analysis_task_id, object_type, object_id, analysis_type, task_status,
    request_payload, model_provider, model_name, error_message,
    deleted, create_user_id, update_user_id
) VALUES (
    200701, 'event', 200601, 'comprehensive', 'completed',
    '{"demo":true}', 'local_heuristic', 'local_heuristic_v1', NULL,
    0, 1, 1
) ON DUPLICATE KEY UPDATE
    task_status = VALUES(task_status),
    deleted = 0,
    update_time = CURRENT_TIMESTAMP;

INSERT INTO campus_analysis_result (
    analysis_result_id, analysis_task_id, object_type, object_id, analysis_type,
    sentiment, suggested_risk_level, summary, keywords, similar_object_ids,
    confidence, result_payload, assistive_label, adoption_status,
    reviewer_user_id, review_time, review_opinion, deleted, create_user_id, update_user_id
) VALUES (
    200702, 200701, 'event', 200601, 'comprehensive',
    'negative', 'major', '该事件涉及学生关注度较高的后勤服务议题，建议事实核验后统一回应。',
    '食堂,食品安全,后勤回应', NULL, 0.86,
    '{"demo":true,"advice":"人工复核后使用"}', '仅供辅助研判',
    'adopted', 1, NOW(), '采纳为报告参考，不作为自动结论。', 0, 1, 1
) ON DUPLICATE KEY UPDATE
    summary = VALUES(summary),
    adoption_status = VALUES(adoption_status),
    review_opinion = VALUES(review_opinion),
    deleted = 0,
    update_time = CURRENT_TIMESTAMP;

INSERT INTO campus_report_template (
    template_id, template_name, report_type, template_content,
    status, remark, deleted, create_user_id, update_user_id
) VALUES (
    200801, '校园舆情事件专报模板', 'event',
    '# ${reportTitle}\n\n## 一、事件概况\n${reportSummary}\n\n## 二、统计周期\n${periodStart} 至 ${periodEnd}\n\n## 三、事件状态\n- 事件标题：${eventTitle}\n- 当前状态：${eventStatus}\n\n## 四、处置建议\n请结合人工研判、部门反馈和事实核验形成最终意见。',
    1, 'Batch20 演示模板', 0, 1, 1
) ON DUPLICATE KEY UPDATE
    template_name = VALUES(template_name),
    template_content = VALUES(template_content),
    status = 1,
    deleted = 0,
    update_time = CURRENT_TIMESTAMP;

INSERT INTO campus_report (
    report_id, report_title, report_type, report_status, template_id,
    event_id, period_start_time, period_end_time, report_summary,
    report_content, report_format, file_name, generated_by, generate_time,
    archive_user_id, archive_time, archive_opinion, deleted, create_user_id, update_user_id
) VALUES (
    200802, '校园食品安全公开舆情事件专报', 'event', 'generated', 200801,
    200601, DATE_SUB(NOW(), INTERVAL 1 DAY), NOW(),
    '围绕食堂食品安全的公开讨论已进入处置流程，建议持续跟踪并发布事实说明。',
    '# 校园食品安全公开舆情事件专报\n\n## 一、事件概况\n公开网页出现食堂食品安全相关讨论，已转入事件处置。\n\n## 二、处置进展\n后勤管理处已开展现场核验，宣传部门准备回应口径。\n\n## 三、建议\n继续关注后续传播，所有结论以人工复核和事实核验为准。',
    'markdown', '校园食品安全公开舆情事件专报.md', 1, NOW(),
    NULL, NULL, NULL, 0, 1, 1
) ON DUPLICATE KEY UPDATE
    report_title = VALUES(report_title),
    report_status = VALUES(report_status),
    report_summary = VALUES(report_summary),
    report_content = VALUES(report_content),
    file_name = VALUES(file_name),
    generate_time = NOW(),
    deleted = 0,
    update_time = CURRENT_TIMESTAMP;

INSERT IGNORE INTO campus_report_event (
    relation_id, report_id, event_id, deleted, create_user_id
) VALUES (
    200803, 200802, 200601, 0, 1
);

INSERT INTO campus_report_job (
    report_job_id, job_name, report_type, template_id, period_rule,
    schedule_cron, output_format, job_status, last_run_time, next_run_time,
    reviewer_user_id, description, deleted, create_user_id, update_user_id
) VALUES (
    200804, '校园舆情日报自动生成任务', 'daily', 200801, 'daily',
    '0 0 8 * * ?', 'markdown', 'active', NOW(), DATE_ADD(NOW(), INTERVAL 1 DAY),
    1, 'Batch20 演示自动报告任务', 0, 1, 1
) ON DUPLICATE KEY UPDATE
    job_name = VALUES(job_name),
    job_status = VALUES(job_status),
    last_run_time = VALUES(last_run_time),
    next_run_time = VALUES(next_run_time),
    deleted = 0,
    update_time = CURRENT_TIMESTAMP;

INSERT INTO campus_report_generation_log (
    generation_log_id, report_job_id, report_id, run_status,
    start_time, end_time, error_message, create_user_id
) VALUES (
    200805, 200804, 200802, 'success',
    DATE_SUB(NOW(), INTERVAL 1 MINUTE), NOW(), NULL, 1
) ON DUPLICATE KEY UPDATE
    report_id = VALUES(report_id),
    run_status = VALUES(run_status),
    start_time = VALUES(start_time),
    end_time = VALUES(end_time),
    error_message = NULL;
