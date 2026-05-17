-- Clean legacy TikHub highlight tags and hide short Xiaohongshu near-duplicates.

UPDATE campus_ingest_record
SET title = CASE WHEN title IS NULL THEN NULL ELSE TRIM(REPLACE(REPLACE(REPLACE(title, '<em class="keyword">', ''), '</em>', ''), '&nbsp;', ' ')) END,
    content = CASE WHEN content IS NULL THEN NULL ELSE TRIM(REPLACE(REPLACE(REPLACE(content, '<em class="keyword">', ''), '</em>', ''), '&nbsp;', ' ')) END,
    update_user_id = 0
WHERE deleted = 0
  AND (
      title LIKE '%<em class="keyword">%'
      OR content LIKE '%<em class="keyword">%'
      OR title LIKE '%&nbsp;%'
      OR content LIKE '%&nbsp;%'
  );

UPDATE campus_monitor_result
SET title = CASE WHEN title IS NULL THEN NULL ELSE TRIM(REPLACE(REPLACE(REPLACE(title, '<em class="keyword">', ''), '</em>', ''), '&nbsp;', ' ')) END,
    content = CASE WHEN content IS NULL THEN NULL ELSE TRIM(REPLACE(REPLACE(REPLACE(content, '<em class="keyword">', ''), '</em>', ''), '&nbsp;', ' ')) END,
    update_user_id = 0
WHERE deleted = 0
  AND (
      title LIKE '%<em class="keyword">%'
      OR content LIKE '%<em class="keyword">%'
      OR title LIKE '%&nbsp;%'
      OR content LIKE '%&nbsp;%'
  );

UPDATE campus_clue
SET clue_title = CASE WHEN clue_title IS NULL THEN NULL ELSE TRIM(REPLACE(REPLACE(REPLACE(clue_title, '<em class="keyword">', ''), '</em>', ''), '&nbsp;', ' ')) END,
    clue_content = CASE WHEN clue_content IS NULL THEN NULL ELSE TRIM(REPLACE(REPLACE(REPLACE(clue_content, '<em class="keyword">', ''), '</em>', ''), '&nbsp;', ' ')) END,
    update_user_id = 0
WHERE deleted = 0
  AND (
      clue_title LIKE '%<em class="keyword">%'
      OR clue_content LIKE '%<em class="keyword">%'
      OR clue_title LIKE '%&nbsp;%'
      OR clue_content LIKE '%&nbsp;%'
  );

UPDATE campus_monitor_result mr
JOIN campus_ingest_record ir
  ON ir.record_id = mr.ingest_record_id
 AND ir.deleted = 0
SET mr.content = ir.content,
    mr.title = CASE
        WHEN IFNULL(mr.title, '') = '' OR CHAR_LENGTH(IFNULL(mr.title, '')) < CHAR_LENGTH(IFNULL(ir.title, ''))
        THEN ir.title
        ELSE mr.title
    END,
    mr.original_url = CASE
        WHEN ir.original_url LIKE 'http%' AND (mr.original_url IS NULL OR mr.original_url = '' OR mr.original_url NOT LIKE 'http%')
        THEN ir.original_url
        ELSE mr.original_url
    END,
    mr.update_user_id = 0
WHERE mr.deleted = 0
  AND ir.content IS NOT NULL
  AND ir.content != ''
  AND (mr.content IS NULL OR mr.content = '' OR CHAR_LENGTH(mr.content) < CHAR_LENGTH(ir.content));

UPDATE campus_clue c
JOIN campus_ingest_record ir
  ON ir.target_type = 'clue'
 AND ir.target_id = c.clue_id
 AND ir.deleted = 0
SET c.clue_content = ir.content,
    c.clue_title = CASE
        WHEN IFNULL(c.clue_title, '') = '' OR CHAR_LENGTH(IFNULL(c.clue_title, '')) < CHAR_LENGTH(IFNULL(ir.title, ''))
        THEN ir.title
        ELSE c.clue_title
    END,
    c.original_url = CASE
        WHEN ir.original_url LIKE 'http%' AND (c.original_url IS NULL OR c.original_url = '' OR c.original_url NOT LIKE 'http%')
        THEN ir.original_url
        ELSE c.original_url
    END,
    c.update_user_id = 0
WHERE c.deleted = 0
  AND ir.content IS NOT NULL
  AND ir.content != ''
  AND (c.clue_content IS NULL OR c.clue_content = '' OR CHAR_LENGTH(c.clue_content) < CHAR_LENGTH(ir.content));

CREATE TEMPORARY TABLE tmp_xhs_short_duplicate_record_ids (
    record_id BIGINT PRIMARY KEY
);

INSERT IGNORE INTO tmp_xhs_short_duplicate_record_ids (record_id)
SELECT DISTINCT short_ir.record_id
FROM campus_ingest_record short_ir
JOIN campus_ingest_record long_ir
  ON long_ir.deleted = 0
 AND long_ir.source_id = short_ir.source_id
 AND long_ir.platform = short_ir.platform
 AND long_ir.title = short_ir.title
 AND long_ir.record_id != short_ir.record_id
 AND CHAR_LENGTH(IFNULL(long_ir.content, '')) >= CHAR_LENGTH(IFNULL(short_ir.content, '')) + 30
WHERE short_ir.deleted = 0
  AND short_ir.platform IN ('xiaohongshu', '小红书', 'red')
  AND IFNULL(short_ir.title, '') != '';

UPDATE campus_monitor_result mr
JOIN tmp_xhs_short_duplicate_record_ids d
  ON d.record_id = mr.ingest_record_id
SET mr.deleted = 1,
    mr.update_user_id = 0
WHERE mr.deleted = 0;

UPDATE campus_clue c
JOIN campus_ingest_record short_ir
  ON short_ir.target_type = 'clue'
 AND short_ir.target_id = c.clue_id
 AND short_ir.deleted = 0
JOIN tmp_xhs_short_duplicate_record_ids d
  ON d.record_id = short_ir.record_id
SET c.deleted = 1,
    c.update_user_id = 0
WHERE c.deleted = 0;

UPDATE campus_ingest_record short_ir
JOIN tmp_xhs_short_duplicate_record_ids d
  ON d.record_id = short_ir.record_id
SET short_ir.deleted = 1,
    short_ir.update_user_id = 0
WHERE short_ir.deleted = 0;

DROP TEMPORARY TABLE tmp_xhs_short_duplicate_record_ids;
