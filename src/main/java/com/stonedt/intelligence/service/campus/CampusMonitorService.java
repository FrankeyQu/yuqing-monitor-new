package com.stonedt.intelligence.service.campus;

import com.github.pagehelper.PageInfo;
import com.stonedt.intelligence.entity.campus.CampusAlert;
import com.stonedt.intelligence.entity.campus.CampusClue;
import com.stonedt.intelligence.entity.campus.CampusMonitorInformation;
import com.stonedt.intelligence.entity.campus.CampusMonitorResult;
import com.stonedt.intelligence.entity.campus.CampusMonitorRunLog;
import com.stonedt.intelligence.entity.campus.CampusMonitorTask;
import com.stonedt.intelligence.entity.campus.CampusMonitorWatchTarget;
import com.stonedt.intelligence.dto.campus.CampusMonitorAiAnalyzeRequest;
import com.stonedt.intelligence.dto.campus.CampusMonitorAiAnalyzeResponse;
import com.stonedt.intelligence.dto.campus.CampusMonitorAlertCleanupPreview;
import com.stonedt.intelligence.dto.campus.CampusMonitorAlertCleanupRequest;
import com.stonedt.intelligence.dto.campus.CampusMonitorAlertCleanupResponse;
import com.stonedt.intelligence.dto.campus.CampusMonitorTaskAiDiagnosis;

import java.util.List;
import java.util.Map;
import java.util.Date;

public interface CampusMonitorService {

    CampusMonitorTask saveTask(CampusMonitorTask task, Long operatorUserId);

    CampusMonitorTask updateTaskStatus(Long monitorTaskId, String taskStatus, Long operatorUserId);

    CampusMonitorTask updateTaskDisplay(Long monitorTaskId, Integer displayEnabled, Long operatorUserId);

    void deleteTask(Long monitorTaskId, Long operatorUserId);

    PageInfo<CampusMonitorTask> listTasks(Integer pageNum,
                                          Integer pageSize,
                                          String keyword,
                                          String taskStatus,
                                          String platform);

    CampusMonitorRunLog runTask(Long monitorTaskId, Long operatorUserId);

    CampusMonitorTaskAiDiagnosis diagnoseTask(Long monitorTaskId, Long operatorUserId);

    CampusMonitorRunLog runScheduledTask(Long monitorTaskId, String schedulerNode);

    CampusMonitorRunLog runScheduledTask(Long monitorTaskId, String schedulerNode, Date lockUntil);

    List<CampusMonitorTask> listDueTasks(Date now, Integer limit);

    boolean acquireScheduleLock(Long monitorTaskId, Date now, Date lockUntil);

    void releaseScheduleLock(Long monitorTaskId);

    void releaseScheduleLock(Long monitorTaskId, Date lockUntil);

    Map<String, Integer> cleanupExpiredData(Integer resultRetentionDays,
                                            Integer runLogRetentionDays,
                                            Integer batchSize);

    PageInfo<CampusMonitorResult> listResults(Integer pageNum,
                                               Integer pageSize,
                                               Long monitorTaskId,
                                              String keyword,
                                              String riskLevel,
                                              String resultStatus,
                                              String platform,
                                              String language,
                                              Boolean converted);

    PageInfo<CampusMonitorInformation> listInformation(Integer pageNum,
                                                       Integer pageSize,
                                                       String keyword,
                                                       Long monitorTaskId,
                                                       String sourcePlatform,
                                                       String sourceSubPlatform,
                                                       String riskLevel,
                                                       String clueStatus,
                                                       String language,
                                                       String sentiment,
                                                       String resultStatus,
                                                       Date publishTimeStart,
                                                       Date publishTimeEnd,
                                                       Date collectTimeStart,
                                                       Date collectTimeEnd,
                                                       String matchScope,
                                                       Boolean similarDedup,
                                                       String hitScope,
                                                       String sortBy);

    List<Map<String, Object>> countInformationByPlatform(String keyword,
                                                         Long monitorTaskId,
                                                         String sourcePlatform,
                                                         String sourceSubPlatform,
                                                         String riskLevel,
                                                         String clueStatus,
                                                         String language,
                                                         String sentiment,
                                                         String resultStatus,
                                                         Date publishTimeStart,
                                                         Date publishTimeEnd,
                                                         Date collectTimeStart,
                                                         Date collectTimeEnd,
                                                         String matchScope,
                                                         Boolean similarDedup,
                                                         String hitScope);

    List<Map<String, Object>> countInformationBySubPlatform(String keyword,
                                                            Long monitorTaskId,
                                                            String sourcePlatform,
                                                            String sourceSubPlatform,
                                                            String riskLevel,
                                                            String clueStatus,
                                                            String language,
                                                            String sentiment,
                                                            String resultStatus,
                                                            Date publishTimeStart,
                                                            Date publishTimeEnd,
                                                            Date collectTimeStart,
                                                            Date collectTimeEnd,
                                                            String matchScope,
                                                            Boolean similarDedup,
                                                            String hitScope);

    CampusMonitorResult alertResult(Long monitorResultId, Long operatorUserId);

    CampusMonitorResult ignoreResult(Long monitorResultId, Long operatorUserId);

    CampusMonitorResult updateResultSentiment(Long monitorResultId,
                                               String sentiment,
                                               Long operatorUserId,
                                               String operatorName);

    CampusMonitorAiAnalyzeResponse analyzeResults(CampusMonitorAiAnalyzeRequest request,
                                                  Long operatorUserId,
                                                  String operatorName);

    CampusMonitorAlertCleanupPreview previewAlertCleanupCandidates(Integer limit);

    CampusMonitorAlertCleanupResponse cleanupAlertCandidates(CampusMonitorAlertCleanupRequest request,
                                                            Long operatorUserId);

    CampusClue convertResultToClue(Long monitorResultId, Long operatorUserId, String operatorName);

    PageInfo<CampusMonitorWatchTarget> listWatchTargets(Integer pageNum,
                                                        Integer pageSize,
                                                        Long monitorTaskId,
                                                        String targetType,
                                                        String platform,
                                                        String keyword,
                                                        String targetStatus);

    CampusMonitorWatchTarget saveWatchTarget(CampusMonitorWatchTarget target, Long operatorUserId);

    CampusMonitorWatchTarget createWatchTargetFromResult(Long monitorResultId,
                                                         Long monitorTaskId,
                                                         String targetType,
                                                         Long operatorUserId);

    void deleteWatchTarget(Long targetId, Long operatorUserId);

    PageInfo<CampusAlert> listAlerts(Integer pageNum,
                                     Integer pageSize,
                                     Long monitorTaskId,
                                     String keyword,
                                     String riskLevel,
                                     String alertStatus);

    CampusAlert handleAlert(Long alertId,
                            String alertStatus,
                            String handleOpinion,
                            Long operatorUserId);

    PageInfo<CampusMonitorRunLog> listRunLogs(Integer pageNum,
                                              Integer pageSize,
                                              Long monitorTaskId);

    Map<String, Integer> overview();
}
