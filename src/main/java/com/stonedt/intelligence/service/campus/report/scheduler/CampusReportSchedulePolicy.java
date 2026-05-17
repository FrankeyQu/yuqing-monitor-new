package com.stonedt.intelligence.service.campus.report.scheduler;

import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.support.CronSequenceGenerator;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class CampusReportSchedulePolicy {

    private static final long MIN_INTERVAL_MS = 5L * 60L * 1000L;

    public String defaultCron(String periodRule) {
        if ("weekly".equals(periodRule)) {
            return "0 0 8 ? * MON";
        }
        if ("monthly".equals(periodRule)) {
            return "0 0 8 1 * ?";
        }
        return "0 0 8 * * ?";
    }

    public Date nextRunTime(String cron, Date from) {
        return newGenerator(cron).next(from == null ? new Date() : from);
    }

    public void validateCronForSchedule(String cron) {
        CronSequenceGenerator generator = newGenerator(cron);
        Date first = generator.next(new Date());
        Date second = generator.next(first);
        if (second.getTime() - first.getTime() < MIN_INTERVAL_MS) {
            throw new IllegalArgumentException("自动报告调度间隔不能小于5分钟");
        }
    }

    private CronSequenceGenerator newGenerator(String cron) {
        if (StringUtils.isBlank(cron)) {
            throw new IllegalArgumentException("自动报告计划表达式不能为空");
        }
        try {
            return new CronSequenceGenerator(cron);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("自动报告计划表达式格式不正确: " + ex.getMessage());
        }
    }
}
