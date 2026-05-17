package com.stonedt.intelligence.service.campus.ingest.scheduler;

import com.stonedt.intelligence.entity.campus.CampusIngestTask;
import com.stonedt.intelligence.service.campus.CampusIngestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
public class CampusIngestScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CampusIngestScheduler.class);

    private final CampusIngestService campusIngestService;

    @Value("${schedule.campus-ingest.open:0}")
    private String schedulerOpen;

    @Value("${schedule.campus-ingest.batch-size:5}")
    private Integer batchSize;

    @Value("${schedule.campus-ingest.lock-minutes:10}")
    private Integer lockMinutes;

    @Value("${schedule.campus-ingest.cleanup-open:1}")
    private String cleanupOpen;

    @Value("${schedule.campus-ingest.record-retention-days:180}")
    private Integer recordRetentionDays;

    @Value("${schedule.campus-ingest.run-log-retention-days:90}")
    private Integer runLogRetentionDays;

    @Value("${schedule.campus-ingest.api-call-log-retention-days:90}")
    private Integer apiCallLogRetentionDays;

    @Value("${schedule.campus-ingest.cleanup-batch-size:1000}")
    private Integer cleanupBatchSize;

    public CampusIngestScheduler(CampusIngestService campusIngestService) {
        this.campusIngestService = campusIngestService;
    }

    @Scheduled(fixedDelayString = "${schedule.campus-ingest.fixed-delay-ms:60000}")
    public void scanAndRunDueTasks() {
        if (!"1".equals(schedulerOpen)) {
            return;
        }
        Date now = new Date();
        List<CampusIngestTask> dueTasks = campusIngestService.listDueTasks(now, safeBatchSize());
        Date lockUntil = new Date(now.getTime() + safeLockMinutes() * 60L * 1000L);
        for (CampusIngestTask task : dueTasks) {
            if (task.getTaskId() == null) {
                continue;
            }
            if (!campusIngestService.acquireScheduleLock(task.getTaskId(), now, lockUntil)) {
                continue;
            }
            String triggerType = task.getCurrentRetryCount() != null && task.getCurrentRetryCount() > 0
                    ? CampusIngestRunContext.TRIGGER_RETRY
                    : CampusIngestRunContext.TRIGGER_SCHEDULE;
            try {
                campusIngestService.runScheduledTask(task.getTaskId(), triggerType,
                        task.getCurrentRetryCount(), schedulerNode(), lockUntil);
            } catch (Exception ex) {
                campusIngestService.releaseScheduleLock(task.getTaskId(), lockUntil);
                LOGGER.warn("校园接入调度任务执行失败，taskId={}", task.getTaskId(), ex);
            }
        }
    }

    @Scheduled(cron = "${schedule.campus-ingest.cleanup-cron:0 10 2 * * ?}")
    public void cleanupExpiredData() {
        if (!"1".equals(schedulerOpen) || !"1".equals(cleanupOpen)) {
            return;
        }
        try {
            Map<String, Integer> result = campusIngestService.cleanupExpiredData(
                    recordRetentionDays, runLogRetentionDays, apiCallLogRetentionDays, cleanupBatchSize);
            LOGGER.info("校园接入历史清理完成，expiredRecordCount={}, expiredRunLogCount={}, expiredApiCallLogCount={}",
                    result.get("expiredRecordCount"),
                    result.get("expiredRunLogCount"),
                    result.get("expiredApiCallLogCount"));
        } catch (Exception ex) {
            LOGGER.warn("校园接入历史清理失败", ex);
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

    private String schedulerNode() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception ex) {
            return "local";
        }
    }
}
