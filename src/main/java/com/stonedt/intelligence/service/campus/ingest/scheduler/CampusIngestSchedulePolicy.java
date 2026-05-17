package com.stonedt.intelligence.service.campus.ingest.scheduler;

import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.support.CronSequenceGenerator;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class CampusIngestSchedulePolicy {

    private static final long MIN_INTERVAL_MS = 5L * 60L * 1000L;
    private static final int DEFAULT_RETRY_INTERVAL_MINUTES = 10;

    public Date nextRunTime(String cron, Date from) {
        CronSequenceGenerator generator = newGenerator(cron);
        return generator.next(from == null ? new Date() : from);
    }

    public Date nextRetryTime(Date from, Integer retryIntervalMinutes) {
        int minutes = retryIntervalMinutes == null || retryIntervalMinutes <= 0
                ? DEFAULT_RETRY_INTERVAL_MINUTES
                : retryIntervalMinutes;
        return new Date((from == null ? System.currentTimeMillis() : from.getTime()) + minutes * 60L * 1000L);
    }

    public void validateCronForSchedule(String cron) {
        CronSequenceGenerator generator = newGenerator(cron);
        Date first = generator.next(new Date());
        Date second = generator.next(first);
        if (second.getTime() - first.getTime() < MIN_INTERVAL_MS) {
            throw new IllegalArgumentException("接入任务自动调度间隔不能小于5分钟");
        }
    }

    private CronSequenceGenerator newGenerator(String cron) {
        if (StringUtils.isBlank(cron)) {
            throw new IllegalArgumentException("启用自动调度时计划表达式不能为空");
        }
        try {
            return new CronSequenceGenerator(cron);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("计划表达式格式不正确: " + ex.getMessage());
        }
    }
}
