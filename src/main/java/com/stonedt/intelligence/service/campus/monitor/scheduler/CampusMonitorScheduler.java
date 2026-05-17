package com.stonedt.intelligence.service.campus.monitor.scheduler;

import com.stonedt.intelligence.entity.campus.CampusMonitorTask;
import com.stonedt.intelligence.service.campus.CampusMonitorService;
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
public class CampusMonitorScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CampusMonitorScheduler.class);

    private final CampusMonitorService campusMonitorService;

    @Value("${schedule.campus-monitor.open:0}")
    private String schedulerOpen;

    @Value("${schedule.campus-monitor.batch-size:5}")
    private Integer batchSize;

    @Value("${schedule.campus-monitor.lock-minutes:10}")
    private Integer lockMinutes;

    @Value("${schedule.campus-monitor.cleanup-open:1}")
    private String cleanupOpen;

    @Value("${schedule.campus-monitor.result-retention-days:180}")
    private Integer resultRetentionDays;

    @Value("${schedule.campus-monitor.run-log-retention-days:90}")
    private Integer runLogRetentionDays;

    @Value("${schedule.campus-monitor.cleanup-batch-size:1000}")
    private Integer cleanupBatchSize;

    public CampusMonitorScheduler(CampusMonitorService campusMonitorService) {
        this.campusMonitorService = campusMonitorService;
    }

    @Scheduled(fixedDelayString = "${schedule.campus-monitor.fixed-delay-ms:60000}")
    public void scanAndRunDueTasks() {
        if (!"1".equals(schedulerOpen)) {
            return;
        }
        Date now = new Date();
        List<CampusMonitorTask> dueTasks = campusMonitorService.listDueTasks(now, safeBatchSize());
        Date lockUntil = new Date(now.getTime() + safeLockMinutes() * 60L * 1000L);
        for (CampusMonitorTask task : dueTasks) {
            if (task.getMonitorTaskId() == null) {
                continue;
            }
            if (!campusMonitorService.acquireScheduleLock(task.getMonitorTaskId(), now, lockUntil)) {
                continue;
            }
            try {
                campusMonitorService.runScheduledTask(task.getMonitorTaskId(), schedulerNode(), lockUntil);
            } catch (Exception ex) {
                campusMonitorService.releaseScheduleLock(task.getMonitorTaskId(), lockUntil);
                LOGGER.warn("校园监测调度任务执行失败，monitorTaskId={}", task.getMonitorTaskId(), ex);
            }
        }
    }

    @Scheduled(cron = "${schedule.campus-monitor.cleanup-cron:0 30 2 * * ?}")
    public void cleanupExpiredData() {
        if (!"1".equals(schedulerOpen) || !"1".equals(cleanupOpen)) {
            return;
        }
        try {
            Map<String, Integer> result = campusMonitorService.cleanupExpiredData(
                    resultRetentionDays, runLogRetentionDays, cleanupBatchSize);
            LOGGER.info("校园监测历史清理完成，expiredResultCount={}, expiredRunLogCount={}",
                    result.get("expiredResultCount"), result.get("expiredRunLogCount"));
        } catch (Exception ex) {
            LOGGER.warn("校园监测历史清理失败", ex);
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
