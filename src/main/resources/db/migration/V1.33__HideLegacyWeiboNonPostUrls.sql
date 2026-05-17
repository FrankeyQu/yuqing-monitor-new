-- Batch 36: remove historic Weibo profile/search/external URLs from post detail fields.

UPDATE `campus_ingest_record`
SET `original_url` = NULL,
    `update_time` = CURRENT_TIMESTAMP
WHERE `deleted` = 0
  AND `platform` = 'weibo'
  AND `original_url` IS NOT NULL
  AND `original_url` != ''
  AND NOT (
      LOWER(`original_url`) REGEXP '^https?://weibo\\.com/[0-9]+/[0-9]{13,}'
      OR LOWER(`original_url`) REGEXP '^https?://m\\.weibo\\.cn/detail/[0-9]{13,}'
  );

UPDATE `campus_clue` c
LEFT JOIN `campus_ingest_record` ir
  ON ir.`target_type` = 'clue'
 AND ir.`target_id` = c.`clue_id`
 AND ir.`deleted` = 0
SET c.`original_url` = NULL,
    c.`update_time` = CURRENT_TIMESTAMP
WHERE c.`deleted` = 0
  AND COALESCE(NULLIF(ir.`platform`, ''), c.`source_platform`) = 'weibo'
  AND c.`original_url` IS NOT NULL
  AND c.`original_url` != ''
  AND NOT (
      LOWER(c.`original_url`) REGEXP '^https?://weibo\\.com/[0-9]+/[0-9]{13,}'
      OR LOWER(c.`original_url`) REGEXP '^https?://m\\.weibo\\.cn/detail/[0-9]{13,}'
  );
