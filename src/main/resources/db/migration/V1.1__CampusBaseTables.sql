-- 校园舆情系统基础能力表
-- Batch 1: organization, dictionary and enhanced audit log.

CREATE TABLE IF NOT EXISTS `campus_department` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `department_id` bigint NOT NULL COMMENT '部门业务ID',
    `parent_id` bigint DEFAULT 0 COMMENT '父级部门业务ID',
    `department_name` varchar(128) NOT NULL COMMENT '部门名称',
    `department_code` varchar(64) DEFAULT NULL COMMENT '部门编码',
    `department_type` varchar(32) DEFAULT NULL COMMENT '部门类型(school|campus|college|department|grade|class)',
    `leader_user_id` bigint DEFAULT NULL COMMENT '负责人用户ID',
    `contact_phone` varchar(32) DEFAULT NULL COMMENT '联系电话',
    `sort_no` int DEFAULT 0 COMMENT '排序号',
    `status` tinyint DEFAULT 1 COMMENT '状态(1启用,0停用)',
    `deleted` tinyint DEFAULT 0 COMMENT '逻辑删除(0否,1是)',
    `create_user_id` bigint DEFAULT NULL COMMENT '创建人',
    `update_user_id` bigint DEFAULT NULL COMMENT '更新人',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_campus_department_id` (`department_id`),
    KEY `idx_campus_department_parent` (`parent_id`),
    KEY `idx_campus_department_code` (`department_code`),
    KEY `idx_campus_department_status` (`status`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='校园组织机构表';

CREATE TABLE IF NOT EXISTS `campus_dict_type` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `dict_type` varchar(64) NOT NULL COMMENT '字典类型',
    `dict_name` varchar(128) NOT NULL COMMENT '字典名称',
    `description` varchar(512) DEFAULT NULL COMMENT '说明',
    `sort_no` int DEFAULT 0 COMMENT '排序号',
    `status` tinyint DEFAULT 1 COMMENT '状态(1启用,0停用)',
    `deleted` tinyint DEFAULT 0 COMMENT '逻辑删除(0否,1是)',
    `create_user_id` bigint DEFAULT NULL COMMENT '创建人',
    `update_user_id` bigint DEFAULT NULL COMMENT '更新人',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_campus_dict_type` (`dict_type`),
    KEY `idx_campus_dict_type_status` (`status`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='校园业务字典类型表';

CREATE TABLE IF NOT EXISTS `campus_dict_item` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `dict_type` varchar(64) NOT NULL COMMENT '字典类型',
    `item_code` varchar(64) NOT NULL COMMENT '字典项编码',
    `item_name` varchar(128) NOT NULL COMMENT '字典项名称',
    `item_value` varchar(255) DEFAULT NULL COMMENT '字典项值',
    `description` varchar(512) DEFAULT NULL COMMENT '说明',
    `sort_no` int DEFAULT 0 COMMENT '排序号',
    `status` tinyint DEFAULT 1 COMMENT '状态(1启用,0停用)',
    `deleted` tinyint DEFAULT 0 COMMENT '逻辑删除(0否,1是)',
    `create_user_id` bigint DEFAULT NULL COMMENT '创建人',
    `update_user_id` bigint DEFAULT NULL COMMENT '更新人',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_campus_dict_item` (`dict_type`, `item_code`),
    KEY `idx_campus_dict_item_type` (`dict_type`, `status`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='校园业务字典项表';

CREATE TABLE IF NOT EXISTS `campus_audit_log` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `audit_id` bigint NOT NULL COMMENT '审计业务ID',
    `operator_user_id` bigint DEFAULT NULL COMMENT '操作人用户ID',
    `operator_name` varchar(128) DEFAULT NULL COMMENT '操作人名称',
    `operator_department_id` bigint DEFAULT NULL COMMENT '操作人部门ID',
    `operation_type` varchar(64) NOT NULL COMMENT '操作类型',
    `module_name` varchar(128) NOT NULL COMMENT '模块名称',
    `object_type` varchar(64) DEFAULT NULL COMMENT '对象类型',
    `object_id` varchar(128) DEFAULT NULL COMMENT '对象ID',
    `request_method` varchar(16) DEFAULT NULL COMMENT '请求方法',
    `request_uri` varchar(512) DEFAULT NULL COMMENT '请求地址',
    `request_ip` varchar(64) DEFAULT NULL COMMENT '请求IP',
    `request_params` text COMMENT '请求参数摘要',
    `before_value` text COMMENT '变更前摘要',
    `after_value` text COMMENT '变更后摘要',
    `operation_result` tinyint DEFAULT 1 COMMENT '操作结果(1成功,0失败)',
    `failure_reason` varchar(512) DEFAULT NULL COMMENT '失败原因',
    `task_no` varchar(128) DEFAULT NULL COMMENT '任务编号/来源依据编号',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_campus_audit_id` (`audit_id`),
    KEY `idx_campus_audit_operator` (`operator_user_id`, `create_time`),
    KEY `idx_campus_audit_object` (`object_type`, `object_id`),
    KEY `idx_campus_audit_module` (`module_name`, `operation_type`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='校园业务增强审计日志表';

INSERT INTO `campus_dict_type` (`dict_type`, `dict_name`, `description`, `sort_no`)
VALUES
    ('clue_source', '线索来源', '校园舆情线索来源分类', 10),
    ('source_platform', '来源平台', '线索或账号动态的平台来源', 20),
    ('risk_level', '风险等级', '舆情风险等级', 30),
    ('event_status', '事件状态', '舆情事件流转状态', 40),
    ('disposal_status', '处置状态', '处置任务办理状态', 50),
    ('account_type', '账号类型', '重点关注账号类型', 60),
    ('account_focus_level', '账号关注级别', '重点关注账号关注级别', 70),
    ('sensitive_word_category', '敏感词类别', '预警敏感词分类', 80),
    ('report_type', '报告类型', '日报、周报、专报、归档报告分类', 90)
ON DUPLICATE KEY UPDATE
    `dict_name` = VALUES(`dict_name`),
    `description` = VALUES(`description`),
    `sort_no` = VALUES(`sort_no`),
    `update_time` = CURRENT_TIMESTAMP;

INSERT INTO `campus_dict_item` (`dict_type`, `item_code`, `item_name`, `item_value`, `sort_no`)
VALUES
    ('clue_source', 'manual', '人工录入', 'manual', 10),
    ('clue_source', 'excel', 'Excel导入', 'excel', 20),
    ('clue_source', 'upper_transfer', '上级移交', 'upper_transfer', 30),
    ('clue_source', 'api', '接口接入', 'api', 40),
    ('risk_level', 'normal', '一般关注', 'normal', 10),
    ('risk_level', 'higher', '较大风险', 'higher', 20),
    ('risk_level', 'major', '重大风险', 'major', 30),
    ('risk_level', 'urgent', '紧急事件', 'urgent', 40),
    ('event_status', 'pending_judge', '待研判', 'pending_judge', 10),
    ('event_status', 'rated', '已定级', 'rated', 20),
    ('event_status', 'assigned', '已分派', 'assigned', 30),
    ('event_status', 'processing', '处理中', 'processing', 40),
    ('event_status', 'feedback', '已反馈', 'feedback', 50),
    ('event_status', 'reviewed', '已复核', 'reviewed', 60),
    ('event_status', 'archived', '已归档', 'archived', 70),
    ('disposal_status', 'pending', '待处理', 'pending', 10),
    ('disposal_status', 'processing', '处理中', 'processing', 20),
    ('disposal_status', 'returned', '退回重办', 'returned', 30),
    ('disposal_status', 'completed', '已完成', 'completed', 40),
    ('account_focus_level', 'normal', '一般关注', 'normal', 10),
    ('account_focus_level', 'important', '重点关注', 'important', 20),
    ('account_focus_level', 'urgent', '紧急关注', 'urgent', 30),
    ('report_type', 'daily', '每日简报', 'daily', 10),
    ('report_type', 'weekly', '每周周报', 'weekly', 20),
    ('report_type', 'special', '专题专报', 'special', 30),
    ('report_type', 'archive', '归档报告', 'archive', 40)
ON DUPLICATE KEY UPDATE
    `item_name` = VALUES(`item_name`),
    `item_value` = VALUES(`item_value`),
    `sort_no` = VALUES(`sort_no`),
    `update_time` = CURRENT_TIMESTAMP;
