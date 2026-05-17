-- Batch 36: normalize historic Weibo links and remove unusable app/search URLs from display data.

UPDATE `campus_ingest_record`
SET `original_url` = CONCAT('https://m.weibo.cn/detail/', `external_id`),
    `update_time` = CURRENT_TIMESTAMP
WHERE `deleted` = 0
  AND `platform` = 'weibo'
  AND `external_id` REGEXP '^[0-9]{13,}$'
  AND (
      `original_url` IS NULL
      OR `original_url` = ''
      OR `original_url` NOT LIKE 'http%'
      OR LOWER(`original_url`) LIKE 'http://s.weibo.com/%'
      OR LOWER(`original_url`) LIKE 'https://s.weibo.com/%'
  )
  AND IFNULL(`content`, '') NOT LIKE '%实时资讯，海量讨论%'
  AND IFNULL(`title`, '') NOT REGEXP '^[0-9.]+万?帖子';

UPDATE `campus_ingest_record`
SET `original_url` = NULL,
    `update_time` = CURRENT_TIMESTAMP
WHERE `deleted` = 0
  AND `platform` = 'weibo'
  AND (
      `original_url` NOT LIKE 'http%'
      OR LOWER(`original_url`) LIKE 'http://s.weibo.com/%'
      OR LOWER(`original_url`) LIKE 'https://s.weibo.com/%'
      OR IFNULL(`content`, '') LIKE '%实时资讯，海量讨论%'
      OR IFNULL(`title`, '') REGEXP '^[0-9.]+万?帖子'
  );

UPDATE `campus_clue` c
JOIN `campus_ingest_record` ir
  ON ir.`target_type` = 'clue'
 AND ir.`target_id` = c.`clue_id`
 AND ir.`deleted` = 0
SET c.`original_url` = ir.`original_url`,
    c.`update_time` = CURRENT_TIMESTAMP
WHERE c.`deleted` = 0
  AND COALESCE(NULLIF(ir.`platform`, ''), c.`source_platform`) = 'weibo'
  AND ir.`original_url` IS NOT NULL
  AND ir.`original_url` != ''
  AND ir.`original_url` LIKE 'http%'
  AND LOWER(ir.`original_url`) NOT LIKE 'http://s.weibo.com/%'
  AND LOWER(ir.`original_url`) NOT LIKE 'https://s.weibo.com/%'
  AND (
      c.`original_url` IS NULL
      OR c.`original_url` = ''
      OR c.`original_url` NOT LIKE 'http%'
      OR LOWER(c.`original_url`) LIKE 'http://s.weibo.com/%'
      OR LOWER(c.`original_url`) LIKE 'https://s.weibo.com/%'
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
  AND (
      c.`original_url` NOT LIKE 'http%'
      OR LOWER(c.`original_url`) LIKE 'http://s.weibo.com/%'
      OR LOWER(c.`original_url`) LIKE 'https://s.weibo.com/%'
  );
