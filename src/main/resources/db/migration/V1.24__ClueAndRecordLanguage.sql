-- 线索和接入记录增加语言字段，支持多语言舆情研判
-- V1.24: Multi-language support for clue & ingest record

ALTER TABLE `campus_clue`
    ADD COLUMN `language` varchar(16) DEFAULT NULL COMMENT '语言(zh/mongolian/uyghur)' AFTER `sentiment`;

CREATE INDEX `idx_campus_clue_language` ON `campus_clue` (`language`, `clue_status`);

ALTER TABLE `campus_ingest_record`
    ADD COLUMN `language` varchar(16) DEFAULT NULL COMMENT '语言(zh/mongolian/uyghur)' AFTER `sentiment`;
