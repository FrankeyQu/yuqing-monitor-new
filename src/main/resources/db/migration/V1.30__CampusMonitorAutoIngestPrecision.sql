-- Batch 35: prevent monitor auto-ingest search results from entering clue/information lists before rule matching.

UPDATE `campus_ingest_task`
SET `target_type` = 'monitor_scan',
    `update_time` = CURRENT_TIMESTAMP
WHERE `deleted` = 0
  AND `task_name` LIKE '自动监测-%'
  AND `target_type` = 'clue';
