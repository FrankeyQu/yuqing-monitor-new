-- Batch 29: prelaunch governance defaults for school trial.
-- This migration does not delete historical data. It narrows default campus
-- roles, adds school initialization templates, and enriches dictionaries.

UPDATE `campus_permission_role`
SET `role_name` = '校园系统管理员',
    `role_type` = 'admin',
    `data_scope` = 'school',
    `status` = 1,
    `remark` = '试运行管理员；负责组织、字典、权限和接入配置',
    `deleted` = 0,
    `update_time` = CURRENT_TIMESTAMP
WHERE `role_id` = 180001;

UPDATE `campus_permission_role`
SET `role_name` = '舆情处置员',
    `role_type` = 'business',
    `data_scope` = 'school',
    `status` = 1,
    `remark` = '处理线索、预警、事件、检测、报告等业务，不包含系统权限配置',
    `deleted` = 0,
    `update_time` = CURRENT_TIMESTAMP
WHERE `role_id` = 180002;

UPDATE `campus_permission_role`
SET `role_name` = '校园查看员',
    `role_type` = 'viewer',
    `data_scope` = 'school',
    `status` = 1,
    `remark` = '查看工作台、态势大屏和报告，只读访问业务数据',
    `deleted` = 0,
    `update_time` = CURRENT_TIMESTAMP
WHERE `role_id` = 180003;

INSERT INTO `campus_permission_api` (
    `api_id`, `api_code`, `api_name`, `module_name`, `request_method`, `request_path`, `status`, `remark`, `deleted`
) VALUES
    (180501, 'campus:system:current-user', '当前用户信息', '系统权限', 'GET', '/campus/system/current-user', 1, '登录用户读取自身信息', 0),
    (180502, 'campus:api:all', '校园系统全部接口', '系统权限', 'ALL', '/campus/**', 1, '管理员权限', 0),
    (180503, 'campus:business:operate:legacy-disabled', '旧业务通配权限已停用', '校园业务', 'ALL', '/campus/legacy-disabled', 0, 'Batch29 停用旧 /campus/** 业务通配权限', 1),
    (180504, 'campus:system:menu-tree', '当前用户菜单', '系统权限', 'GET', '/campus/system/menu-tree', 1, '登录用户读取自身菜单', 0),
    (181501, 'campus:dashboard:read', '工作台只读', '工作台', 'GET', '/campus/dashboard/**', 1, '工作台和态势统计只读', 0),
    (181502, 'campus:clue:read', '线索只读', '线索库', 'GET', '/campus/clue/**', 1, '线索列表、详情和日志只读', 0),
    (181503, 'campus:clue:operate', '线索处置', '线索库', 'POST', '/campus/clue/**', 1, '线索新增、研判、归档和删除', 0),
    (181504, 'campus:account:read', '重点账号只读', '重点账号', 'GET', '/campus/account/**', 1, '重点账号和公开动态只读', 0),
    (181505, 'campus:account:operate', '重点账号维护', '重点账号', 'POST', '/campus/account/**', 1, '重点账号登记、审核、状态变更和公开动态维护', 0),
    (181506, 'campus:event:read', '事件只读', '事件处置', 'GET', '/campus/event/**', 1, '事件、任务和反馈记录只读', 0),
    (181507, 'campus:event:operate', '事件处置', '事件处置', 'POST', '/campus/event/**', 1, '事件定级、分派、反馈、复核和归档', 0),
    (181508, 'campus:alert:read', '预警只读', '预警中心', 'GET', '/campus/alert/**', 1, '预警、规则和敏感词只读', 0),
    (181509, 'campus:alert:operate', '预警处置', '预警中心', 'POST', '/campus/alert/**', 1, '预警处理、规则和敏感词维护', 0),
    (181510, 'campus:detection:read', '检测只读', '检测任务', 'GET', '/campus/detection/**', 1, '检测主题、规则、任务、命中和运行日志只读', 0),
    (181511, 'campus:detection:operate', '检测维护', '检测任务', 'POST', '/campus/detection/**', 1, '检测主题、规则、任务和命中处理', 0),
    (181512, 'campus:report:read', '报告只读', '报告归档', 'GET', '/campus/report/**', 1, '报告、模板和下载只读', 0),
    (181513, 'campus:report:operate', '报告维护', '报告归档', 'POST', '/campus/report/**', 1, '报告生成、归档、删除和模板维护', 0),
    (181514, 'campus:analysis:read', '辅助研判只读', '辅助研判', 'GET', '/campus/analysis/**', 1, '研判任务和结果只读', 0),
    (181515, 'campus:analysis:operate', '辅助研判维护', '辅助研判', 'POST', '/campus/analysis/**', 1, '研判任务创建、运行和结果复核', 0),
    (181516, 'campus:auto-report:read', '自动报告只读', '自动报告', 'GET', '/campus/auto-report/**', 1, '自动报告任务和日志只读', 0),
    (181517, 'campus:auto-report:operate', '自动报告维护', '自动报告', 'POST', '/campus/auto-report/**', 1, '自动报告任务维护、状态变更和运行', 0),
    (181518, 'campus:dict:enabled:read', '启用字典只读', '数据字典', 'GET', '/campus/dict/item/enabled', 1, '业务表单读取启用字典项', 0),
    (181519, 'campus:department:read', '组织只读', '组织机构', 'GET', '/campus/department/**', 1, '组织列表、树和详情只读', 0),
    (181520, 'campus:audit:read', '审计日志只读', '审计日志', 'GET', '/campus/audit/list', 1, '审计日志查询', 0),
    (181521, 'campus:ingest:read', '接入中心只读', '媒体接入', 'GET', '/campus/ingest/**', 1, '来源、任务、记录、运行日志、调用日志和白名单只读', 0),
    (181522, 'campus:ingest:record-operate', '接入记录处理', '媒体接入', 'POST', '/campus/ingest/record/**', 1, '接入记录提交和转换，不包含来源、任务、白名单配置', 0),
    (181523, 'campus:ingest:run', '接入任务运行', '媒体接入', 'POST', '/campus/ingest/task/run', 1, '接入任务手动运行，启用前需确认来源授权和额度', 0),
    (181524, 'campus:system:admin', '系统权限管理', '系统权限', 'ALL', '/campus/system/**', 1, '角色、菜单和接口权限管理', 0),
    (181525, 'campus:department:admin', '组织管理', '组织机构', 'ALL', '/campus/department/**', 1, '组织机构维护', 0),
    (181526, 'campus:dict:admin', '字典管理', '数据字典', 'ALL', '/campus/dict/**', 1, '字典类型和字典项维护', 0),
    (181527, 'campus:ingest:admin', '接入配置管理', '媒体接入', 'ALL', '/campus/ingest/**', 1, '来源、任务、白名单和接入运行配置管理', 0)
ON DUPLICATE KEY UPDATE
    `api_code` = VALUES(`api_code`),
    `api_name` = VALUES(`api_name`),
    `module_name` = VALUES(`module_name`),
    `request_method` = VALUES(`request_method`),
    `request_path` = VALUES(`request_path`),
    `status` = VALUES(`status`),
    `remark` = VALUES(`remark`),
    `deleted` = VALUES(`deleted`),
    `update_time` = CURRENT_TIMESTAMP;

UPDATE `campus_role_api`
SET `deleted` = 1,
    `update_time` = CURRENT_TIMESTAMP
WHERE `role_id` IN (180002, 180003)
  AND `api_id` = 180503
  AND `deleted` = 0;

INSERT INTO `campus_role_api` (`relation_id`, `role_id`, `api_id`)
VALUES
    (190900501, 180001, 180501),
    (190900504, 180001, 180504),
    (190900502, 180001, 180502)
ON DUPLICATE KEY UPDATE
    `deleted` = 0,
    `update_time` = CURRENT_TIMESTAMP;

INSERT INTO `campus_role_api` (`relation_id`, `role_id`, `api_id`)
VALUES
    (191180501, 180002, 180501),
    (191180504, 180002, 180504),
    (191181501, 180002, 181501),
    (191181502, 180002, 181502),
    (191181503, 180002, 181503),
    (191181504, 180002, 181504),
    (191181505, 180002, 181505),
    (191181506, 180002, 181506),
    (191181507, 180002, 181507),
    (191181508, 180002, 181508),
    (191181509, 180002, 181509),
    (191181510, 180002, 181510),
    (191181511, 180002, 181511),
    (191181512, 180002, 181512),
    (191181513, 180002, 181513),
    (191181514, 180002, 181514),
    (191181515, 180002, 181515),
    (191181516, 180002, 181516),
    (191181517, 180002, 181517),
    (191181518, 180002, 181518),
    (191181519, 180002, 181519),
    (191181521, 180002, 181521),
    (191181522, 180002, 181522)
ON DUPLICATE KEY UPDATE
    `deleted` = 0,
    `update_time` = CURRENT_TIMESTAMP;

INSERT INTO `campus_role_api` (`relation_id`, `role_id`, `api_id`)
VALUES
    (192180501, 180003, 180501),
    (192180504, 180003, 180504),
    (192181501, 180003, 181501),
    (192181502, 180003, 181502),
    (192181506, 180003, 181506),
    (192181508, 180003, 181508),
    (192181510, 180003, 181510),
    (192181512, 180003, 181512),
    (192181518, 180003, 181518)
ON DUPLICATE KEY UPDATE
    `deleted` = 0,
    `update_time` = CURRENT_TIMESTAMP;

UPDATE `campus_role_menu` rm
INNER JOIN `campus_permission_menu` m ON m.`menu_id` = rm.`menu_id`
SET rm.`deleted` = 1,
    rm.`update_time` = CURRENT_TIMESTAMP
WHERE rm.`role_id` = 180002
  AND m.`menu_code` = 'settings'
  AND rm.`deleted` = 0;

UPDATE `campus_role_menu` rm
INNER JOIN `campus_permission_menu` m ON m.`menu_id` = rm.`menu_id`
SET rm.`deleted` = 1,
    rm.`update_time` = CURRENT_TIMESTAMP
WHERE rm.`role_id` = 180003
  AND m.`menu_code` NOT IN ('workbench', 'situation', 'reports')
  AND rm.`deleted` = 0;

INSERT INTO `campus_role_menu` (`relation_id`, `role_id`, `menu_id`)
VALUES
    (193180101, 180003, 180101),
    (193180112, 180003, 180112),
    (193180109, 180003, 180109)
ON DUPLICATE KEY UPDATE
    `deleted` = 0,
    `update_time` = CURRENT_TIMESTAMP;

UPDATE `campus_user_role`
SET `deleted` = 1,
    `update_time` = CURRENT_TIMESTAMP
WHERE `role_id` = 180001
  AND `user_id` <> 13900000000
  AND `deleted` = 0;

INSERT INTO `campus_user_role` (`relation_id`, `user_id`, `role_id`)
SELECT `user_id`, `user_id`, 180001
FROM `user`
WHERE `status` = 1
  AND `user_id` = 13900000000
ON DUPLICATE KEY UPDATE
    `deleted` = 0,
    `update_time` = CURRENT_TIMESTAMP;

INSERT INTO `campus_user_role` (`relation_id`, `user_id`, `role_id`)
SELECT `user_id` + 290000000000, `user_id`, 180002
FROM `user`
WHERE `status` = 1
  AND `user_id` IS NOT NULL
  AND `user_id` <> 13900000000
ON DUPLICATE KEY UPDATE
    `deleted` = 0,
    `update_time` = CURRENT_TIMESTAMP;

INSERT INTO `campus_department` (
    `department_id`, `parent_id`, `department_name`, `department_code`, `department_type`,
    `leader_user_id`, `contact_phone`, `sort_no`, `status`, `deleted`, `create_user_id`, `update_user_id`
) VALUES
    (290001, 0, '试运行学校', 'SCHOOL_ROOT', 'school', NULL, NULL, 1, 1, 0, 0, 0),
    (290101, 290001, '网信办', 'WXB', 'department', NULL, NULL, 10, 1, 0, 0, 0),
    (290102, 290001, '宣传部', 'XCB', 'department', NULL, NULL, 20, 1, 0, 0, 0),
    (290103, 290001, '学生工作部', 'XGB', 'department', NULL, NULL, 30, 1, 0, 0, 0),
    (290104, 290001, '保卫处', 'BWC', 'department', NULL, NULL, 40, 1, 0, 0, 0),
    (290105, 290001, '后勤管理处', 'HQGLC', 'department', NULL, NULL, 50, 1, 0, 0, 0),
    (290106, 290001, '学院单位', 'COLLEGES', 'college', NULL, NULL, 60, 1, 0, 0, 0)
ON DUPLICATE KEY UPDATE
    `parent_id` = VALUES(`parent_id`),
    `department_name` = VALUES(`department_name`),
    `department_code` = VALUES(`department_code`),
    `department_type` = VALUES(`department_type`),
    `sort_no` = VALUES(`sort_no`),
    `status` = VALUES(`status`),
    `deleted` = 0,
    `update_time` = CURRENT_TIMESTAMP;

UPDATE `campus_department`
SET `parent_id` = 290001,
    `department_type` = 'department',
    `update_time` = CURRENT_TIMESTAMP
WHERE `department_id` IN (200101, 200102, 200103, 200104, 200105)
  AND `parent_id` = 0;

INSERT INTO `campus_dict_type` (`dict_type`, `dict_name`, `description`, `sort_no`, `status`, `deleted`)
VALUES
    ('department_type', '组织类型', '学校组织机构类型', 15, 1, 0)
ON DUPLICATE KEY UPDATE
    `dict_name` = VALUES(`dict_name`),
    `description` = VALUES(`description`),
    `sort_no` = VALUES(`sort_no`),
    `status` = VALUES(`status`),
    `deleted` = 0,
    `update_time` = CURRENT_TIMESTAMP;

INSERT INTO `campus_dict_item` (`dict_type`, `item_code`, `item_name`, `item_value`, `sort_no`, `status`, `deleted`)
VALUES
    ('department_type', 'school', '学校', 'school', 10, 1, 0),
    ('department_type', 'campus', '校区', 'campus', 20, 1, 0),
    ('department_type', 'college', '学院', 'college', 30, 1, 0),
    ('department_type', 'department', '职能部门', 'department', 40, 1, 0),
    ('department_type', 'grade', '年级', 'grade', 50, 1, 0),
    ('department_type', 'class', '班级', 'class', 60, 1, 0),
    ('department_type', 'office', '办公室', 'office', 70, 1, 0),
    ('source_platform', 'school_website', '学校官网', 'school_website', 10, 1, 0),
    ('source_platform', 'college_website', '学院网站', 'college_website', 20, 1, 0),
    ('source_platform', 'wechat_official', '微信公众号', 'wechat_official', 30, 1, 0),
    ('source_platform', 'weibo', '微博', 'weibo', 40, 1, 0),
    ('source_platform', 'douyin', '抖音', 'douyin', 50, 1, 0),
    ('source_platform', 'video_account', '视频号', 'video_account', 60, 1, 0),
    ('source_platform', 'xiaohongshu', '小红书', 'xiaohongshu', 70, 1, 0),
    ('source_platform', 'bilibili', 'B站', 'bilibili', 80, 1, 0),
    ('source_platform', 'forum', '公开论坛', 'forum', 90, 1, 0),
    ('source_platform', 'upper_transfer', '上级移交', 'upper_transfer', 100, 1, 0),
    ('source_platform', 'manual', '人工录入', 'manual', 110, 1, 0),
    ('account_type', 'official', '学校官方号', 'official', 10, 1, 0),
    ('account_type', 'department', '部门账号', 'department', 20, 1, 0),
    ('account_type', 'student_org', '学生组织账号', 'student_org', 30, 1, 0),
    ('account_type', 'personal_public', '个人公开账号', 'personal_public', 40, 1, 0),
    ('account_type', 'media', '媒体账号', 'media', 50, 1, 0),
    ('account_type', 'other', '其他', 'other', 60, 1, 0),
    ('sensitive_word_category', 'safety', '校园安全', 'safety', 10, 1, 0),
    ('sensitive_word_category', 'public_order', '秩序管理', 'public_order', 20, 1, 0),
    ('sensitive_word_category', 'teaching', '教学运行', 'teaching', 30, 1, 0),
    ('sensitive_word_category', 'network', '网络舆情', 'network', 40, 1, 0),
    ('sensitive_word_category', 'emergency', '突发事件', 'emergency', 50, 1, 0),
    ('sensitive_word_category', 'other', '其他', 'other', 60, 1, 0)
ON DUPLICATE KEY UPDATE
    `item_name` = VALUES(`item_name`),
    `item_value` = VALUES(`item_value`),
    `sort_no` = VALUES(`sort_no`),
    `status` = VALUES(`status`),
    `deleted` = 0,
    `update_time` = CURRENT_TIMESTAMP;
