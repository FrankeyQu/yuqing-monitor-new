-- 校园自动报告调度增强
-- 为自动报告任务增加调度/手动执行锁，防止多节点或重复扫描并发生成同一份报告。

ALTER TABLE `campus_report_job`
    ADD COLUMN `schedule_lock_until` datetime DEFAULT NULL COMMENT '调度或手动执行锁过期时间' AFTER `next_run_time`,
    ADD KEY `idx_campus_report_job_due` (`job_status`, `next_run_time`, `schedule_lock_until`, `deleted`);

UPDATE `campus_report_job`
SET `schedule_cron` = CASE
        WHEN `period_rule` = 'weekly' THEN '0 0 8 ? * MON'
        WHEN `period_rule` = 'monthly' THEN '0 0 8 1 * ?'
        ELSE '0 0 8 * * ?'
    END,
    `update_time` = CURRENT_TIMESTAMP
WHERE `deleted` = 0
  AND (`schedule_cron` IS NULL OR `schedule_cron` = '');

UPDATE `campus_report_job`
SET `next_run_time` = IFNULL(`next_run_time`, NOW()),
    `update_time` = CURRENT_TIMESTAMP
WHERE `deleted` = 0
  AND `job_status` = 'active';
