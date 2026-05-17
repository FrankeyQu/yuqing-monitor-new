-- Batch 37: remove legacy food-safety demo/search noise and make WeChat auto-ingest less brittle.

UPDATE `campus_ingest_task`
SET `schedule_enabled` = 0,
    `task_status` = 'paused',
    `update_user_id` = 0,
    `update_time` = CURRENT_TIMESTAMP,
    `governance_remark` = '历史食品安全接入任务已停用，避免旧演示/宽泛搜索数据继续进入监测信息'
WHERE `deleted` = 0
  AND (`task_name` LIKE '%食品安全%' OR `task_name` LIKE '%校园食品%');

UPDATE `campus_clue` c
JOIN `campus_ingest_record` r
  ON r.`target_type` = 'clue'
 AND r.`target_id` = c.`clue_id`
JOIN `campus_ingest_task` t
  ON t.`task_id` = r.`task_id`
SET c.`deleted` = 1,
    c.`update_user_id` = 0,
    c.`update_time` = CURRENT_TIMESTAMP
WHERE c.`deleted` = 0
  AND r.`deleted` = 0
  AND t.`deleted` = 0
  AND (t.`task_name` LIKE '%食品安全%' OR t.`task_name` LIKE '%校园食品%');

UPDATE `campus_ingest_record` r
JOIN `campus_ingest_task` t
  ON t.`task_id` = r.`task_id`
SET r.`deleted` = 1,
    r.`update_user_id` = 0,
    r.`update_time` = CURRENT_TIMESTAMP
WHERE r.`deleted` = 0
  AND t.`deleted` = 0
  AND (t.`task_name` LIKE '%食品安全%' OR t.`task_name` LIKE '%校园食品%');

UPDATE `campus_alert` a
JOIN `campus_monitor_result` mr
  ON (mr.`monitor_result_id` = a.`source_object_id` OR mr.`alert_id` = a.`alert_id`)
JOIN `campus_monitor_task` mt
  ON mt.`monitor_task_id` = mr.`monitor_task_id`
SET a.`deleted` = 1,
    a.`update_user_id` = 0,
    a.`update_time` = CURRENT_TIMESTAMP
WHERE a.`deleted` = 0
  AND a.`alert_source` = 'monitor'
  AND mr.`deleted` = 0
  AND mt.`deleted` = 0
  AND (
      mt.`task_name` LIKE '%食品安全%'
      OR mt.`monitor_subject` LIKE '%食品%'
      OR mt.`keywords` LIKE '%食品安全%'
  );

UPDATE `campus_monitor_result` mr
JOIN `campus_monitor_task` mt
  ON mt.`monitor_task_id` = mr.`monitor_task_id`
SET mr.`deleted` = 1,
    mr.`update_user_id` = 0,
    mr.`update_time` = CURRENT_TIMESTAMP
WHERE mr.`deleted` = 0
  AND mt.`deleted` = 0
  AND (
      mt.`task_name` LIKE '%食品安全%'
      OR mt.`monitor_subject` LIKE '%食品%'
      OR mt.`keywords` LIKE '%食品安全%'
  );

UPDATE `campus_monitor_task`
SET `display_enabled` = 0,
    `auto_ingest_enabled` = 0,
    `schedule_enabled` = 0,
    `task_status` = 'disabled',
    `update_user_id` = 0,
    `update_time` = CURRENT_TIMESTAMP
WHERE `deleted` = 0
  AND (
      `task_name` LIKE '%食品安全%'
      OR `monitor_subject` LIKE '%食品%'
      OR `keywords` LIKE '%食品安全%'
  );

UPDATE `campus_ingest_task`
SET `fetch_config` = REPLACE(
        REPLACE(`fetch_config`, '"sortType":"_2"', '"sortType":"_0"'),
        '"query":"新疆大学 新大"', '"query":"新疆大学"'
    ),
    `update_user_id` = 0,
    `update_time` = CURRENT_TIMESTAMP
WHERE `deleted` = 0
  AND `fetch_config` LIKE '%"endpointKey":"wechat_mp_search_article"%';
