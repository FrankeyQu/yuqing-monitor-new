-- Batch 33: add backend monitor task settings entry.

INSERT INTO `campus_permission_menu` (
    `menu_id`, `parent_id`, `menu_code`, `menu_name`, `menu_type`,
    `route_path`, `permission_code`, `icon`, `sort_no`, `visible`, `status`, `deleted`
) VALUES
    (180115, 0, 'monitor_task_admin', '监测任务管理', 'menu', '/admin/monitor-tasks', 'campus:monitor:view', 'ClipboardList', 17, 1, 1, 0)
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
    (211180115, 180001, 180115),
    (212180115, 180002, 180115),
    (213180115, 180003, 180115)
ON DUPLICATE KEY UPDATE
    `deleted` = 0,
    `update_time` = CURRENT_TIMESTAMP;
