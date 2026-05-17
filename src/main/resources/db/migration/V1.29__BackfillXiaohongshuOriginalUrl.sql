-- Backfill Xiaohongshu original URLs from exact note ids.
-- TikHub search/detail records use external_id as note_id; this data fix is deterministic
-- and does not consume platform API quota.

UPDATE campus_ingest_record
SET original_url = CONCAT('https://www.xiaohongshu.com/explore/', external_id),
    update_time = NOW()
WHERE deleted = 0
  AND platform IN ('xiaohongshu', '小红书', 'red')
  AND (original_url IS NULL OR original_url = '')
  AND external_id IS NOT NULL
  AND external_id <> ''
  AND external_id REGEXP '^[A-Za-z0-9_-]+$';
