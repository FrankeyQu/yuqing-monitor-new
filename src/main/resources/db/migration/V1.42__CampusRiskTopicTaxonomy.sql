-- Batch 42: align campus risk level labels and seed event topic taxonomy.

INSERT INTO `campus_dict_type` (`dict_type`, `dict_name`, `description`, `sort_no`, `status`, `deleted`)
VALUES
    ('risk_level', '风险等级', '校园舆情风险等级，统一编码 normal/concern/major/urgent', 30, 1, 0),
    ('campus_event_topic', '校园事件主题', '线索、监测命中和事件归集使用的校园舆情主题分类', 350, 1, 0)
ON DUPLICATE KEY UPDATE
    `dict_name` = VALUES(`dict_name`),
    `description` = VALUES(`description`),
    `sort_no` = VALUES(`sort_no`),
    `status` = VALUES(`status`),
    `deleted` = 0,
    `update_time` = CURRENT_TIMESTAMP;

INSERT INTO `campus_dict_item` (`dict_type`, `item_code`, `item_name`, `item_value`, `description`, `sort_no`, `status`, `deleted`)
VALUES
    ('risk_level', 'normal', '普通关注', 'normal', '默认风险等级；进入日常关注和复核', 10, 1, 0),
    ('risk_level', 'concern', '一般预警', 'concern', '需要业务人员研判和跟踪', 20, 1, 0),
    ('risk_level', 'major', '重大预警', 'major', '需要部门协同处置和事件化跟踪', 30, 1, 0),
    ('risk_level', 'urgent', '特别重大', 'urgent', '需要快速响应、升级通报和领导关注', 40, 1, 0),
    ('campus_event_topic', 'food_safety', '食品安全', 'food_safety', '食堂、供餐、饮水、食品卫生等', 10, 1, 0),
    ('campus_event_topic', 'dormitory', '宿舍管理', 'dormitory', '宿舍条件、门禁、水电、维修和住宿矛盾', 20, 1, 0),
    ('campus_event_topic', 'campus_safety', '校园安全', 'campus_safety', '治安、消防、交通、设施和突发安全事件', 30, 1, 0),
    ('campus_event_topic', 'bullying_conflict', '欺凌冲突', 'bullying_conflict', '校园欺凌、学生冲突、肢体或网络冲突', 40, 1, 0),
    ('campus_event_topic', 'teacher_ethics', '师德师风', 'teacher_ethics', '教师行为、教学纪律、师生关系争议', 50, 1, 0),
    ('campus_event_topic', 'fee_dispute', '收费争议', 'fee_dispute', '学费、住宿费、服务性收费、退费等争议', 60, 1, 0),
    ('campus_event_topic', 'admission_employment', '招生就业', 'admission_employment', '招生、录取、就业、实习和升学相关舆情', 70, 1, 0),
    ('campus_event_topic', 'exam_teaching', '考试教学', 'exam_teaching', '考试、作业、课程、教学质量和成绩争议', 80, 1, 0),
    ('campus_event_topic', 'logistics_service', '后勤服务', 'logistics_service', '校车、物业、维修、网络、医疗和生活服务', 90, 1, 0),
    ('campus_event_topic', 'public_incident', '公共事件', 'public_incident', '公共卫生、自然灾害、群体活动和校外关联事件', 100, 1, 0),
    ('campus_event_topic', 'rumor', '谣言不实信息', 'rumor', '未经证实、误传、恶意编造或断章取义内容', 110, 1, 0),
    ('campus_event_topic', 'other', '其他', 'other', '暂未归类的校园舆情主题', 999, 1, 0)
ON DUPLICATE KEY UPDATE
    `item_name` = VALUES(`item_name`),
    `item_value` = VALUES(`item_value`),
    `description` = VALUES(`description`),
    `sort_no` = VALUES(`sort_no`),
    `status` = VALUES(`status`),
    `deleted` = 0,
    `update_time` = CURRENT_TIMESTAMP;

UPDATE `campus_dict_item`
SET `status` = 0,
    `deleted` = 1,
    `description` = '历史风险等级编码，后续统一映射为 major',
    `update_time` = CURRENT_TIMESTAMP
WHERE `dict_type` = 'risk_level'
  AND `item_code` = 'higher';
