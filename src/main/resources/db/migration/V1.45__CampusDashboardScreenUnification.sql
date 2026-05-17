-- Unify the campus dashboard and situation screen entry.
-- Keep /situation reachable for direct links, but hide the duplicate menu item from customers.

UPDATE `campus_permission_menu`
SET `menu_name` = '舆情态势',
    `icon` = 'LayoutDashboard',
    `update_user_id` = 0
WHERE `deleted` = 0
  AND `menu_code` = 'workbench'
  AND `route_path` = '/';

UPDATE `campus_permission_menu`
SET `visible` = 0,
    `icon` = 'MonitorUp',
    `update_user_id` = 0
WHERE `deleted` = 0
  AND (`menu_code` = 'situation' OR `route_path` = '/situation');
