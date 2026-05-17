package com.stonedt.intelligence.service.campus;

import com.github.pagehelper.PageInfo;
import com.stonedt.intelligence.entity.campus.CampusAccountContent;
import com.stonedt.intelligence.entity.campus.CampusClue;
import com.stonedt.intelligence.entity.campus.CampusIngestApiCallLog;
import com.stonedt.intelligence.entity.campus.CampusIngestRecord;
import com.stonedt.intelligence.entity.campus.CampusIngestRunLog;
import com.stonedt.intelligence.entity.campus.CampusIngestSource;
import com.stonedt.intelligence.entity.campus.CampusIngestTask;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface CampusIngestService {

    CampusIngestSource saveSource(CampusIngestSource source, Long operatorUserId);

    void deleteSource(Long sourceId, Long operatorUserId);

    PageInfo<CampusIngestSource> listSources(Integer pageNum,
                                             Integer pageSize,
                                             String keyword,
                                             String sourceType,
                                             String platform,
                                             Integer enabled);

    CampusIngestTask saveTask(CampusIngestTask task, Long operatorUserId);

    CampusIngestTask updateTaskStatus(Long taskId, String taskStatus, Long operatorUserId);

    void deleteTask(Long taskId, Long operatorUserId);

    PageInfo<CampusIngestTask> listTasks(Integer pageNum,
                                         Integer pageSize,
                                         String keyword,
                                         Long sourceId,
                                         String targetType,
                                         String taskStatus);

    CampusIngestRecord submitRecord(CampusIngestRecord record, Long operatorUserId);

    PageInfo<CampusIngestRecord> listRecords(Integer pageNum,
                                             Integer pageSize,
                                             String keyword,
                                             Long sourceId,
                                             Long taskId,
                                             String normalizedStatus,
                                             String targetType,
                                             Date startTime,
                                             Date endTime);

    CampusClue convertRecordToClue(Long recordId, Long operatorUserId, String operatorName);

    CampusAccountContent convertRecordToAccountContent(Long recordId,
                                                       Long accountId,
                                                       Long operatorUserId);

    CampusIngestRunLog startRun(Long taskId, Long operatorUserId);

    CampusIngestRunLog runTask(Long taskId, Long operatorUserId);

    CampusIngestRunLog runScheduledTask(Long taskId,
                                        String triggerType,
                                        Integer retryCount,
                                        String schedulerNode);

    CampusIngestRunLog runScheduledTask(Long taskId,
                                        String triggerType,
                                        Integer retryCount,
                                        String schedulerNode,
                                        Date lockUntil);

    List<CampusIngestTask> listDueTasks(Date now, Integer limit);

    boolean acquireScheduleLock(Long taskId, Date now, Date lockUntil);

    void releaseScheduleLock(Long taskId);

    void releaseScheduleLock(Long taskId, Date lockUntil);

    Map<String, Integer> cleanupExpiredData(Integer recordRetentionDays,
                                            Integer runLogRetentionDays,
                                            Integer apiCallLogRetentionDays,
                                            Integer batchSize);

    CampusIngestRunLog finishRun(Long runId,
                                 String runStatus,
                                 Integer fetchedCount,
                                 Integer successCount,
                                 Integer failCount,
                                 String errorMessage,
                                 Long operatorUserId);

    List<CampusIngestRunLog> listRunLogs(Long taskId);

    PageInfo<CampusIngestRunLog> listRunLogPage(Integer pageNum,
                                                Integer pageSize,
                                                Long taskId,
                                                String runStatus,
                                                String errorType,
                                                String triggerType);

    List<CampusIngestApiCallLog> listApiCallLogs(Long taskId,
                                                 Long runId,
                                                 String provider,
                                                 String callStatus);
}
