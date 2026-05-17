-- Batch 24: ingest normalization and dedup diagnostics.
-- Keeps campus_ingest_record compatible and only adds run-level counters.

ALTER TABLE `campus_ingest_run_log`
    ADD COLUMN `duplicate_count` int DEFAULT 0 COMMENT '去重跳过数量' AFTER `success_count`,
    ADD COLUMN `invalid_count` int DEFAULT 0 COMMENT '标准化无效数量' AFTER `duplicate_count`;
