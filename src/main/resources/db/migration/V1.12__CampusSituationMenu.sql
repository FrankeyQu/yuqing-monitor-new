-- Batch 19: campus situation screen menu.

INSERT INTO `campus_permission_menu` (`menu_id`, `parent_id`, `menu_code`, `menu_name`, `menu_type`, `route_path`, `permission_code`, `icon`, `sort_no`, `visible`, `status`)
VALUES
    (180112, 0, 'situation', '态势大屏', 'menu', '/situation', 'campus:situation:view', 'Activity', 15, 1, 1)
ON DUPLICATE KEY UPDATE
    `menu_name` = VALUES(`menu_name`),
    `route_path` = VALUES(`route_path`),
    `permission_code` = VALUES(`permission_code`),
    `icon` = VALUES(`icon`),
    `sort_no` = VALUES(`sort_no`),
    `visible` = VALUES(`visible`),
    `status` = VALUES(`status`),
    `update_time` = CURRENT_TIMESTAMP;

INSERT IGNORE INTO `campus_role_menu` (`relation_id`, `role_id`, `menu_id`)
VALUES
    (183180112, 180001, 180112),
    (184180112, 180002, 180112),
    (185180112, 180003, 180112);
