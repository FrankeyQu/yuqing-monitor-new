-- Hide the legacy clue library menu entry now that /monitor is the single front-office information workspace.

UPDATE `campus_permission_menu`
SET `visible` = 0,
    `status` = 0,
    `update_time` = CURRENT_TIMESTAMP
WHERE `menu_code` = 'clues'
  AND `route_path` = '/clues'
  AND `deleted` = 0;
