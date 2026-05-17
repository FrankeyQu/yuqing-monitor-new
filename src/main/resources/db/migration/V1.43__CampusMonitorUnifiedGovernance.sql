-- Campus monitor unified governance.
-- Keeps data reversible by using visibility/status flags and logical delete only.

UPDATE `campus_permission_menu`
SET `visible` = 0,
    `status` = 0,
    `update_user_id` = 0
WHERE `deleted` = 0
  AND (`menu_code` = 'ingest' OR `route_path` IN ('/ingest', '/admin/ingest'));

UPDATE `campus_ingest_record`
SET `sentiment` = 'negative',
    `update_user_id` = 0
WHERE `sentiment` IS NOT NULL
  AND (LOWER(`sentiment`) IN ('negative', 'neg') OR `sentiment` LIKE '%负%');

UPDATE `campus_ingest_record`
SET `sentiment` = 'positive',
    `update_user_id` = 0
WHERE `sentiment` IS NOT NULL
  AND (LOWER(`sentiment`) IN ('positive', 'pos') OR `sentiment` LIKE '%正%');

UPDATE `campus_ingest_record`
SET `sentiment` = 'neutral',
    `update_user_id` = 0
WHERE `sentiment` IS NOT NULL
  AND (LOWER(`sentiment`) = 'neutral' OR `sentiment` LIKE '%中%');

UPDATE `campus_ingest_record`
SET `sentiment` = 'none',
    `update_user_id` = 0
WHERE `sentiment` IS NOT NULL
  AND (LOWER(`sentiment`) IN ('none', 'unknown') OR `sentiment` = '未知');

UPDATE `campus_monitor_result`
SET `sentiment` = 'negative',
    `update_user_id` = 0
WHERE `sentiment` IS NOT NULL
  AND (LOWER(`sentiment`) IN ('negative', 'neg') OR `sentiment` LIKE '%负%');

UPDATE `campus_monitor_result`
SET `sentiment` = 'positive',
    `update_user_id` = 0
WHERE `sentiment` IS NOT NULL
  AND (LOWER(`sentiment`) IN ('positive', 'pos') OR `sentiment` LIKE '%正%');

UPDATE `campus_monitor_result`
SET `sentiment` = 'neutral',
    `update_user_id` = 0
WHERE `sentiment` IS NOT NULL
  AND (LOWER(`sentiment`) = 'neutral' OR `sentiment` LIKE '%中%');

UPDATE `campus_monitor_result`
SET `sentiment` = 'none',
    `update_user_id` = 0
WHERE `sentiment` IS NOT NULL
  AND (LOWER(`sentiment`) IN ('none', 'unknown') OR `sentiment` = '未知');

UPDATE `campus_clue`
SET `sentiment` = 'negative',
    `update_user_id` = 0
WHERE `sentiment` IS NOT NULL
  AND (LOWER(`sentiment`) IN ('negative', 'neg') OR `sentiment` LIKE '%负%');

UPDATE `campus_clue`
SET `sentiment` = 'positive',
    `update_user_id` = 0
WHERE `sentiment` IS NOT NULL
  AND (LOWER(`sentiment`) IN ('positive', 'pos') OR `sentiment` LIKE '%正%');

UPDATE `campus_clue`
SET `sentiment` = 'neutral',
    `update_user_id` = 0
WHERE `sentiment` IS NOT NULL
  AND (LOWER(`sentiment`) = 'neutral' OR `sentiment` LIKE '%中%');

UPDATE `campus_clue`
SET `sentiment` = 'none',
    `update_user_id` = 0
WHERE `sentiment` IS NOT NULL
  AND (LOWER(`sentiment`) IN ('none', 'unknown') OR `sentiment` = '未知');

UPDATE `campus_monitor_result` mr
JOIN `campus_ingest_record` ir ON ir.`record_id` = mr.`ingest_record_id`
SET mr.`deleted` = 1,
    mr.`update_user_id` = 0
WHERE mr.`deleted` = 0
  AND mr.`clue_id` IS NULL
  AND mr.`alert_id` IS NULL
  AND (
      LOWER(IFNULL(ir.`external_id`, '')) LIKE 'search\_%'
      OR LOWER(IFNULL(ir.`original_url`, '')) LIKE '%/short-video/search\_%'
      OR TRIM(IFNULL(ir.`title`, '')) IN ('与搜索词无关', '内容过时', '封面质量差', '不再看到该作者', '不再看到该作品', '内容违规、血腥、低俗', '与其他结果相似', '不够权威', '其他')
      OR TRIM(IFNULL(ir.`content`, '')) IN ('与搜索词无关', '内容过时', '封面质量差', '不再看到该作者', '不再看到该作品', '内容违规、血腥、低俗', '与其他结果相似', '不够权威', '其他')
      OR (
          CONCAT(IFNULL(ir.`title`, ''), IFNULL(ir.`content`, '')) LIKE '%与搜索词无关%'
          AND CONCAT(IFNULL(ir.`title`, ''), IFNULL(ir.`content`, '')) LIKE '%内容过时%'
      )
      OR (
          CONCAT(IFNULL(ir.`title`, ''), IFNULL(ir.`content`, '')) LIKE '%封面质量差%'
          AND CONCAT(IFNULL(ir.`title`, ''), IFNULL(ir.`content`, '')) LIKE '%不再看到该作者%'
      )
  );

UPDATE `campus_ingest_record` ir
SET ir.`deleted` = 1,
    ir.`normalized_status` = 'ignored',
    ir.`error_message` = 'ui_noise_filtered',
    ir.`update_user_id` = 0
WHERE ir.`deleted` = 0
  AND NOT (ir.`target_type` = 'clue' AND ir.`target_id` IS NOT NULL)
  AND NOT EXISTS (
      SELECT 1
      FROM `campus_monitor_result` mr
      WHERE mr.`ingest_record_id` = ir.`record_id`
        AND mr.`deleted` = 0
        AND (mr.`clue_id` IS NOT NULL OR mr.`alert_id` IS NOT NULL)
  )
  AND (
      LOWER(IFNULL(ir.`external_id`, '')) LIKE 'search\_%'
      OR LOWER(IFNULL(ir.`original_url`, '')) LIKE '%/short-video/search\_%'
      OR TRIM(IFNULL(ir.`title`, '')) IN ('与搜索词无关', '内容过时', '封面质量差', '不再看到该作者', '不再看到该作品', '内容违规、血腥、低俗', '与其他结果相似', '不够权威', '其他')
      OR TRIM(IFNULL(ir.`content`, '')) IN ('与搜索词无关', '内容过时', '封面质量差', '不再看到该作者', '不再看到该作品', '内容违规、血腥、低俗', '与其他结果相似', '不够权威', '其他')
      OR (
          CONCAT(IFNULL(ir.`title`, ''), IFNULL(ir.`content`, '')) LIKE '%与搜索词无关%'
          AND CONCAT(IFNULL(ir.`title`, ''), IFNULL(ir.`content`, '')) LIKE '%内容过时%'
      )
      OR (
          CONCAT(IFNULL(ir.`title`, ''), IFNULL(ir.`content`, '')) LIKE '%封面质量差%'
          AND CONCAT(IFNULL(ir.`title`, ''), IFNULL(ir.`content`, '')) LIKE '%不再看到该作者%'
      )
  );
