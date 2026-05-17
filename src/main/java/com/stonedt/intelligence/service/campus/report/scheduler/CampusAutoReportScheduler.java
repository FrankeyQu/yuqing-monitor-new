package com.stonedt.intelligence.service.campus.report.scheduler;

import com.stonedt.intelligence.entity.campus.CampusReportJob;
import com.stonedt.intelligence.service.campus.CampusAutoReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class CampusAutoReportScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CampusAutoReportScheduler.class);
    private static final Long SYSTEM_USER_ID = 0L;

    private final CampusAutoReportService campusAutoReportService;

    @Value("${schedule.campus-report.open:0}")
    private String schedulerOpen;

    @Value("${schedule.campus-report.batch-size:5}")
    private Integer batchSize;

    @Value("${schedule.campus-report.lock-minutes:10}")
    private Integer lockMinutes;

    public CampusAutoReportScheduler(CampusAutoReportService campusAutoReportService) {
        this.campusAutoReportService = campusAutoReportService;
    }

    @Scheduled(fixedDelayString = "${schedule.campus-report.fixed-delay-ms:60000}")
    public void scanAndRunDueJobs() {
        if (!"1".equals(schedulerOpen)) {
            return;
        }
        Date now = new Date();
        List<CampusReportJob> dueJobs = campusAutoReportService.listDueJobs(now, safeBatchSize());
        Date lockUntil = new Date(now.getTime() + safeLockMinutes() * 60L * 1000L);
        for (CampusReportJob job : dueJobs) {
            if (job.getReportJobId() == null) {
                continue;
            }
            if (!campusAutoReportService.acquireScheduleLock(job.getReportJobId(), now, lockUntil, SYSTEM_USER_ID)) {
                continue;
            }
            try {
                campusAutoReportService.runScheduledJob(job.getReportJobId(), SYSTEM_USER_ID);
            } catch (Exception ex) {
                campusAutoReportService.releaseScheduleLock(job.getReportJobId(), SYSTEM_USER_ID);
                LOGGER.warn("校园自动报告调度执行失败，reportJobId={}", job.getReportJobId(), ex);
            }
        }
    }

    private int safeBatchSize() {
        if (batchSize == null || batchSize < 1) {
            return 5;
        }
        return Math.min(batchSize, 20);
    }

    private int safeLockMinutes() {
        if (lockMinutes == null || lockMinutes < 1) {
            return 10;
        }
        return Math.min(lockMinutes, 60);
    }
}
