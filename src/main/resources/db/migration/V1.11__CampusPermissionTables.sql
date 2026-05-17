-- 校园权限模型表
-- Batch 18: roles, menus and API permissions for /campus/**.

CREATE TABLE IF NOT EXISTS `campus_permission_role` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `role_id` bigint NOT NULL COMMENT '角色业务ID',
    `role_code` varchar(64) NOT NULL COMMENT '角色编码',
    `role_name` varchar(128) NOT NULL COMMENT '角色名称',
    `role_type` varchar(64) DEFAULT 'business' COMMENT '角色类型(admin|business|viewer)',
    `data_scope` varchar(64) DEFAULT 'school' COMMENT '数据范围(school|department|self)',
    `status` tinyint DEFAULT 1 COMMENT '状态(1启用,0停用)',
    `remark` varchar(512) DEFAULT NULL COMMENT '备注',
    `deleted` tinyint DEFAULT 0 COMMENT '逻辑删除',
    `create_user_id` bigint DEFAULT NULL COMMENT '创建人',
    `update_user_id` bigint DEFAULT NULL COMMENT '更新人',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_campus_permission_role_id` (`role_id`),
    UNIQUE KEY `uk_campus_permission_role_code` (`role_code`),
    KEY `idx_campus_permission_role_status` (`status`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='校园权限角色表';

CREATE TABLE IF NOT EXISTS `campus_permission_menu` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `menu_id` bigint NOT NULL COMMENT '菜单业务ID',
    `parent_id` bigint DEFAULT 0 COMMENT '父菜单ID',
    `menu_code` varchar(64) NOT NULL COMMENT '菜单编码',
    `menu_name` varchar(128) NOT NULL COMMENT '菜单名称',
    `menu_type` varchar(32) DEFAULT 'menu' COMMENT '菜单类型(menu|button)',
    `route_path` varchar(255) DEFAULT NULL COMMENT '前端路由',
    `component_path` varchar(255) DEFAULT NULL COMMENT '组件路径',
    `permission_code` varchar(128) DEFAULT NULL COMMENT '权限编码',
    `icon` varchar(64) DEFAULT NULL COMMENT '图标',
    `sort_no` int DEFAULT 0 COMMENT '排序',
    `visible` tinyint DEFAULT 1 COMMENT '是否可见',
    `status` tinyint DEFAULT 1 COMMENT '状态(1启用,0停用)',
    `deleted` tinyint DEFAULT 0 COMMENT '逻辑删除',
    `create_user_id` bigint DEFAULT NULL COMMENT '创建人',
    `update_user_id` bigint DEFAULT NULL COMMENT '更新人',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_campus_permission_menu_id` (`menu_id`),
    UNIQUE KEY `uk_campus_permission_menu_code` (`menu_code`),
    KEY `idx_campus_permission_menu_parent` (`parent_id`, `sort_no`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='校园权限菜单表';

CREATE TABLE IF NOT EXISTS `campus_permission_api` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `api_id` bigint NOT NULL COMMENT '接口权限业务ID',
    `api_code` varchar(128) NOT NULL COMMENT '接口权限编码',
    `api_name` varchar(128) NOT NULL COMMENT '接口权限名称',
    `module_name` varchar(128) DEFAULT NULL COMMENT '模块名称',
    `request_method` varchar(16) DEFAULT 'ALL' COMMENT '请求方法',
    `request_path` varchar(255) NOT NULL COMMENT '接口路径，支持 /campus/** 前缀匹配',
    `status` tinyint DEFAULT 1 COMMENT '状态(1启用,0停用)',
    `remark` varchar(512) DEFAULT NULL COMMENT '备注',
    `deleted` tinyint DEFAULT 0 COMMENT '逻辑删除',
    `create_user_id` bigint DEFAULT NULL COMMENT '创建人',
    `update_user_id` bigint DEFAULT NULL COMMENT '更新人',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_campus_permission_api_id` (`api_id`),
    UNIQUE KEY `uk_campus_permission_api_code` (`api_code`),
    KEY `idx_campus_permission_api_path` (`request_path`, `status`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='校园接口权限表';

CREATE TABLE IF NOT EXISTS `campus_user_role` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `relation_id` bigint NOT NULL COMMENT '关联业务ID',
    `user_id` bigint NOT NULL COMMENT '用户业务ID',
    `role_id` bigint NOT NULL COMMENT '角色业务ID',
    `deleted` tinyint DEFAULT 0 COMMENT '逻辑删除',
    `create_user_id` bigint DEFAULT NULL COMMENT '创建人',
    `update_user_id` bigint DEFAULT NULL COMMENT '更新人',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_campus_user_role_relation` (`relation_id`),
    UNIQUE KEY `uk_campus_user_role` (`user_id`, `role_id`),
    KEY `idx_campus_user_role_user` (`user_id`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='校园用户角色表';

CREATE TABLE IF NOT EXISTS `campus_role_menu` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `relation_id` bigint NOT NULL COMMENT '关联业务ID',
    `role_id` bigint NOT NULL COMMENT '角色业务ID',
    `menu_id` bigint NOT NULL COMMENT '菜单业务ID',
    `deleted` tinyint DEFAULT 0 COMMENT '逻辑删除',
    `create_user_id` bigint DEFAULT NULL COMMENT '创建人',
    `update_user_id` bigint DEFAULT NULL COMMENT '更新人',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_campus_role_menu_relation` (`relation_id`),
    UNIQUE KEY `uk_campus_role_menu` (`role_id`, `menu_id`),
    KEY `idx_campus_role_menu_role` (`role_id`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='校园角色菜单表';

CREATE TABLE IF NOT EXISTS `campus_role_api` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `relation_id` bigint NOT NULL COMMENT '关联业务ID',
    `role_id` bigint NOT NULL COMMENT '角色业务ID',
    `api_id` bigint NOT NULL COMMENT '接口权限业务ID',
    `deleted` tinyint DEFAULT 0 COMMENT '逻辑删除',
    `create_user_id` bigint DEFAULT NULL COMMENT '创建人',
    `update_user_id` bigint DEFAULT NULL COMMENT '更新人',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_campus_role_api_relation` (`relation_id`),
    UNIQUE KEY `uk_campus_role_api` (`role_id`, `api_id`),
    KEY `idx_campus_role_api_role` (`role_id`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='校园角色接口权限表';

INSERT INTO `campus_permission_role` (`role_id`, `role_code`, `role_name`, `role_type`, `data_scope`, `status`, `remark`)
VALUES
    (180001, 'campus_admin', '校园系统管理员', 'admin', 'school', 1, '默认拥有校园系统全部菜单和接口权限'),
    (180002, 'campus_operator', '舆情处置员', 'business', 'school', 1, '可处理线索、预警、事件、检测、报告等业务'),
    (180003, 'campus_viewer', '校园查看员', 'viewer', 'school', 1, '可查看工作台和报告')
ON DUPLICATE KEY UPDATE
    `role_name` = VALUES(`role_name`),
    `role_type` = VALUES(`role_type`),
    `data_scope` = VALUES(`data_scope`),
    `status` = VALUES(`status`),
    `remark` = VALUES(`remark`),
    `update_time` = CURRENT_TIMESTAMP;

INSERT INTO `campus_permission_menu` (`menu_id`, `parent_id`, `menu_code`, `menu_name`, `menu_type`, `route_path`, `permission_code`, `icon`, `sort_no`, `visible`, `status`)
VALUES
    (180101, 0, 'workbench', '工作台', 'menu', '/', 'campus:workbench:view', 'Gauge', 10, 1, 1),
    (180102, 0, 'clues', '线索库', 'menu', '/clues', 'campus:clue:view', 'ClipboardList', 20, 1, 1),
    (180103, 0, 'accounts', '重点账号', 'menu', '/accounts', 'campus:account:view', 'UsersRound', 30, 1, 1),
    (180104, 0, 'events', '事件处置', 'menu', '/events', 'campus:event:view', 'ShieldCheck', 40, 1, 1),
    (180105, 0, 'alerts', '预警中心', 'menu', '/alerts', 'campus:alert:view', 'Bell', 50, 1, 1),
    (180106, 0, 'detection', '检测任务', 'menu', '/detection', 'campus:detection:view', 'Radar', 60, 1, 1),
    (180107, 0, 'ingest', '数据接入', 'menu', '/ingest', 'campus:ingest:view', 'Database', 70, 1, 1),
    (180108, 0, 'analysis', '辅助研判', 'menu', '/analysis', 'campus:analysis:view', 'BrainCircuit', 80, 1, 1),
    (180109, 0, 'reports', '报告归档', 'menu', '/reports', 'campus:report:view', 'FileText', 90, 1, 1),
    (180110, 0, 'auto-reports', '自动报告', 'menu', '/auto-reports', 'campus:auto-report:view', 'CalendarClock', 100, 1, 1),
    (180111, 0, 'settings', '系统设置', 'menu', '/settings/departments', 'campus:settings:view', 'BookOpenText', 110, 1, 1)
ON DUPLICATE KEY UPDATE
    `menu_name` = VALUES(`menu_name`),
    `route_path` = VALUES(`route_path`),
    `permission_code` = VALUES(`permission_code`),
    `icon` = VALUES(`icon`),
    `sort_no` = VALUES(`sort_no`),
    `visible` = VALUES(`visible`),
    `status` = VALUES(`status`),
    `update_time` = CURRENT_TIMESTAMP;

INSERT INTO `campus_permission_api` (`api_id`, `api_code`, `api_name`, `module_name`, `request_method`, `request_path`, `status`, `remark`)
VALUES
    (180501, 'campus:system:self', '当前用户与菜单', '系统权限', 'ALL', '/campus/system/**', 1, '登录用户可读取自身权限和菜单'),
    (180502, 'campus:api:all', '校园系统全部接口', '系统权限', 'ALL', '/campus/**', 1, '管理员权限'),
    (180503, 'campus:business:operate', '校园业务操作接口', '校园业务', 'ALL', '/campus/**', 1, '业务处置员权限')
ON DUPLICATE KEY UPDATE
    `api_name` = VALUES(`api_name`),
    `module_name` = VALUES(`module_name`),
    `request_method` = VALUES(`request_method`),
    `request_path` = VALUES(`request_path`),
    `status` = VALUES(`status`),
    `remark` = VALUES(`remark`),
    `update_time` = CURRENT_TIMESTAMP;

INSERT IGNORE INTO `campus_role_menu` (`relation_id`, `role_id`, `menu_id`)
SELECT 180000000 + menu_id, 180001, menu_id
FROM `campus_permission_menu`
WHERE deleted = 0;

INSERT IGNORE INTO `campus_role_menu` (`relation_id`, `role_id`, `menu_id`)
SELECT 181000000 + menu_id, 180002, menu_id
FROM `campus_permission_menu`
WHERE deleted = 0
  AND menu_code NOT IN ('settings');

INSERT IGNORE INTO `campus_role_menu` (`relation_id`, `role_id`, `menu_id`)
SELECT 182000000 + menu_id, 180003, menu_id
FROM `campus_permission_menu`
WHERE deleted = 0
  AND menu_code IN ('workbench', 'reports');

INSERT IGNORE INTO `campus_role_api` (`relation_id`, `role_id`, `api_id`)
VALUES
    (180900501, 180001, 180501),
    (180900502, 180001, 180502),
    (180900503, 180002, 180501),
    (180900504, 180002, 180503),
    (180900505, 180003, 180501);

INSERT IGNORE INTO `campus_user_role` (`relation_id`, `user_id`, `role_id`)
SELECT `user_id`, `user_id`, 180001
FROM `user`
WHERE `status` = 1
  AND `user_id` IS NOT NULL;
