-- Correct monitor information platform and alert signal quality after unified monitor information rollout.

UPDATE campus_ingest_record r
JOIN campus_ingest_task t ON t.task_id = r.task_id AND t.deleted = 0
JOIN campus_ingest_source s ON s.source_id = t.source_id AND s.deleted = 0
SET r.platform = s.platform,
    r.update_time = CURRENT_TIMESTAMP
WHERE r.deleted = 0
  AND s.platform IS NOT NULL
  AND s.platform != ''
  AND (r.platform IS NULL OR r.platform = '' OR r.platform != s.platform);

UPDATE campus_monitor_result mr
JOIN campus_ingest_record r ON r.record_id = mr.ingest_record_id AND r.deleted = 0
SET mr.platform = r.platform,
    mr.publish_time = COALESCE(mr.publish_time, r.publish_time),
    mr.like_count = COALESCE(mr.like_count, r.like_count),
    mr.comment_count = COALESCE(mr.comment_count, r.comment_count),
    mr.share_count = COALESCE(mr.share_count, r.share_count),
    mr.collect_count = COALESCE(mr.collect_count, r.collect_count),
    mr.view_count = COALESCE(mr.view_count, r.view_count),
    mr.update_time = CURRENT_TIMESTAMP
WHERE mr.deleted = 0;

UPDATE campus_clue c
JOIN campus_ingest_record r
  ON r.deleted = 0
 AND r.target_type = 'clue'
 AND r.target_id = c.clue_id
SET c.source_platform = COALESCE(NULLIF(r.platform, ''), c.source_platform),
    c.original_url = COALESCE(NULLIF(c.original_url, ''), r.original_url),
    c.publish_time = COALESCE(c.publish_time, r.publish_time),
    c.involved_account = COALESCE(NULLIF(c.involved_account, ''), r.author_name),
    c.update_time = CURRENT_TIMESTAMP
WHERE c.deleted = 0;

UPDATE campus_alert a
JOIN campus_monitor_result mr
  ON a.alert_source = 'monitor'
 AND a.source_object_id = mr.monitor_result_id
 AND mr.deleted = 0
JOIN campus_monitor_task mt
  ON mt.monitor_task_id = mr.monitor_task_id
 AND mt.deleted = 0
SET a.alert_status = 'ignored',
    a.handle_opinion = '系统校正：普通中性命中不作为自动预警',
    a.handle_time = CURRENT_TIMESTAMP,
    a.deleted = 1,
    a.update_time = CURRENT_TIMESTAMP
WHERE a.deleted = 0
  AND mr.result_status = 'alerted'
  AND (
      (
        mt.alert_mode = 'all_hits'
        AND (mr.matched_negative_words IS NULL OR mr.matched_negative_words = '')
        AND LOWER(COALESCE(mr.sentiment, 'neutral')) IN ('', 'neutral')
        AND COALESCE(mr.risk_level, 'normal') = 'normal'
      )
      OR (
        mr.matched_negative_words IS NOT NULL
        AND mr.matched_negative_words != ''
        AND mr.matched_negative_words NOT LIKE '%,%'
        AND FIND_IN_SET(mr.matched_negative_words, REPLACE(COALESCE(mr.matched_keywords, ''), '，', ',')) > 0
        AND LOWER(COALESCE(mr.sentiment, 'negative')) = 'negative'
        AND COALESCE(mr.risk_level, 'normal') = 'concern'
      )
  );

UPDATE campus_monitor_result mr
JOIN campus_monitor_task mt
  ON mt.monitor_task_id = mr.monitor_task_id
 AND mt.deleted = 0
SET mr.result_status = 'pending',
    mr.alert_id = NULL,
    mr.sentiment = CASE
        WHEN mr.matched_negative_words IS NOT NULL
         AND mr.matched_negative_words != ''
         AND mr.matched_negative_words NOT LIKE '%,%'
         AND FIND_IN_SET(mr.matched_negative_words, REPLACE(COALESCE(mr.matched_keywords, ''), '，', ',')) > 0
        THEN 'neutral'
        ELSE mr.sentiment
    END,
    mr.risk_level = CASE
        WHEN mr.matched_negative_words IS NOT NULL
         AND mr.matched_negative_words != ''
         AND mr.matched_negative_words NOT LIKE '%,%'
         AND FIND_IN_SET(mr.matched_negative_words, REPLACE(COALESCE(mr.matched_keywords, ''), '，', ',')) > 0
        THEN 'normal'
        ELSE mr.risk_level
    END,
    mr.update_time = CURRENT_TIMESTAMP
WHERE mr.deleted = 0
  AND mr.result_status = 'alerted'
  AND (
      (
        mt.alert_mode = 'all_hits'
        AND (mr.matched_negative_words IS NULL OR mr.matched_negative_words = '')
        AND LOWER(COALESCE(mr.sentiment, 'neutral')) IN ('', 'neutral')
        AND COALESCE(mr.risk_level, 'normal') = 'normal'
      )
      OR (
        mr.matched_negative_words IS NOT NULL
        AND mr.matched_negative_words != ''
        AND mr.matched_negative_words NOT LIKE '%,%'
        AND FIND_IN_SET(mr.matched_negative_words, REPLACE(COALESCE(mr.matched_keywords, ''), '，', ',')) > 0
        AND LOWER(COALESCE(mr.sentiment, 'negative')) = 'negative'
        AND COALESCE(mr.risk_level, 'normal') = 'concern'
      )
  );
