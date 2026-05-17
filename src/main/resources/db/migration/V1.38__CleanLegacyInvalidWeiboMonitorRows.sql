-- Hide legacy Weibo profile/topic cards that are not real post records.

UPDATE campus_monitor_result
SET deleted = 1,
    update_user_id = 0
WHERE deleted = 0
  AND LOWER(IFNULL(platform, '')) IN ('weibo', '微博', '新浪微博')
  AND (
      original_url IS NULL
      OR original_url = ''
      OR original_url NOT LIKE 'http%'
      OR LOWER(original_url) LIKE 'http://s.weibo.com/%'
      OR LOWER(original_url) LIKE 'https://s.weibo.com/%'
      OR NOT (
          LOWER(original_url) REGEXP '^https?://weibo\\.com/[0-9]+/[0-9]{13,}'
          OR LOWER(original_url) REGEXP '^https?://m\\.weibo\\.cn/detail/[0-9]{13,}'
      )
      OR content LIKE '%实时资讯，海量讨论%'
      OR title REGEXP '^[0-9.]+万?帖子'
  );

UPDATE campus_ingest_record
SET deleted = 1,
    update_user_id = 0
WHERE deleted = 0
  AND LOWER(IFNULL(platform, '')) IN ('weibo', '微博', '新浪微博')
  AND (
      original_url IS NULL
      OR original_url = ''
      OR original_url NOT LIKE 'http%'
      OR LOWER(original_url) LIKE 'http://s.weibo.com/%'
      OR LOWER(original_url) LIKE 'https://s.weibo.com/%'
      OR NOT (
          LOWER(original_url) REGEXP '^https?://weibo\\.com/[0-9]+/[0-9]{13,}'
          OR LOWER(original_url) REGEXP '^https?://m\\.weibo\\.cn/detail/[0-9]{13,}'
      )
      OR content LIKE '%实时资讯，海量讨论%'
      OR title REGEXP '^[0-9.]+万?帖子'
  );

UPDATE campus_clue
SET deleted = 1,
    update_user_id = 0
WHERE deleted = 0
  AND LOWER(IFNULL(source_platform, '')) IN ('weibo', '微博', '新浪微博')
  AND (
      original_url IS NULL
      OR original_url = ''
      OR original_url NOT LIKE 'http%'
      OR LOWER(original_url) LIKE 'http://s.weibo.com/%'
      OR LOWER(original_url) LIKE 'https://s.weibo.com/%'
      OR NOT (
          LOWER(original_url) REGEXP '^https?://weibo\\.com/[0-9]+/[0-9]{13,}'
          OR LOWER(original_url) REGEXP '^https?://m\\.weibo\\.cn/detail/[0-9]{13,}'
      )
      OR clue_content LIKE '%实时资讯，海量讨论%'
      OR clue_title REGEXP '^[0-9.]+万?帖子'
  );
