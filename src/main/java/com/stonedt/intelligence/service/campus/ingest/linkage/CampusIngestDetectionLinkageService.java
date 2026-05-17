package com.stonedt.intelligence.service.campus.ingest.linkage;

import com.stonedt.intelligence.dao.campus.CampusDetectionTaskDao;
import com.stonedt.intelligence.entity.campus.CampusDetectionRunLog;
import com.stonedt.intelligence.entity.campus.CampusDetectionTask;
import com.stonedt.intelligence.entity.campus.CampusIngestRunLog;
import com.stonedt.intelligence.entity.campus.CampusIngestTask;
import com.stonedt.intelligence.service.campus.CampusDetectionService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.Set;

@Service
public class CampusIngestDetectionLinkageService {

    private static final String TASK_ACTIVE = "active";
    private static final String OBJECT_INGEST_RECORD = "ingest_record";

    private final CampusDetectionService campusDetectionService;
    private final CampusDetectionTaskDao campusDetectionTaskDao;

    public CampusIngestDetectionLinkageService(CampusDetectionService campusDetectionService,
                                               CampusDetectionTaskDao campusDetectionTaskDao) {
        this.campusDetectionService = campusDetectionService;
        this.campusDetectionTaskDao = campusDetectionTaskDao;
    }

    public CampusIngestDetectionLinkageResult linkAfterIngestRun(CampusIngestTask ingestTask,
                                                                 CampusIngestRunLog ingestRunLog,
                                                                 int insertedCount,
                                                                 Long operatorUserId) {
        CampusIngestDetectionLinkageResult result = new CampusIngestDetectionLinkageResult();
        if (ingestTask == null || ingestRunLog == null || insertedCount <= 0) {
            return result;
        }
        if (ingestTask.getAutoDetectEnabled() == null || ingestTask.getAutoDetectEnabled() != 1) {
            return result;
        }
        Set<Long> detectionTaskIds = parseTaskIds(ingestTask.getDetectionTaskIds(), result);
        if (detectionTaskIds.isEmpty()) {
            return result;
        }
        for (Long detectionTaskId : detectionTaskIds) {
            try {
                CampusDetectionTask detectionTask = campusDetectionTaskDao.selectByTaskId(detectionTaskId);
                if (!isRunnableIngestDetectionTask(detectionTask)) {
                    result.appendError("检测任务不可用于接入联动: " + detectionTaskId);
                    continue;
                }
                CampusDetectionRunLog runLog = campusDetectionService.runIngestRecordTask(
                        detectionTaskId, ingestRunLog.getRunId(), operatorUserId);
                result.addRunResult(runLog.getHitCount(), runLog.getAlertCount());
            } catch (RuntimeException ex) {
                result.appendError("检测任务 " + detectionTaskId + " 联动失败: " + ex.getMessage());
            }
        }
        return result;
    }

    private Set<Long> parseTaskIds(String raw, CampusIngestDetectionLinkageResult result) {
        Set<Long> ids = new LinkedHashSet<>();
        if (StringUtils.isBlank(raw)) {
            return ids;
        }
        String[] parts = raw.split("[,;，；\\n\\r\\t ]+");
        for (String part : parts) {
            if (StringUtils.isBlank(part)) {
                continue;
            }
            try {
                ids.add(Long.parseLong(part.trim()));
            } catch (NumberFormatException ex) {
                result.appendError("检测任务ID不合法: " + part);
            }
        }
        return ids;
    }

    private boolean isRunnableIngestDetectionTask(CampusDetectionTask task) {
        if (task == null || !TASK_ACTIVE.equals(task.getTaskStatus())) {
            return false;
        }
        Set<String> objectTypes = splitTokens(StringUtils.defaultIfBlank(task.getObjectTypes(), OBJECT_INGEST_RECORD));
        return objectTypes.contains(OBJECT_INGEST_RECORD);
    }

    private Set<String> splitTokens(String raw) {
        Set<String> tokens = new LinkedHashSet<>();
        if (StringUtils.isBlank(raw)) {
            return tokens;
        }
        String[] parts = raw.split("[,;，；\\n\\r\\t ]+");
        for (String part : parts) {
            if (StringUtils.isNotBlank(part)) {
                tokens.add(part.trim());
            }
        }
        return tokens;
    }
}
