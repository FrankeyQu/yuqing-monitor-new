package com.stonedt.intelligence.service.impl.campus;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.stonedt.intelligence.dao.campus.CampusAlertDao;
import com.stonedt.intelligence.dao.campus.CampusDictDao;
import com.stonedt.intelligence.dao.campus.CampusIngestRecordDao;
import com.stonedt.intelligence.dao.campus.CampusIngestSourceDao;
import com.stonedt.intelligence.dao.campus.CampusIngestTaskDao;
import com.stonedt.intelligence.dao.campus.CampusMonitorIngestTaskRelationDao;
import com.stonedt.intelligence.dao.campus.CampusMonitorResultDao;
import com.stonedt.intelligence.dao.campus.CampusMonitorRunLogDao;
import com.stonedt.intelligence.dao.campus.CampusMonitorTaskDao;
import com.stonedt.intelligence.dao.campus.CampusMonitorWatchTargetDao;
import com.stonedt.intelligence.dto.campus.CampusMonitorAiAnalyzeRequest;
import com.stonedt.intelligence.dto.campus.CampusMonitorAiAnalyzeResponse;
import com.stonedt.intelligence.dto.campus.CampusMonitorAlertCleanupCandidate;
import com.stonedt.intelligence.dto.campus.CampusMonitorAlertCleanupPreview;
import com.stonedt.intelligence.dto.campus.CampusMonitorAlertCleanupRequest;
import com.stonedt.intelligence.dto.campus.CampusMonitorAlertCleanupResponse;
import com.stonedt.intelligence.dto.campus.CampusMonitorTaskAiDiagnosis;
import com.stonedt.intelligence.entity.campus.CampusAiPromptTemplate;
import com.stonedt.intelligence.entity.campus.CampusAlert;
import com.stonedt.intelligence.entity.campus.CampusClue;
import com.stonedt.intelligence.entity.campus.CampusDictItem;
import com.stonedt.intelligence.entity.campus.CampusIngestRecord;
import com.stonedt.intelligence.entity.campus.CampusIngestRunLog;
import com.stonedt.intelligence.entity.campus.CampusIngestSource;
import com.stonedt.intelligence.entity.campus.CampusIngestTask;
import com.stonedt.intelligence.entity.campus.CampusMonitorInformation;
import com.stonedt.intelligence.entity.campus.CampusMonitorResult;
import com.stonedt.intelligence.entity.campus.CampusMonitorRunLog;
import com.stonedt.intelligence.entity.campus.CampusMonitorTask;
import com.stonedt.intelligence.entity.campus.CampusMonitorWatchTarget;
import com.stonedt.intelligence.service.campus.CampusAlertService;
import com.stonedt.intelligence.service.campus.CampusClueService;
import com.stonedt.intelligence.service.campus.CampusIngestService;
import com.stonedt.intelligence.service.campus.CampusMonitorService;
import com.stonedt.intelligence.service.campus.ai.CampusAiChatRequest;
import com.stonedt.intelligence.service.campus.ai.CampusAiChatResponse;
import com.stonedt.intelligence.service.campus.ai.CampusAiChatService;
import com.stonedt.intelligence.service.campus.ai.CampusAiRuntimeConfig;
import com.stonedt.intelligence.service.campus.ai.CampusAiRuntimeService;
import com.stonedt.intelligence.service.campus.support.CampusRiskLevel;
import com.stonedt.intelligence.service.campus.support.CampusSchoolRelevance;
import com.stonedt.intelligence.service.campus.support.CampusSchoolRelevanceService;
import com.stonedt.intelligence.service.campus.support.CampusSentimentNormalizer;
import com.stonedt.intelligence.service.campus.support.CampusTopicClassification;
import com.stonedt.intelligence.service.campus.support.CampusTopicClassifier;
import com.stonedt.intelligence.util.SnowflakeUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class CampusMonitorServiceImpl implements CampusMonitorService {

    private static final String TASK_ACTIVE = "active";
    private static final String TASK_PAUSED = "paused";
    private static final String TASK_DISABLED = "disabled";
    private static final String ALERT_MODE_NEGATIVE_ONLY = "negative_only";
    private static final String ALERT_MODE_ALL_HITS = "all_hits";
    private static final String ALERT_MODE_MANUAL = "manual";
    private static final String RESULT_PENDING = "pending";
    private static final String RESULT_ALERTED = "alerted";
    private static final String RESULT_IGNORED = "ignored";
    private static final String RESULT_HANDLED = "handled";
    private static final String RESULT_CONVERTED = "converted";
    private static final String INGEST_STATUS_CONVERTED = "converted";
    private static final String INGEST_TARGET_CLUE = "clue";
    private static final String INGEST_TARGET_MONITOR_SCAN = "monitor_scan";
    private static final String INGEST_TASK_ACTIVE = "active";
    private static final String AUTO_INGEST_READY = "ready";
    private static final String AUTO_INGEST_PARTIAL = "partial";
    private static final String AUTO_INGEST_UNSUPPORTED = "unsupported";
    private static final String AUTO_INGEST_FAILED = "failed";
    private static final String AUTO_INGEST_PENDING = "pending";
    private static final String RUN_RUNNING = "running";
    private static final String RUN_SUCCESS = "success";
    private static final String RUN_FAILED = "failed";
    private static final String TRIGGER_MANUAL = "manual";
    private static final String TRIGGER_SCHEDULE = "schedule";
    private static final String RISK_NORMAL = CampusRiskLevel.normalCode();
    private static final String RISK_CONCERN = CampusRiskLevel.concernCode();
    private static final String RISK_MAJOR = "major";
    private static final String RISK_URGENT = "urgent";
    private static final String ALERT_SOURCE_MONITOR = "monitor";
    private static final String WATCH_TARGET_ACCOUNT = "account";
    private static final String WATCH_TARGET_LINK = "link";
    private static final String WATCH_STATUS_ACTIVE = "active";
    private static final String DICT_NEGATIVE_WORD = "campus_negative_word";
    private static final String DICT_RISK_WORD = "campus_risk_word";
    private static final String FEATURE_MONITOR_RESULT_ANALYSIS = "monitor_result_analysis";
    private static final String FEATURE_MONITOR_TASK_DIAGNOSIS = "monitor_task_diagnosis";
    private static final String AI_STATUS_NONE = "none";
    private static final String AI_STATUS_PENDING = "pending";
    private static final String AI_STATUS_PROCESSING = "processing";
    private static final String AI_STATUS_DONE = "done";
    private static final String AI_STATUS_FAILED = "failed";
    private static final String AI_TRIGGER_AUTO = "auto";
    private static final String AI_TRIGGER_MANUAL = "manual";
    private static final String ALERT_CLEANUP_CONFIRM_TEXT = "确认取消预警";
    private static final int ALERT_CLEANUP_PREVIEW_MAX = 50;
    private static final int ALERT_CLEANUP_EXECUTE_MAX = 500;
    private static final Long SYSTEM_USER_ID = 0L;
    private static final int DEFAULT_LOCK_MINUTES = 10;
    private static final int DEFAULT_SCAN_FREQUENCY_MINUTES = 60;
    private static final int MIN_SCAN_FREQUENCY_MINUTES = 5;
    private static final int DEFAULT_SCAN_OVERLAP_MINUTES = 5;
    private static final int DEFAULT_INITIAL_SCAN_WINDOW_HOURS = 24;
    private static final int DEFAULT_MAX_SCAN_WINDOW_HOURS = 24;
    private static final int DEFAULT_RESULT_RETENTION_DAYS = 180;
    private static final int DEFAULT_RUN_LOG_RETENTION_DAYS = 90;
    private static final int DEFAULT_CLEANUP_BATCH_SIZE = 1000;
    private static final int MAX_CLEANUP_BATCH_SIZE = 5000;
    private static final int MAX_CLEANUP_BATCHES = 20;
    private static final int DEFAULT_AI_ANALYZE_LIMIT = 20;
    private static final int MAX_AI_ANALYZE_LIMIT = 20;
    private static final String AI_ANALYSIS_BASIS_CONTENT = "content";
    private static final String AI_ANALYSIS_BASIS_TITLE = "title";
    private static final String AI_ANALYSIS_BASIS_NONE = "none";
    private static final int MIN_AI_CONTENT_TEXT_LENGTH = 20;
    private static final String[] AI_CONTENT_NOISE_TERMS = new String[]{
            "打开app", "打开APP", "登录", "注册", "点击查看", "查看全文", "展开全文",
            "评论", "转发", "点赞", "分享", "复制链接", "搜索", "关注", "发布于", "来自"
    };

    private final CampusSchoolRelevanceService schoolRelevanceService = new CampusSchoolRelevanceService();
    private final CampusTopicClassifier topicClassifier = new CampusTopicClassifier();

    @Value("${schedule.campus-monitor.scan-overlap-minutes:5}")
    private Integer scanOverlapMinutes;

    @Value("${schedule.campus-monitor.initial-scan-window-hours:24}")
    private Integer initialScanWindowHours;

    @Value("${schedule.campus-monitor.max-scan-window-hours:24}")
    private Integer maxScanWindowHours;

    private final CampusMonitorTaskDao campusMonitorTaskDao;
    private final CampusMonitorIngestTaskRelationDao campusMonitorIngestTaskRelationDao;
    private final CampusMonitorResultDao campusMonitorResultDao;
    private final CampusMonitorRunLogDao campusMonitorRunLogDao;
    private final CampusMonitorWatchTargetDao campusMonitorWatchTargetDao;
    private final CampusIngestRecordDao campusIngestRecordDao;
    private final CampusIngestSourceDao campusIngestSourceDao;
    private final CampusIngestTaskDao campusIngestTaskDao;
    private final CampusDictDao campusDictDao;
    private final CampusAlertDao campusAlertDao;
    private final CampusIngestService campusIngestService;
    private final CampusAlertService campusAlertService;
    private final CampusClueService campusClueService;
    private final CampusAiChatService campusAiChatService;
    private final CampusAiRuntimeService campusAiRuntimeService;
    private final TransactionTemplate transactionTemplate;

    public CampusMonitorServiceImpl(CampusMonitorTaskDao campusMonitorTaskDao,
                                    CampusMonitorIngestTaskRelationDao campusMonitorIngestTaskRelationDao,
                                    CampusMonitorResultDao campusMonitorResultDao,
                                    CampusMonitorRunLogDao campusMonitorRunLogDao,
                                    CampusMonitorWatchTargetDao campusMonitorWatchTargetDao,
                                    CampusIngestRecordDao campusIngestRecordDao,
                                    CampusIngestSourceDao campusIngestSourceDao,
                                    CampusIngestTaskDao campusIngestTaskDao,
                                    CampusDictDao campusDictDao,
                                    CampusAlertDao campusAlertDao,
                                    CampusIngestService campusIngestService,
                                    CampusAlertService campusAlertService,
                                    CampusClueService campusClueService,
                                    CampusAiChatService campusAiChatService,
                                    CampusAiRuntimeService campusAiRuntimeService,
                                    PlatformTransactionManager transactionManager) {
        this.campusMonitorTaskDao = campusMonitorTaskDao;
        this.campusMonitorIngestTaskRelationDao = campusMonitorIngestTaskRelationDao;
        this.campusMonitorResultDao = campusMonitorResultDao;
        this.campusMonitorRunLogDao = campusMonitorRunLogDao;
        this.campusMonitorWatchTargetDao = campusMonitorWatchTargetDao;
        this.campusIngestRecordDao = campusIngestRecordDao;
        this.campusIngestSourceDao = campusIngestSourceDao;
        this.campusIngestTaskDao = campusIngestTaskDao;
        this.campusDictDao = campusDictDao;
        this.campusAlertDao = campusAlertDao;
        this.campusIngestService = campusIngestService;
        this.campusAlertService = campusAlertService;
        this.campusClueService = campusClueService;
        this.campusAiChatService = campusAiChatService;
        this.campusAiRuntimeService = campusAiRuntimeService;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    @Transactional
    public CampusMonitorTask saveTask(CampusMonitorTask task, Long operatorUserId) {
        validateTask(task);
        rejectForbiddenText(joinText(task.getMonitorSubject(), task.getSubjectAliases(), task.getKeywords(),
                task.getKeywordsI18n(), task.getNegativeWords(), task.getNegativeWordsI18n(),
                task.getExcludeWords(), task.getExcludeWordsI18n(), task.getPlatformScope(), task.getRemark()));
        if (task.getMonitorTaskId() == null && task.getId() == null) {
            task.setMonitorTaskId(SnowflakeUtil.getId());
            task.setCreateUserId(operatorUserId);
            task.setUpdateUserId(operatorUserId);
            setTaskDefaults(task);
            campusMonitorTaskDao.insert(task);
            syncTaskIngestScope(task, operatorUserId, false);
            return campusMonitorTaskDao.selectByTaskId(task.getMonitorTaskId());
        }
        CampusMonitorTask existing = resolveTaskForSave(task);
        task.setMonitorTaskId(existing.getMonitorTaskId());
        task.setUpdateUserId(operatorUserId);
        setTaskDefaults(task);
        campusMonitorTaskDao.update(task);
        syncTaskIngestScope(task, operatorUserId, false);
        return campusMonitorTaskDao.selectByTaskId(task.getMonitorTaskId());
    }

    @Override
    public CampusMonitorTask updateTaskStatus(Long monitorTaskId, String taskStatus, Long operatorUserId) {
        requireTask(monitorTaskId);
        validateTaskStatus(taskStatus);
        campusMonitorTaskDao.updateStatus(monitorTaskId, taskStatus, operatorUserId);
        CampusMonitorTask saved = campusMonitorTaskDao.selectByTaskId(monitorTaskId);
        if (saved != null && TASK_ACTIVE.equals(saved.getTaskStatus())
                && saved.getScheduleEnabled() != null && saved.getScheduleEnabled() == 1
                && saved.getNextRunTime() == null) {
            campusMonitorTaskDao.updateNextRunTime(monitorTaskId, new Date(), operatorUserId);
            saved = campusMonitorTaskDao.selectByTaskId(monitorTaskId);
        }
        return saved;
    }

    @Override
    public CampusMonitorTask updateTaskDisplay(Long monitorTaskId, Integer displayEnabled, Long operatorUserId) {
        requireTask(monitorTaskId);
        int enabled = displayEnabled != null && displayEnabled == 1 ? 1 : 0;
        campusMonitorTaskDao.updateDisplayEnabled(monitorTaskId, enabled, operatorUserId);
        return campusMonitorTaskDao.selectByTaskId(monitorTaskId);
    }

    @Override
    public void deleteTask(Long monitorTaskId, Long operatorUserId) {
        requireTask(monitorTaskId);
        campusMonitorTaskDao.logicalDelete(monitorTaskId, operatorUserId);
        campusMonitorIngestTaskRelationDao.softDeleteAll(monitorTaskId, operatorUserId);
    }

    @Override
    public PageInfo<CampusMonitorTask> listTasks(Integer pageNum,
                                                Integer pageSize,
                                                String keyword,
                                                String taskStatus,
                                                String platform) {
        PageHelper.startPage(defaultPageNum(pageNum), defaultPageSize(pageSize));
        return new PageInfo<>(campusMonitorTaskDao.list(keyword, taskStatus, platform));
    }

    @Override
    public CampusMonitorRunLog runTask(Long monitorTaskId, Long operatorUserId) {
        CampusMonitorTask task = requireTask(monitorTaskId);
        if (TASK_DISABLED.equals(task.getTaskStatus())) {
            throw new IllegalArgumentException("监测任务已禁用，不能运行");
        }
        Date now = new Date();
        Date lockUntil = new Date(now.getTime() + DEFAULT_LOCK_MINUTES * 60L * 1000L);
        if (campusMonitorTaskDao.acquireExecutionLock(monitorTaskId, now, lockUntil, operatorUserId) != 1) {
            throw new IllegalStateException("监测任务正在运行，请稍后再试");
        }
        try {
            return runTaskInternal(task, operatorUserId, TRIGGER_MANUAL, null, true, lockUntil);
        } catch (RuntimeException ex) {
            campusMonitorTaskDao.releaseScheduleLockBefore(monitorTaskId, lockUntil);
            throw ex;
        }
    }

    @Override
    public CampusMonitorTaskAiDiagnosis diagnoseTask(Long monitorTaskId, Long operatorUserId) {
        CampusMonitorTask task = requireTask(monitorTaskId);
        List<CampusMonitorResult> recentResults = campusMonitorResultDao.listRecentForAi(monitorTaskId, DEFAULT_AI_ANALYZE_LIMIT);
        JSONObject taskJson = buildTaskDiagnosisJson(task);
        JSONObject statsJson = buildTaskDiagnosisStats(recentResults);
        Map<String, String> variables = new HashMap<>();
        variables.put("taskJson", taskJson.toJSONString());
        variables.put("statsJson", statsJson.toJSONString());

        CampusAiRuntimeConfig config = resolveAiConfig(FEATURE_MONITOR_TASK_DIAGNOSIS);
        CampusAiPromptTemplate prompt = campusAiRuntimeService.getActivePrompt(FEATURE_MONITOR_TASK_DIAGNOSIS);
        String systemPrompt = StringUtils.defaultIfBlank(prompt == null ? null : prompt.getSystemPrompt(),
                "你是校园舆情监测任务配置顾问。请只返回JSON，不要展示具体采集内容。");
        String userPrompt = applyPromptTemplate(StringUtils.defaultIfBlank(prompt == null ? null : prompt.getUserPrompt(),
                "请体检以下监测任务配置和近期统计，只给配置建议，不修改配置。任务：${taskJson}。近期统计：${statsJson}。"),
                variables);

        CampusAiChatRequest chatRequest = new CampusAiChatRequest();
        chatRequest.setFeatureCode(FEATURE_MONITOR_TASK_DIAGNOSIS);
        chatRequest.setSystemPrompt(systemPrompt);
        chatRequest.setUserPrompt(userPrompt);
        chatRequest.setMaxTokens(2048);
        chatRequest.setTemperature(new BigDecimal("0.10"));
        CampusAiChatResponse chatResponse = campusAiChatService.chat(chatRequest);
        String content = chatResponse == null ? null : chatResponse.getContent();
        CampusMonitorTaskAiDiagnosis diagnosis = parseTaskDiagnosis(content);
        diagnosis.setMonitorTaskId(task.getMonitorTaskId());
        diagnosis.setTaskName(task.getTaskName());
        diagnosis.setProviderCode(config == null ? null : config.getProviderCode());
        diagnosis.setModelCode(config == null ? null : config.getModelCode());
        return diagnosis;
    }

    @Override
    public CampusMonitorRunLog runScheduledTask(Long monitorTaskId, String schedulerNode) {
        return runScheduledTask(monitorTaskId, schedulerNode, null);
    }

    @Override
    public CampusMonitorRunLog runScheduledTask(Long monitorTaskId, String schedulerNode, Date lockUntil) {
        CampusMonitorTask task = requireTask(monitorTaskId);
        if (!TASK_ACTIVE.equals(task.getTaskStatus()) || task.getScheduleEnabled() == null || task.getScheduleEnabled() != 1) {
            throw new IllegalArgumentException("监测任务未启用自动扫描");
        }
        return runTaskInternal(task, SYSTEM_USER_ID, TRIGGER_SCHEDULE, schedulerNode, false, lockUntil);
    }

    @Override
    public List<CampusMonitorTask> listDueTasks(Date now, Integer limit) {
        return campusMonitorTaskDao.listDueTasks(now == null ? new Date() : now,
                limit == null || limit < 1 ? 5 : Math.min(limit, 20));
    }

    @Override
    public boolean acquireScheduleLock(Long monitorTaskId, Date now, Date lockUntil) {
        return campusMonitorTaskDao.acquireScheduleLock(monitorTaskId,
                now == null ? new Date() : now,
                lockUntil == null ? new Date(System.currentTimeMillis() + DEFAULT_LOCK_MINUTES * 60L * 1000L) : lockUntil) == 1;
    }

    @Override
    public void releaseScheduleLock(Long monitorTaskId) {
        if (monitorTaskId != null) {
            campusMonitorTaskDao.releaseScheduleLock(monitorTaskId);
        }
    }

    @Override
    public void releaseScheduleLock(Long monitorTaskId, Date lockUntil) {
        if (monitorTaskId != null) {
            campusMonitorTaskDao.releaseScheduleLockBefore(monitorTaskId, lockUntil);
        }
    }

    @Override
    public Map<String, Integer> cleanupExpiredData(Integer resultRetentionDays,
                                                   Integer runLogRetentionDays,
                                                   Integer batchSize) {
        Map<String, Integer> cleanup = new HashMap<>();
        int safeBatchSize = safeCleanupBatchSize(batchSize);
        cleanup.put("expiredResultCount", cleanupExpiredResults(resultRetentionDays, safeBatchSize));
        cleanup.put("expiredRunLogCount", cleanupExpiredRunLogs(runLogRetentionDays, safeBatchSize));
        return cleanup;
    }

    private CampusMonitorRunLog runTaskInternal(CampusMonitorTask task,
                                                Long operatorUserId,
                                                String triggerType,
                                                String schedulerNode,
                                                boolean manual,
                                                Date lockUntil) {
        Long monitorTaskId = task.getMonitorTaskId();

        Long runLogId = SnowflakeUtil.getId();
        CampusMonitorRunLog runLog = new CampusMonitorRunLog();
        runLog.setRunLogId(runLogId);
        runLog.setMonitorTaskId(monitorTaskId);
        runLog.setRunStatus(RUN_RUNNING);
        runLog.setTriggerType(StringUtils.defaultIfBlank(triggerType, TRIGGER_MANUAL));
        runLog.setStartTime(new Date());
        runLog.setScannedCount(0);
        runLog.setMatchCount(0);
        runLog.setNegativeCount(0);
        runLog.setAlertCount(0);
        runLog.setSchedulerNode(schedulerNode);
        runLog.setCreateUserId(operatorUserId);
        campusMonitorRunLogDao.insert(runLog);

        MonitorCounter counter = new MonitorCounter();
        AutoIngestOutcome autoOutcome = AutoIngestOutcome.pending();
        try {
            autoOutcome = syncTaskIngestScope(task, operatorUserId, true);
            Date endTime = new Date();
            Date startTime = resolveStartTime(task, endTime);
            List<Long> boundTaskIds = campusMonitorIngestTaskRelationDao.listIngestTaskIds(monitorTaskId);
            List<CampusMonitorWatchTarget> watchTargets = campusMonitorWatchTargetDao.listActiveByTask(monitorTaskId);
            List<CampusIngestRecord> records;
            if (boundTaskIds == null || boundTaskIds.isEmpty()) {
                records = isAutoIngestEnabled(task) ? new ArrayList<CampusIngestRecord>() : campusIngestRecordDao.listForDetection(startTime, endTime);
            } else {
                records = campusIngestRecordDao.listForDetectionByTaskIds(startTime, endTime, boundTaskIds);
            }
            for (CampusIngestRecord record : records) {
                scanRecord(task, record, watchTargets, operatorUserId, counter);
            }
            campusMonitorRunLogDao.finish(runLogId, RUN_SUCCESS, counter.scannedCount,
                    counter.matchCount, counter.negativeCount, counter.alertCount, null);
            campusMonitorTaskDao.updateRunSummary(monitorTaskId, runLogId, counter.matchCount,
                    autoOutcome.getLastCollectTime(), autoOutcome.getStatus(),
                    autoOutcome.success() ? null : summary(autoOutcome.errorMessage(), 1024), operatorUserId);
            completeTaskAfterRun(task, runLogId, true, operatorUserId, manual, lockUntil);
            return campusMonitorRunLogDao.selectByRunLogId(runLogId);
        } catch (RuntimeException e) {
            campusMonitorRunLogDao.finish(runLogId, RUN_FAILED, counter.scannedCount,
                    counter.matchCount, counter.negativeCount, counter.alertCount, summary(e.getMessage(), 2048));
            String errorMessage = StringUtils.defaultIfBlank(e.getMessage(), autoOutcome.errorMessage());
            campusMonitorTaskDao.updateRunSummary(monitorTaskId, runLogId, counter.matchCount,
                    autoOutcome.getLastCollectTime(), AUTO_INGEST_FAILED,
                    summary(errorMessage, 1024), operatorUserId);
            completeTaskAfterRun(task, runLogId, false, operatorUserId, manual, lockUntil);
            throw e;
        }
    }

    private void completeTaskAfterRun(CampusMonitorTask task,
                                      Long runLogId,
                                      boolean success,
                                      Long operatorUserId,
                                      boolean manual,
                                      Date lockUntil) {
        if (manual) {
            campusMonitorTaskDao.updateLastRun(task.getMonitorTaskId(), runLogId, operatorUserId);
            refreshManualNextRunTimeIfDue(task, operatorUserId);
            campusMonitorTaskDao.releaseScheduleLockBefore(task.getMonitorTaskId(), lockUntil);
            return;
        }
        Date nextRunTime = nextFrequencyRunTime(task, new Date());
        if (success) {
            campusMonitorTaskDao.markScheduleSuccess(task.getMonitorTaskId(), runLogId, nextRunTime, lockUntil, SYSTEM_USER_ID);
        } else {
            campusMonitorTaskDao.markScheduleFailure(task.getMonitorTaskId(), runLogId, nextRunTime, lockUntil, SYSTEM_USER_ID);
        }
    }

    private void refreshManualNextRunTimeIfDue(CampusMonitorTask task, Long operatorUserId) {
        if (task == null || task.getScheduleEnabled() == null || task.getScheduleEnabled() != 1) {
            return;
        }
        Date now = new Date();
        if (task.getNextRunTime() == null || !task.getNextRunTime().after(now)) {
            campusMonitorTaskDao.updateNextRunTime(task.getMonitorTaskId(), nextFrequencyRunTime(task, now), operatorUserId);
        }
    }

    private Date nextFrequencyRunTime(CampusMonitorTask task, Date from) {
        int minutes = safeScanFrequencyMinutes(task);
        return new Date((from == null ? System.currentTimeMillis() : from.getTime()) + minutes * 60L * 1000L);
    }

    @Override
    public PageInfo<CampusMonitorResult> listResults(Integer pageNum,
                                                    Integer pageSize,
                                                    Long monitorTaskId,
                                                    String keyword,
                                                    String riskLevel,
                                                    String resultStatus,
                                                    String platform,
                                                    String language,
                                                    Boolean converted) {
        PageHelper.startPage(defaultPageNum(pageNum), defaultPageSize(pageSize));
        return new PageInfo<>(campusMonitorResultDao.list(monitorTaskId, keyword,
                CampusRiskLevel.normalizeForQuery(riskLevel), resultStatus, platform, language, converted));
    }

    @Override
    public PageInfo<CampusMonitorInformation> listInformation(Integer pageNum,
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
                                                              String sortBy) {
        PageHelper.startPage(defaultPageNum(pageNum), defaultPageSize(pageSize));
        return new PageInfo<>(campusMonitorResultDao.listInformation(keyword, monitorTaskId, sourcePlatform, sourceSubPlatform,
                CampusRiskLevel.normalizeForQuery(riskLevel), clueStatus, language, sentiment, resultStatus,
                publishTimeStart, publishTimeEnd, collectTimeStart, collectTimeEnd,
                matchScope, similarDedup, normalizeInformationHitScope(hitScope), sortBy));
    }

    @Override
    public List<Map<String, Object>> countInformationByPlatform(String keyword,
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
                                                                String hitScope) {
        return campusMonitorResultDao.countInformationByPlatform(keyword, monitorTaskId, sourcePlatform, sourceSubPlatform,
                CampusRiskLevel.normalizeForQuery(riskLevel), clueStatus, language, sentiment, resultStatus,
                publishTimeStart, publishTimeEnd, collectTimeStart, collectTimeEnd,
                matchScope, similarDedup, normalizeInformationHitScope(hitScope));
    }

    @Override
    public List<Map<String, Object>> countInformationBySubPlatform(String keyword,
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
                                                                   String hitScope) {
        return campusMonitorResultDao.countInformationBySubPlatform(keyword, monitorTaskId, sourcePlatform, sourceSubPlatform,
                CampusRiskLevel.normalizeForQuery(riskLevel), clueStatus, language, sentiment, resultStatus,
                publishTimeStart, publishTimeEnd, collectTimeStart, collectTimeEnd,
                matchScope, similarDedup, normalizeInformationHitScope(hitScope));
    }

    private String normalizeInformationHitScope(String hitScope) {
        return "risk".equalsIgnoreCase(StringUtils.trimToEmpty(hitScope)) ? "risk" : "all";
    }

    private String requireSentiment(String sentiment) {
        String normalized = CampusSentimentNormalizer.normalize(sentiment);
        if (StringUtils.isBlank(normalized)) {
            throw new IllegalArgumentException("情感类型只能为 positive、neutral、negative、none");
        }
        return normalized;
    }

    @Override
    public CampusMonitorResult alertResult(Long monitorResultId, Long operatorUserId) {
        CampusMonitorResult result = requireResult(monitorResultId);
        if (RESULT_ALERTED.equals(result.getResultStatus()) && result.getAlertId() != null) {
            return result;
        }
        CampusAlert alert = createAlertForResult(result, operatorUserId);
        campusMonitorResultDao.updateStatus(monitorResultId, RESULT_ALERTED, alert.getAlertId(), operatorUserId);
        return campusMonitorResultDao.selectByResultId(monitorResultId);
    }

    @Override
    @Transactional
    public CampusMonitorResult ignoreResult(Long monitorResultId, Long operatorUserId) {
        CampusMonitorResult result = requireResult(monitorResultId);
        if (result.getAlertId() != null) {
            campusAlertDao.handle(result.getAlertId(), RESULT_IGNORED, "监测信息取消预警", operatorUserId, operatorUserId);
        }
        campusMonitorResultDao.updateStatusAndRisk(monitorResultId, RESULT_IGNORED, null, RISK_NORMAL, 0, operatorUserId);
        return campusMonitorResultDao.selectByResultId(monitorResultId);
    }

    @Override
    @Transactional
    public CampusMonitorResult updateResultSentiment(Long monitorResultId,
                                                     String sentiment,
                                                     Long operatorUserId,
                                                     String operatorName) {
        CampusMonitorResult result = requireResult(monitorResultId);
        String normalized = requireSentiment(sentiment);
        if (result.getClueId() != null) {
            campusClueService.updateSentimentFromMonitor(result.getClueId(), normalized,
                    monitorResultId, operatorUserId, operatorName);
        }
        int updated = campusMonitorResultDao.updateSentiment(monitorResultId, normalized, operatorUserId);
        if (updated != 1) {
            throw new IllegalArgumentException("监测结果不存在或已删除");
        }
        return campusMonitorResultDao.selectByResultId(monitorResultId);
    }

    @Override
    public CampusMonitorAiAnalyzeResponse analyzeResults(CampusMonitorAiAnalyzeRequest request,
                                                         Long operatorUserId,
                                                         String operatorName) {
        return analyzeResolvedResults(resolveAiAnalyzeTargets(request), operatorUserId, operatorName, AI_TRIGGER_MANUAL);
    }

    @Override
    public CampusMonitorAiAnalyzeResponse analyzePendingAiResults(Integer limit) {
        int safeLimit = safeAiAnalyzeLimit(limit);
        List<CampusMonitorResult> targets = campusMonitorResultDao.listPendingAiAnalysis(safeLimit);
        return analyzeResolvedResults(targets, SYSTEM_USER_ID, "系统自动AI分析", AI_TRIGGER_AUTO);
    }

    private CampusMonitorAiAnalyzeResponse analyzeResolvedResults(List<CampusMonitorResult> targets,
                                                                  Long operatorUserId,
                                                                  String operatorName,
                                                                  String trigger) {
        CampusMonitorAiAnalyzeResponse response = new CampusMonitorAiAnalyzeResponse();
        if (targets == null) {
            return response;
        }
        if (targets.isEmpty()) {
            return response;
        }
        String analysisTrigger = normalizeAiAnalysisTrigger(trigger);
        Date attemptTime = new Date();

        List<CampusMonitorResult> analyzable = new ArrayList<>();
        for (CampusMonitorResult target : targets) {
            if (target == null || target.getMonitorResultId() == null) {
                continue;
            }
            if (isArchivedLinkedClue(target)) {
                markAiAnalysisFailed(target, analysisTrigger, "已归档线索关联的监测命中跳过AI分析", operatorUserId);
                response.add(aiAnalyzeItem(target.getMonitorResultId(), false, true,
                        "已归档线索关联的监测命中跳过AI分析", null));
                continue;
            }
            analyzable.add(target);
        }
        if (analyzable.isEmpty()) {
            return response;
        }
        for (CampusMonitorResult target : analyzable) {
            campusMonitorResultDao.updateAiAnalysisStatus(target.getMonitorResultId(),
                    AI_STATUS_PROCESSING, analysisTrigger, null, attemptTime, operatorUserId);
        }

        CampusAiRuntimeConfig config = resolveAiConfig(FEATURE_MONITOR_RESULT_ANALYSIS);
        String content;
        try {
            content = callMonitorResultAnalysisAi(analyzable);
        } catch (Exception ex) {
            String message = StringUtils.defaultIfBlank(ex.getMessage(), "AI分析失败");
            for (CampusMonitorResult target : analyzable) {
                markAiAnalysisFailed(target, analysisTrigger, message, operatorUserId);
                response.add(aiAnalyzeItem(target.getMonitorResultId(), false, false, message, null));
            }
            return response;
        }

        Map<String, JSONObject> resultMap;
        try {
            resultMap = parseMonitorResultAnalysisMap(content);
        } catch (Exception ex) {
            String message = StringUtils.defaultIfBlank(ex.getMessage(), "AI返回格式解析失败");
            for (CampusMonitorResult target : analyzable) {
                markAiAnalysisFailed(target, analysisTrigger, message, operatorUserId);
                response.add(aiAnalyzeItem(target.getMonitorResultId(), false, false, message, null));
            }
            return response;
        }

        for (CampusMonitorResult target : analyzable) {
            JSONObject analysis = resultMap.get(String.valueOf(target.getMonitorResultId()));
            if (analysis == null) {
                markAiAnalysisFailed(target, analysisTrigger, "AI未返回该条结果", operatorUserId);
                response.add(aiAnalyzeItem(target.getMonitorResultId(), false, false,
                        "AI未返回该条结果", null));
                continue;
            }
            try {
                AiResultAnalysis parsed = normalizeAiResultAnalysis(analysis);
                parsed.analysisBasis = StringUtils.defaultIfBlank(parsed.analysisBasis, inferAiAnalysisBasis(target));
                parsed.providerCode = config == null ? null : config.getProviderCode();
                parsed.modelCode = config == null ? null : config.getModelCode();
                CampusMonitorResult saved = applyAiResultAnalysis(target, parsed, operatorUserId, operatorName, analysisTrigger);
                response.add(aiAnalyzeItem(target.getMonitorResultId(), true, false, "AI分析完成", saved));
            } catch (Exception ex) {
                markAiAnalysisFailed(target, analysisTrigger,
                        StringUtils.defaultIfBlank(ex.getMessage(), "AI分析写入失败"), operatorUserId);
                response.add(aiAnalyzeItem(target.getMonitorResultId(), false, false,
                        StringUtils.defaultIfBlank(ex.getMessage(), "AI分析写入失败"), null));
            }
        }
        return response;
    }

    @Override
    public CampusMonitorAlertCleanupPreview previewAlertCleanupCandidates(Integer limit) {
        int safeLimit = clampLimit(limit, 20, ALERT_CLEANUP_PREVIEW_MAX);
        CampusMonitorAlertCleanupPreview preview = new CampusMonitorAlertCleanupPreview();
        int actionableCount = campusMonitorResultDao.countAlertCleanupCandidates(false);
        int totalCount = campusMonitorResultDao.countAlertCleanupCandidates(true);
        preview.setActionableCandidateCount(actionableCount);
        preview.setTotalCandidateCount(totalCount);
        preview.setLinkedClueCandidateCount(Math.max(totalCount - actionableCount, 0));
        preview.setNegativeEvidenceAlertCount(campusMonitorResultDao.countAlertCleanupNegativeEvidence());
        preview.setPreviewLimit(safeLimit);
        preview.setItems(campusMonitorResultDao.listAlertCleanupCandidates(safeLimit, false));
        return preview;
    }

    @Override
    @Transactional
    public CampusMonitorAlertCleanupResponse cleanupAlertCandidates(CampusMonitorAlertCleanupRequest request,
                                                                    Long operatorUserId) {
        if (request == null || !ALERT_CLEANUP_CONFIRM_TEXT.equals(StringUtils.trimToEmpty(request.getConfirmText()))) {
            throw new IllegalArgumentException("请先确认取消疑似误预警");
        }
        boolean includeLinkedClue = Boolean.TRUE.equals(request.getIncludeLinkedClue());
        int maxCount = clampLimit(request.getMaxCount(), 100, ALERT_CLEANUP_EXECUTE_MAX);
        List<CampusMonitorAlertCleanupCandidate> candidates =
                campusMonitorResultDao.listAlertCleanupCandidates(maxCount, includeLinkedClue);
        CampusMonitorAlertCleanupResponse response = new CampusMonitorAlertCleanupResponse();
        response.setRequestedCount(maxCount);
        response.setIncludeLinkedClue(includeLinkedClue);
        response.setItems(candidates);
        if (candidates.isEmpty()) {
            return response;
        }
        for (CampusMonitorAlertCleanupCandidate candidate : candidates) {
            if (!includeLinkedClue && candidate.getClueId() != null) {
                response.setSkipCount(response.getSkipCount() + 1);
                continue;
            }
            int alertUpdated = campusAlertDao.handle(candidate.getAlertId(), RESULT_IGNORED,
                    "疑似误预警批量取消", operatorUserId, operatorUserId);
            int resultUpdated = campusMonitorResultDao.updateStatusAndRisk(candidate.getMonitorResultId(),
                    RESULT_IGNORED, null, RISK_NORMAL, 0, operatorUserId);
            if (alertUpdated != 1 || resultUpdated != 1) {
                throw new IllegalStateException("取消预警失败，监测结果ID：" + candidate.getMonitorResultId());
            }
            response.setSuccessCount(response.getSuccessCount() + 1);
        }
        return response;
    }

    private int clampLimit(Integer value, int defaultValue, int maxValue) {
        int result = value == null ? defaultValue : value;
        if (result < 1) {
            return defaultValue;
        }
        return Math.min(result, maxValue);
    }

    private List<CampusMonitorResult> resolveAiAnalyzeTargets(CampusMonitorAiAnalyzeRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("AI分析参数不能为空");
        }
        int limit = safeAiAnalyzeLimit(request.getLimit());
        if (request.getMonitorResultIds() != null && !request.getMonitorResultIds().isEmpty()) {
            List<Long> ids = new ArrayList<>();
            for (Long id : request.getMonitorResultIds()) {
                if (id != null && !ids.contains(id)) {
                    ids.add(id);
                }
                if (ids.size() >= limit) {
                    break;
                }
            }
            if (ids.isEmpty()) {
                return Collections.emptyList();
            }
            return campusMonitorResultDao.listByResultIds(ids);
        }
        if (request.getMonitorTaskId() != null) {
            requireTask(request.getMonitorTaskId());
            return campusMonitorResultDao.listRecentForAi(request.getMonitorTaskId(), limit);
        }
        throw new IllegalArgumentException("请选择监测信息或监测任务");
    }

    private int safeAiAnalyzeLimit(Integer limit) {
        if (limit == null || limit < 1) {
            return DEFAULT_AI_ANALYZE_LIMIT;
        }
        return Math.min(limit, MAX_AI_ANALYZE_LIMIT);
    }

    private String normalizeAiAnalysisTrigger(String trigger) {
        return AI_TRIGGER_AUTO.equals(trigger) ? AI_TRIGGER_AUTO : AI_TRIGGER_MANUAL;
    }

    private void markAiAnalysisFailed(CampusMonitorResult result,
                                      String trigger,
                                      String message,
                                      Long operatorUserId) {
        if (result == null || result.getMonitorResultId() == null) {
            return;
        }
        campusMonitorResultDao.updateAiAnalysisStatus(result.getMonitorResultId(),
                AI_STATUS_FAILED,
                normalizeAiAnalysisTrigger(trigger),
                StringUtils.left(StringUtils.defaultIfBlank(message, "AI分析失败"), 512),
                new Date(),
                operatorUserId);
    }

    private boolean isArchivedLinkedClue(CampusMonitorResult result) {
        if (result == null || result.getClueId() == null) {
            return false;
        }
        try {
            CampusClue clue = campusClueService.detail(result.getClueId());
            return clue != null && "archived".equals(clue.getClueStatus());
        } catch (RuntimeException ex) {
            return false;
        }
    }

    private String callMonitorResultAnalysisAi(List<CampusMonitorResult> targets) {
        JSONObject taskJson = buildCommonTaskJson(targets);
        JSONArray itemsJson = buildAiAnalyzeItemsJson(targets);
        Map<String, String> variables = new HashMap<>();
        variables.put("taskJson", taskJson.toJSONString());
        variables.put("itemsJson", itemsJson.toJSONString());
        CampusAiPromptTemplate prompt = campusAiRuntimeService.getActivePrompt(FEATURE_MONITOR_RESULT_ANALYSIS);
        String systemPrompt = StringUtils.defaultIfBlank(prompt == null ? null : prompt.getSystemPrompt(),
                "你是校园舆情监测分析助手。请只返回JSON，不要输出解释。");
        String userPrompt = applyPromptTemplate(StringUtils.defaultIfBlank(prompt == null ? null : prompt.getUserPrompt(),
                "请分析以下监测命中列表。任务信息：${taskJson}。命中列表：${itemsJson}。逐条判断情感、一句话摘要、是否应该算作该任务命中、学校相关性、主题分类和理由。"),
                variables);
        userPrompt = appendMonitorResultAnalysisContract(userPrompt);

        CampusAiChatRequest chatRequest = new CampusAiChatRequest();
        chatRequest.setFeatureCode(FEATURE_MONITOR_RESULT_ANALYSIS);
        chatRequest.setSystemPrompt(systemPrompt);
        chatRequest.setUserPrompt(userPrompt);
        chatRequest.setMaxTokens(4096);
        chatRequest.setTemperature(new BigDecimal("0.10"));
        CampusAiChatResponse chatResponse = campusAiChatService.chat(chatRequest);
        return chatResponse == null ? null : chatResponse.getContent();
    }

    private JSONArray buildAiAnalyzeItemsJson(List<CampusMonitorResult> targets) {
        JSONArray array = new JSONArray();
        Map<Long, CampusMonitorTask> taskCache = new HashMap<>();
        for (CampusMonitorResult result : targets) {
            CampusMonitorTask task = null;
            if (result.getMonitorTaskId() != null) {
                task = taskCache.get(result.getMonitorTaskId());
                if (task == null) {
                    task = campusMonitorTaskDao.selectByTaskId(result.getMonitorTaskId());
                    taskCache.put(result.getMonitorTaskId(), task);
                }
            }
            CampusIngestRecord record = result.getIngestRecordId() == null
                    ? null
                    : campusIngestRecordDao.selectByRecordId(result.getIngestRecordId());
            AiAnalysisText analysisText = buildAiAnalysisText(result, record, task);
            JSONObject item = new JSONObject();
            item.put("monitorResultId", String.valueOf(result.getMonitorResultId()));
            item.put("task", buildMonitorResultTaskJson(task));
            item.put("title", StringUtils.left(analysisText.title, 300));
            item.put("content", StringUtils.left(analysisText.content, 1200));
            item.put("primaryText", StringUtils.left(analysisText.primaryText, 1200));
            item.put("secondaryTitle", StringUtils.left(analysisText.secondaryTitle, 300));
            item.put("analysisBasisHint", analysisText.analysisBasis);
            item.put("contentUsable", analysisText.contentUsable);
            item.put("contentQualityReason", analysisText.contentQualityReason);
            item.put("titleSignalScore", analysisText.titleSignalScore);
            item.put("contentSignalScore", analysisText.contentSignalScore);
            item.put("textSelectionReason", analysisText.selectionReason);
            item.put("platform", StringUtils.defaultIfBlank(result.getPlatform(), record == null ? null : record.getPlatform()));
            item.put("authorName", StringUtils.defaultIfBlank(result.getAuthorName(), record == null ? null : record.getAuthorName()));
            item.put("matchedSubjects", result.getMatchedSubjects());
            item.put("matchedKeywords", result.getMatchedKeywords());
            item.put("matchedNegativeWords", result.getMatchedNegativeWords());
            item.put("currentSentiment", result.getSentiment());
            array.add(item);
        }
        return array;
    }

    private AiAnalysisText buildAiAnalysisText(CampusMonitorResult result,
                                               CampusIngestRecord record,
                                               CampusMonitorTask task) {
        String title = cleanAiAnalysisText(preferText(result == null ? null : result.getTitle(),
                record == null ? null : record.getTitle()));
        String content = cleanAiAnalysisText(preferLongerText(result == null ? null : result.getContent(),
                record == null ? null : record.getContent()));
        int titleScore = calculateAiTextSignalScore(title, result, record, task);
        int contentScore = calculateAiTextSignalScore(content, result, record, task);
        boolean contentUsable = isUsableAiAnalysisContent(content, title);
        boolean titleUsable = StringUtils.isNotBlank(title);
        String basis;
        if (contentUsable && (!titleUsable || contentScore >= titleScore)) {
            basis = AI_ANALYSIS_BASIS_CONTENT;
        } else if (titleUsable) {
            basis = AI_ANALYSIS_BASIS_TITLE;
        } else if (contentUsable) {
            basis = AI_ANALYSIS_BASIS_CONTENT;
        } else {
            basis = AI_ANALYSIS_BASIS_NONE;
        }
        String primaryText = AI_ANALYSIS_BASIS_CONTENT.equals(basis) ? content
                : AI_ANALYSIS_BASIS_TITLE.equals(basis) ? title : "";
        String secondaryTitle = AI_ANALYSIS_BASIS_CONTENT.equals(basis) ? title
                : AI_ANALYSIS_BASIS_TITLE.equals(basis) ? content : "";
        String qualityReason = contentUsable ? "正文可用于比较判断"
                : StringUtils.isBlank(content) ? "正文缺失，使用标题兜底"
                : "正文过短、重复标题或疑似平台噪声，使用标题兜底";
        String selectionReason = buildAiTextSelectionReason(basis, titleScore, contentScore, contentUsable);
        return new AiAnalysisText(title, content, primaryText, secondaryTitle, basis, contentUsable,
                qualityReason, titleScore, contentScore, selectionReason);
    }

    private String inferAiAnalysisBasis(CampusMonitorResult result) {
        CampusIngestRecord record = result == null || result.getIngestRecordId() == null
                ? null
                : campusIngestRecordDao.selectByRecordId(result.getIngestRecordId());
        CampusMonitorTask task = result == null || result.getMonitorTaskId() == null
                ? null
                : campusMonitorTaskDao.selectByTaskId(result.getMonitorTaskId());
        return buildAiAnalysisText(result, record, task).analysisBasis;
    }

    private int calculateAiTextSignalScore(String text,
                                           CampusMonitorResult result,
                                           CampusIngestRecord record,
                                           CampusMonitorTask task) {
        if (StringUtils.isBlank(text)) {
            return 0;
        }
        int score = 0;
        score += scoreMatchedTokens(text, splitTokens(result == null ? null : result.getMatchedSubjects()), 35);
        score += scoreMatchedTokens(text, splitTokens(joinTokens(task == null ? null : task.getMonitorSubject(),
                task == null ? null : task.getSubjectAliases())), 25);
        score += scoreMatchedTokens(text, splitTokens(result == null ? null : result.getMatchedNegativeWords()), 25);
        score += scoreMatchedTokens(text, splitTokens(task == null ? null : task.getNegativeWords()), 20);
        score += scoreMatchedTokens(text, splitTokens(result == null ? null : result.getMatchedKeywords()), 16);
        score += scoreMatchedTokens(text, splitTokens(joinTokens(task == null ? null : task.getKeywords(),
                record == null ? null : record.getKeywords())), 12);
        score += scoreMatchedTokens(text, splitTokens(StringUtils.join(AI_CONTENT_NOISE_TERMS, ",")), -3);
        return Math.max(score, 0);
    }

    private int scoreMatchedTokens(String text, Set<String> tokens, int weight) {
        if (StringUtils.isBlank(text) || tokens == null || tokens.isEmpty() || weight == 0) {
            return 0;
        }
        int count = 0;
        for (String token : tokens) {
            if (StringUtils.length(StringUtils.trimToEmpty(token)) >= 2
                    && StringUtils.containsIgnoreCase(text, token)) {
                count++;
            }
        }
        return count * weight;
    }

    private String buildAiTextSelectionReason(String basis,
                                              int titleScore,
                                              int contentScore,
                                              boolean contentUsable) {
        if (AI_ANALYSIS_BASIS_CONTENT.equals(basis)) {
            if (contentScore == titleScore) {
                return "标题和正文匹配度相同，按正文优先";
            }
            return "正文包含更多任务或校园相关信息";
        }
        if (AI_ANALYSIS_BASIS_TITLE.equals(basis)) {
            if (!contentUsable) {
                return "正文不可用，标题作为主信息";
            }
            return "标题包含更多任务或校园相关信息，正文更像回复或补充";
        }
        return "标题和正文均缺少可分析信息";
    }

    private boolean isUsableAiAnalysisContent(String content, String title) {
        String compactContent = compactAiAnalysisText(content);
        if (StringUtils.isBlank(compactContent) || compactContent.length() < MIN_AI_CONTENT_TEXT_LENGTH) {
            return false;
        }
        String compactTitle = compactAiAnalysisText(title);
        if (StringUtils.isNotBlank(compactTitle) && StringUtils.equalsIgnoreCase(compactContent, compactTitle)) {
            return false;
        }
        if (looksLikeUrlOnly(compactContent)) {
            return false;
        }
        return !looksLikePlatformNoise(compactContent);
    }

    private String cleanAiAnalysisText(String text) {
        String trimmed = StringUtils.trimToEmpty(text);
        if (StringUtils.isBlank(trimmed)) {
            return "";
        }
        return trimmed.replaceAll("[\\r\\n\\t ]+", " ");
    }

    private String compactAiAnalysisText(String text) {
        return StringUtils.trimToEmpty(text).replaceAll("[\\s　]+", "");
    }

    private boolean looksLikeUrlOnly(String compactText) {
        String lower = StringUtils.lowerCase(compactText);
        return (lower.startsWith("http://") || lower.startsWith("https://") || lower.startsWith("www."))
                && !StringUtils.containsAny(compactText, "。", "，", "！", "？", "、");
    }

    private boolean looksLikePlatformNoise(String compactText) {
        if (StringUtils.length(compactText) > 80) {
            return false;
        }
        int hits = 0;
        for (String term : AI_CONTENT_NOISE_TERMS) {
            if (StringUtils.containsIgnoreCase(compactText, term)) {
                hits++;
            }
        }
        return hits >= 3;
    }

    private JSONObject buildCommonTaskJson(List<CampusMonitorResult> targets) {
        JSONObject json = new JSONObject();
        Set<Long> taskIds = new LinkedHashSet<>();
        for (CampusMonitorResult result : targets) {
            if (result != null && result.getMonitorTaskId() != null) {
                taskIds.add(result.getMonitorTaskId());
            }
        }
        if (taskIds.size() == 1) {
            CampusMonitorTask task = campusMonitorTaskDao.selectByTaskId(taskIds.iterator().next());
            return buildMonitorResultTaskJson(task);
        }
        json.put("scope", "multiple_tasks");
        json.put("taskCount", taskIds.size());
        return json;
    }

    private JSONObject buildMonitorResultTaskJson(CampusMonitorTask task) {
        JSONObject json = new JSONObject();
        if (task == null) {
            return json;
        }
        json.put("monitorTaskId", String.valueOf(task.getMonitorTaskId()));
        json.put("taskName", task.getTaskName());
        json.put("monitorSubject", task.getMonitorSubject());
        json.put("subjectAliases", task.getSubjectAliases());
        json.put("keywords", task.getKeywords());
        json.put("negativeWords", task.getNegativeWords());
        json.put("excludeWords", task.getExcludeWords());
        json.put("platformScope", task.getPlatformScope());
        return json;
    }

    private String appendMonitorResultAnalysisContract(String prompt) {
        return StringUtils.defaultString(prompt)
                + "\n\n分析依据要求：每条输入都包含title、content、primaryText、secondaryTitle、analysisBasisHint、contentUsable、titleSignalScore、contentSignalScore和textSelectionReason。"
                + "你必须同时阅读title和content，先比较哪一段更包含监测任务、校园主体、关键词、负面词或具体事实，再判断情感、摘要、风险等级、主题和学校相关性。"
                + "primaryText是系统按匹配度预选的主信息源：当analysisBasisHint=title时，标题更像主要内容，正文可能只是回复、评论或补充；当analysisBasisHint=content时，正文更像主要内容。"
                + "如果你判断系统预选不合理，可以用另一段或综合两段，但必须在analysisBasis中返回content、title或mixed。"
                + "当标题和正文同样匹配时，优先依据正文；正文仍不足时再依据标题。"
                + "不能只因为标题负面就制造正文没有的风险；仅标题负面但没有明确校园风险事实时，riskLevel保持normal。"
                + "\n\n返回格式必须是一个JSON对象，不要返回JSON数组，结构如下："
                + "{\"results\":[{\"monitorResultId\":\"输入中的ID字符串\","
                + "\"analysisBasis\":\"content|title|mixed\","
                + "\"sentiment\":\"positive|neutral|negative|none\","
                + "\"summary\":\"50字以内一句话\","
                + "\"shouldHit\":\"hit|not_hit|uncertain\","
                + "\"hitReason\":\"80字以内\","
                + "\"confidence\":0,"
                + "\"schoolRelevanceScore\":0,"
                + "\"matchedSchoolTerms\":\"命中的学校相关词\","
                + "\"topicCategory\":\"校园主题大类\","
                + "\"topicSubCategory\":\"校园主题小类\","
                + "\"topicReason\":\"主题判断理由\","
                + "\"riskLevel\":\"normal|concern\","
                + "\"riskReason\":\"是否进入一般预警的理由\"}]}"
                + "。必须逐条保留输入中的monitorResultId。普通关键词只判断是否属于任务，不能因为普通关键词命中就判为concern；"
                + "只有负面词、风险词、原始风险、负面情感或内容本身存在明确校园舆情风险时，riskLevel才返回concern。";
    }

    private Map<String, JSONObject> parseMonitorResultAnalysisMap(String content) {
        JSONArray results = parseAiResultsArray(content);
        if (results == null || results.isEmpty()) {
            throw new IllegalArgumentException("AI响应缺少results数组");
        }
        Map<String, JSONObject> map = new LinkedHashMap<>();
        for (int i = 0; i < results.size(); i++) {
            JSONObject item = results.getJSONObject(i);
            if (item == null) {
                continue;
            }
            String monitorResultId = StringUtils.trimToNull(item.getString("monitorResultId"));
            if (monitorResultId != null) {
                map.put(monitorResultId, item);
            }
        }
        if (map.isEmpty()) {
            throw new IllegalArgumentException("AI响应未包含有效监测结果ID");
        }
        return map;
    }

    private JSONArray parseAiResultsArray(String content) {
        JSONObject root = parseAiObject(content);
        if (root != null) {
            JSONArray results = root.getJSONArray("results");
            if (results != null) {
                return results;
            }
            JSONArray data = root.getJSONArray("data");
            if (data != null) {
                return data;
            }
        }
        return parseAiArray(content);
    }

    private JSONArray parseAiArray(String content) {
        if (StringUtils.isBlank(content)) {
            return null;
        }
        String json = content.trim();
        if (json.startsWith("```")) {
            int start = json.indexOf('\n');
            if (start >= 0 && start < json.length() - 1) {
                json = json.substring(start + 1);
            }
            int end = json.lastIndexOf("```");
            if (end > 0) {
                json = json.substring(0, end);
            }
            json = json.trim();
        }
        if (!json.startsWith("[")) {
            int start = json.indexOf('[');
            int end = json.lastIndexOf(']');
            if (start >= 0 && end > start) {
                json = json.substring(start, end + 1);
            }
        }
        try {
            return JSON.parseArray(json);
        } catch (Exception ex) {
            return null;
        }
    }

    private AiResultAnalysis normalizeAiResultAnalysis(JSONObject json) {
        AiResultAnalysis analysis = new AiResultAnalysis();
        analysis.analysisBasis = normalizeAiAnalysisBasis(json.getString("analysisBasis"));
        String sentiment = CampusSentimentNormalizer.normalize(json.getString("sentiment"));
        analysis.sentiment = StringUtils.defaultIfBlank(sentiment, "none");
        analysis.summary = StringUtils.left(json.getString("summary"), 255);
        analysis.hitRecommendation = normalizeAiHitRecommendation(StringUtils.defaultIfBlank(
                json.getString("shouldHit"), StringUtils.defaultIfBlank(
                        json.getString("aiHitRecommendation"), json.getString("hitAdvice"))));
        String reason = StringUtils.defaultIfBlank(json.getString("hitReason"), json.getString("reason"));
        analysis.hitReason = StringUtils.left(reason, 512);
        analysis.confidence = clamp(json.getInteger("confidence"), 0, 100);
        analysis.schoolRelevanceScore = clamp(json.getInteger("schoolRelevanceScore"), 0, 100);
        if (analysis.schoolRelevanceScore == null) {
            analysis.schoolRelevanceScore = normalizeSchoolRelevanceScore(json.getString("schoolRelevance"));
        }
        analysis.matchedSchoolTerms = StringUtils.left(json.getString("matchedSchoolTerms"), 512);
        analysis.schoolRelevanceReason = StringUtils.left(StringUtils.defaultIfBlank(
                json.getString("schoolRelevanceReason"), reason), 1024);
        analysis.topicCategory = StringUtils.left(json.getString("topicCategory"), 64);
        analysis.topicSubCategory = StringUtils.left(json.getString("topicSubCategory"), 64);
        analysis.topicReason = StringUtils.left(StringUtils.defaultIfBlank(json.getString("topicReason"), reason), 1024);
        analysis.riskLevel = normalizeAiRiskLevel(StringUtils.defaultIfBlank(json.getString("riskLevel"),
                StringUtils.defaultIfBlank(json.getString("warningLevel"), json.getString("alertLevel"))));
        if (analysis.riskLevel == null && isAiAffirmative(json.getString("shouldWarn"))) {
            analysis.riskLevel = RISK_CONCERN;
        }
        analysis.riskReason = StringUtils.left(StringUtils.defaultIfBlank(json.getString("riskReason"), reason), 512);
        return analysis;
    }

    private String normalizeAiAnalysisBasis(String value) {
        String normalized = StringUtils.lowerCase(StringUtils.trimToEmpty(value));
        if (StringUtils.isBlank(normalized)) {
            return null;
        }
        if ("正文".equals(normalized) || "content".equals(normalized) || "body".equals(normalized)) {
            return AI_ANALYSIS_BASIS_CONTENT;
        }
        if ("标题".equals(normalized) || "title".equals(normalized) || "headline".equals(normalized)) {
            return AI_ANALYSIS_BASIS_TITLE;
        }
        if ("mixed".equals(normalized) || "both".equals(normalized) || "综合".equals(normalized)) {
            return "mixed";
        }
        return null;
    }

    private CampusMonitorResult applyAiResultAnalysis(CampusMonitorResult result,
                                                      AiResultAnalysis analysis,
                                                      Long operatorUserId,
                                                      String operatorName,
                                                      String analysisTrigger) {
        final Date now = new Date();
        final String excludedReason = "not_hit".equals(analysis.hitRecommendation)
                ? StringUtils.left(analysis.hitReason, 512)
                : null;
        final Integer schoolScore = analysis.schoolRelevanceScore == null
                ? result.getSchoolRelevanceScore()
                : analysis.schoolRelevanceScore;
        final String schoolReason = StringUtils.defaultIfBlank(analysis.schoolRelevanceReason, result.getSchoolRelevanceReason());
        final String schoolTerms = StringUtils.defaultIfBlank(analysis.matchedSchoolTerms, result.getMatchedSchoolTerms());
        final String topicCategory = StringUtils.defaultIfBlank(analysis.topicCategory, result.getTopicCategory());
        final String topicSubCategory = StringUtils.defaultIfBlank(analysis.topicSubCategory, result.getTopicSubCategory());
        final String topicReason = StringUtils.defaultIfBlank(analysis.topicReason, result.getTopicReason());
        final String sentiment = StringUtils.defaultIfBlank(analysis.sentiment,
                CampusSentimentNormalizer.normalizeOrDefault(result.getSentiment(), "none"));
        final CampusIngestRecord ingestRecord = result.getIngestRecordId() == null
                ? null
                : campusIngestRecordDao.selectByRecordId(result.getIngestRecordId());
        final String riskLevel = resolveAiAnalysisRiskLevel(result, ingestRecord, analysis, sentiment);
        final Integer riskScore = resolveAiAnalysisRiskScore(riskLevel, result);

        return transactionTemplate.execute(status -> {
            if (result.getClueId() != null) {
                campusClueService.updateAnalysisFromMonitor(result.getClueId(), sentiment, riskLevel,
                        schoolScore, schoolReason, schoolTerms, excludedReason,
                        topicCategory, topicSubCategory, topicReason,
                        result.getMonitorResultId(), operatorUserId, operatorName);
            }
            int updated = campusMonitorResultDao.updateAiAnalysis(result.getMonitorResultId(),
                    sentiment,
                    StringUtils.left(analysis.summary, 255),
                    analysis.hitRecommendation,
                    StringUtils.left(analysis.hitReason, 512),
                    analysis.confidence,
                    now,
                    StringUtils.left(analysis.providerCode, 64),
                    StringUtils.left(analysis.modelCode, 64),
                    normalizeAiAnalysisTrigger(analysisTrigger),
                    riskLevel,
                    riskScore,
                    schoolScore,
                    StringUtils.left(schoolReason, 1024),
                    StringUtils.left(schoolTerms, 512),
                    excludedReason,
                    StringUtils.left(topicCategory, 64),
                    StringUtils.left(topicSubCategory, 64),
                    StringUtils.left(topicReason, 1024),
                    operatorUserId);
            if (updated != 1) {
                throw new IllegalArgumentException("监测结果不存在或已删除");
            }
            return campusMonitorResultDao.selectByResultId(result.getMonitorResultId());
        });
    }

    private CampusMonitorAiAnalyzeResponse.Item aiAnalyzeItem(Long monitorResultId,
                                                              boolean success,
                                                              boolean skipped,
                                                              String message,
                                                              CampusMonitorResult saved) {
        CampusMonitorAiAnalyzeResponse.Item item = new CampusMonitorAiAnalyzeResponse.Item();
        item.setMonitorResultId(monitorResultId);
        item.setSuccess(success);
        item.setSkipped(skipped);
        item.setMessage(message);
        if (saved != null) {
            item.setSentiment(saved.getSentiment());
            item.setAiSummary(saved.getAiSummary());
            item.setAiHitRecommendation(saved.getAiHitRecommendation());
            item.setAiHitReason(saved.getAiHitReason());
            item.setAiConfidence(saved.getAiConfidence());
            item.setAiAnalysisTime(saved.getAiAnalysisTime());
            item.setAiAnalysisStatus(saved.getAiAnalysisStatus());
            item.setAiAnalysisTrigger(saved.getAiAnalysisTrigger());
            item.setAiAnalysisError(saved.getAiAnalysisError());
            item.setAiLastAttemptTime(saved.getAiLastAttemptTime());
            item.setRiskLevel(saved.getRiskLevel());
            item.setRiskScore(saved.getRiskScore());
            item.setSchoolRelevanceScore(saved.getSchoolRelevanceScore());
            item.setTopicCategory(saved.getTopicCategory());
            item.setTopicSubCategory(saved.getTopicSubCategory());
        }
        return item;
    }

    @Override
    @Transactional
    public CampusClue convertResultToClue(Long monitorResultId, Long operatorUserId, String operatorName) {
        CampusMonitorResult result = requireResult(monitorResultId);
        if (result.getClueId() != null) {
            return campusClueService.detail(result.getClueId());
        }
        CampusClue existingClue = resolveExistingClueForResult(result, operatorUserId);
        if (existingClue != null) {
            return existingClue;
        }
        CampusIngestRecord record = result.getIngestRecordId() == null
                ? null
                : campusIngestRecordDao.selectByRecordId(result.getIngestRecordId());
        CampusClue clue = new CampusClue();
        clue.setClueTitle(StringUtils.defaultIfBlank(preferText(result.getTitle(), record == null ? null : record.getTitle()), "未命名监测线索"));
        clue.setClueContent(preferLongerText(result.getContent(), record == null ? null : record.getContent()));
        clue.setClueSource("monitor");
        clue.setSourcePlatform(StringUtils.defaultIfBlank(result.getPlatform(), record == null ? null : record.getPlatform()));
        clue.setOriginalUrl(StringUtils.defaultIfBlank(result.getOriginalUrl(), record == null ? null : record.getOriginalUrl()));
        clue.setPublishTime(result.getPublishTime() == null && record != null ? record.getPublishTime() : result.getPublishTime());
        clue.setDiscoverTime(new Date());
        clue.setInvolvedAccount(StringUtils.defaultIfBlank(result.getAuthorName(), record == null ? null : record.getAuthorName()));
        clue.setKeywords(joinTokens(result.getMatchedKeywords(), result.getMatchedNegativeWords()));
        clue.setRiskLevel(result.getRiskLevel());
        clue.setSchoolRelevanceScore(result.getSchoolRelevanceScore());
        clue.setSchoolRelevanceReason(result.getSchoolRelevanceReason());
        clue.setMatchedSchoolTerms(result.getMatchedSchoolTerms());
        clue.setExcludedReason(result.getExcludedReason());
        clue.setTopicCategory(result.getTopicCategory());
        clue.setTopicSubCategory(result.getTopicSubCategory());
        clue.setTopicReason(result.getTopicReason());
        clue.setSentiment(StringUtils.defaultIfBlank(result.getSentiment(), record == null ? null : record.getSentiment()));
        clue.setLanguage(StringUtils.defaultIfBlank(result.getLanguage(), record == null ? null : record.getLanguage()));
        clue.setRemark("由监测结果 " + monitorResultId + " 转入线索库");
        CampusClue saved = campusClueService.save(clue, operatorUserId, operatorName);
        campusMonitorResultDao.updateClue(monitorResultId, RESULT_CONVERTED, saved.getClueId(), operatorUserId);
        bindIngestRecordToClue(result.getIngestRecordId(), saved.getClueId(), operatorUserId);
        return saved;
    }

    private CampusAiRuntimeConfig resolveAiConfig(String featureCode) {
        return campusAiRuntimeService.resolveFeature(featureCode,
                "deepseek", "deepseek-chat", null, "DEEPSEEK_API_KEY", 180000);
    }

    private JSONObject buildTaskDiagnosisJson(CampusMonitorTask task) {
        JSONObject json = new JSONObject();
        json.put("monitorTaskId", String.valueOf(task.getMonitorTaskId()));
        json.put("taskName", task.getTaskName());
        json.put("monitorSubject", task.getMonitorSubject());
        json.put("subjectAliases", task.getSubjectAliases());
        json.put("keywords", task.getKeywords());
        json.put("keywordsI18n", task.getKeywordsI18n());
        json.put("negativeWords", task.getNegativeWords());
        json.put("negativeWordsI18n", task.getNegativeWordsI18n());
        json.put("excludeWords", task.getExcludeWords());
        json.put("excludeWordsI18n", task.getExcludeWordsI18n());
        json.put("platformScope", task.getPlatformScope());
        json.put("scanFrequencyMinutes", task.getScanFrequencyMinutes());
        json.put("scheduleEnabled", task.getScheduleEnabled());
        json.put("alertMode", task.getAlertMode());
        json.put("autoIngestEnabled", task.getAutoIngestEnabled());
        json.put("displayEnabled", task.getDisplayEnabled());
        json.put("taskStatus", task.getTaskStatus());
        json.put("ingestCapabilityStatus", task.getIngestCapabilityStatus());
        json.put("lastMatchCount", task.getLastMatchCount());
        json.put("displayResultCount", task.getDisplayResultCount());
        json.put("lastErrorMessage", task.getLastErrorMessage());
        return json;
    }

    private JSONObject buildTaskDiagnosisStats(List<CampusMonitorResult> recentResults) {
        JSONObject stats = new JSONObject();
        int total = recentResults == null ? 0 : recentResults.size();
        int negative = 0;
        int notAnalyzed = 0;
        Map<String, Integer> platformCounts = new LinkedHashMap<>();
        Map<String, Integer> topicCounts = new LinkedHashMap<>();
        if (recentResults != null) {
            for (CampusMonitorResult result : recentResults) {
                if ("negative".equalsIgnoreCase(result.getSentiment())) {
                    negative++;
                }
                if (StringUtils.isBlank(result.getAiHitRecommendation())) {
                    notAnalyzed++;
                }
                increment(platformCounts, StringUtils.defaultIfBlank(result.getPlatform(), "unknown"));
                increment(topicCounts, StringUtils.defaultIfBlank(result.getTopicCategory(), "unknown"));
            }
        }
        stats.put("recentSampleCount", total);
        stats.put("negativeCount", negative);
        stats.put("notAiAnalyzedCount", notAnalyzed);
        stats.put("platformCounts", platformCounts);
        stats.put("topicCounts", topicCounts);
        return stats;
    }

    private void increment(Map<String, Integer> map, String key) {
        map.put(key, map.containsKey(key) ? map.get(key) + 1 : 1);
    }

    private CampusMonitorTaskAiDiagnosis parseTaskDiagnosis(String content) {
        CampusMonitorTaskAiDiagnosis diagnosis = new CampusMonitorTaskAiDiagnosis();
        diagnosis.setRawText(content);
        JSONObject json = parseAiObject(content);
        if (json == null) {
            diagnosis.setSummary(StringUtils.left(StringUtils.defaultIfBlank(content, "AI未返回有效内容"), 1000));
            return diagnosis;
        }
        diagnosis.setSummary(StringUtils.left(json.getString("summary"), 1000));
        diagnosis.setKeywordSuggestions(jsonArrayToStringList(json.getJSONArray("keywordSuggestions")));
        diagnosis.setNegativeWordSuggestions(jsonArrayToStringList(json.getJSONArray("negativeWordSuggestions")));
        diagnosis.setExcludeWordSuggestions(jsonArrayToStringList(json.getJSONArray("excludeWordSuggestions")));
        diagnosis.setPlatformSuggestions(jsonArrayToStringList(json.getJSONArray("platformSuggestions")));
        diagnosis.setFrequencySuggestion(StringUtils.left(json.getString("frequencySuggestion"), 500));
        diagnosis.setAlertModeSuggestion(StringUtils.left(json.getString("alertModeSuggestion"), 500));
        diagnosis.setRisks(jsonArrayToStringList(json.getJSONArray("risks")));
        diagnosis.setSuggestions(jsonArrayToStringList(json.getJSONArray("suggestions")));
        return diagnosis;
    }

    private List<String> jsonArrayToStringList(JSONArray array) {
        List<String> values = new ArrayList<>();
        if (array == null) {
            return values;
        }
        for (int i = 0; i < array.size(); i++) {
            String value = StringUtils.trimToNull(array.getString(i));
            if (value != null) {
                values.add(StringUtils.left(value, 300));
            }
        }
        return values;
    }

    private String applyPromptTemplate(String template, Map<String, String> variables) {
        String result = StringUtils.defaultString(template);
        if (variables == null || variables.isEmpty()) {
            return result;
        }
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("${" + entry.getKey() + "}", StringUtils.defaultString(entry.getValue()));
        }
        return result;
    }

    private JSONObject parseAiObject(String content) {
        if (StringUtils.isBlank(content)) {
            return null;
        }
        String json = content.trim();
        if (json.startsWith("```")) {
            int start = json.indexOf('\n');
            if (start >= 0 && start < json.length() - 1) {
                json = json.substring(start + 1);
            }
            int end = json.lastIndexOf("```");
            if (end > 0) {
                json = json.substring(0, end);
            }
            json = json.trim();
        }
        if (!json.startsWith("{")) {
            int start = json.indexOf('{');
            int end = json.lastIndexOf('}');
            if (start >= 0 && end > start) {
                json = json.substring(start, end + 1);
            }
        }
        try {
            return JSON.parseObject(json);
        } catch (Exception ex) {
            return null;
        }
    }

    private String normalizeAiHitRecommendation(String value) {
        String normalized = StringUtils.lowerCase(StringUtils.trimToEmpty(value));
        if ("hit".equals(normalized) || "not_hit".equals(normalized) || "uncertain".equals(normalized)) {
            return normalized;
        }
        if ("true".equals(normalized) || "yes".equals(normalized) || "保留".equals(normalized) || "命中".equals(normalized)) {
            return "hit";
        }
        if ("false".equals(normalized) || "no".equals(normalized) || "忽略".equals(normalized) || "不命中".equals(normalized)) {
            return "not_hit";
        }
        return "uncertain";
    }

    private Integer normalizeSchoolRelevanceScore(String value) {
        String normalized = StringUtils.lowerCase(StringUtils.trimToEmpty(value));
        if (StringUtils.isBlank(normalized)) {
            return null;
        }
        if ("high".equals(normalized) || normalized.contains("高")) {
            return 90;
        }
        if ("medium".equals(normalized) || "mid".equals(normalized) || normalized.contains("中")) {
            return 60;
        }
        if ("low".equals(normalized) || normalized.contains("低")) {
            return 30;
        }
        if ("none".equals(normalized) || "no".equals(normalized)
                || normalized.contains("无") || normalized.contains("不相关")) {
            return 0;
        }
        try {
            return clamp(new BigDecimal(normalized).intValue(), 0, 100);
        } catch (Exception ex) {
            return null;
        }
    }

    private String normalizeAiRiskLevel(String value) {
        String normalized = CampusRiskLevel.normalizeForQuery(value);
        if (StringUtils.isBlank(normalized)) {
            return null;
        }
        try {
            return CampusRiskLevel.requireValid(normalized);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private boolean isAiAffirmative(String value) {
        String normalized = StringUtils.lowerCase(StringUtils.trimToEmpty(value));
        if (normalized.contains("不需要") || normalized.contains("无需") || normalized.contains("不应")) {
            return false;
        }
        return "true".equals(normalized)
                || "yes".equals(normalized)
                || "y".equals(normalized)
                || "1".equals(normalized)
                || "concern".equals(normalized)
                || "一般预警".equals(normalized)
                || "需要".equals(normalized)
                || normalized.contains("需要预警")
                || normalized.contains("应预警");
    }

    private String resolveAiAnalysisRiskLevel(CampusMonitorResult result,
                                              CampusIngestRecord ingestRecord,
                                              AiResultAnalysis analysis,
                                              String sentiment) {
        String recordRiskLevel = ingestRecord == null ? null : normalizeValidRiskLevel(ingestRecord.getRiskLevel());
        if (CampusRiskLevel.isNonNormal(recordRiskLevel)) {
            return recordRiskLevel;
        }
        String existingRiskLevel = normalizeValidRiskLevel(result.getRiskLevel());
        if (RISK_URGENT.equals(existingRiskLevel) || RISK_MAJOR.equals(existingRiskLevel)) {
            return existingRiskLevel;
        }
        if (RISK_URGENT.equals(analysis.riskLevel) || RISK_MAJOR.equals(analysis.riskLevel)) {
            return analysis.riskLevel;
        }
        if (RISK_CONCERN.equals(analysis.riskLevel)) {
            return RISK_CONCERN;
        }
        if (StringUtils.isNotBlank(result.getMatchedNegativeWords())) {
            return RISK_CONCERN;
        }
        if ("negative".equalsIgnoreCase(sentiment)
                && !AI_ANALYSIS_BASIS_TITLE.equals(analysis.analysisBasis)) {
            return RISK_CONCERN;
        }
        return RISK_NORMAL;
    }

    private String normalizeValidRiskLevel(String value) {
        String normalized = CampusRiskLevel.normalizeForQuery(value);
        if (StringUtils.isBlank(normalized)) {
            return null;
        }
        try {
            return CampusRiskLevel.requireValid(normalized);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private Integer resolveAiAnalysisRiskScore(String riskLevel, CampusMonitorResult result) {
        String normalized = CampusRiskLevel.normalizeOrDefault(riskLevel);
        if (RISK_URGENT.equals(normalized)) {
            return 90;
        }
        if (RISK_MAJOR.equals(normalized)) {
            return 70;
        }
        if (RISK_CONCERN.equals(normalized)) {
            int score = 45;
            if (StringUtils.isNotBlank(result.getMatchedNegativeWords())) {
                score += Math.min(35, splitTokens(result.getMatchedNegativeWords()).size() * 10);
            }
            return Math.min(score, 69);
        }
        return 0;
    }

    private Integer clamp(Integer value, int min, int max) {
        if (value == null) {
            return null;
        }
        return Math.max(min, Math.min(max, value));
    }

    private String preferText(String primary, String fallback) {
        return StringUtils.defaultIfBlank(primary, fallback);
    }

    private String preferLongerText(String primary, String fallback) {
        String primaryText = StringUtils.trimToNull(primary);
        String fallbackText = StringUtils.trimToNull(fallback);
        if (primaryText == null) {
            return fallbackText;
        }
        if (fallbackText == null) {
            return primaryText;
        }
        return fallbackText.length() > primaryText.length() ? fallbackText : primaryText;
    }

    private CampusClue resolveExistingClueForResult(CampusMonitorResult result, Long operatorUserId) {
        if (result == null || result.getIngestRecordId() == null) {
            return null;
        }
        CampusIngestRecord record = campusIngestRecordDao.selectByRecordId(result.getIngestRecordId());
        if (record == null || !INGEST_TARGET_CLUE.equals(record.getTargetType()) || record.getTargetId() == null) {
            return null;
        }
        CampusClue existing;
        try {
            existing = campusClueService.detail(record.getTargetId());
        } catch (RuntimeException ex) {
            return null;
        }
        campusMonitorResultDao.updateClue(result.getMonitorResultId(), RESULT_CONVERTED, existing.getClueId(), operatorUserId);
        return existing;
    }

    private void bindIngestRecordToClue(Long ingestRecordId, Long clueId, Long operatorUserId) {
        if (ingestRecordId == null || clueId == null) {
            return;
        }
        CampusIngestRecord record = campusIngestRecordDao.selectByRecordId(ingestRecordId);
        if (record == null) {
            return;
        }
        if (StringUtils.isBlank(record.getTargetType()) || INGEST_TARGET_CLUE.equals(record.getTargetType())) {
            campusIngestRecordDao.updateStatus(ingestRecordId, INGEST_STATUS_CONVERTED,
                    INGEST_TARGET_CLUE, clueId, null, operatorUserId);
        }
    }

    @Override
    public PageInfo<CampusMonitorWatchTarget> listWatchTargets(Integer pageNum,
                                                               Integer pageSize,
                                                               Long monitorTaskId,
                                                               String targetType,
                                                               String platform,
                                                               String keyword,
                                                               String targetStatus) {
        PageHelper.startPage(defaultPageNum(pageNum), defaultPageSize(pageSize));
        return new PageInfo<>(campusMonitorWatchTargetDao.list(monitorTaskId, targetType, platform,
                keyword, targetStatus));
    }

    @Override
    public CampusMonitorWatchTarget saveWatchTarget(CampusMonitorWatchTarget target, Long operatorUserId) {
        validateWatchTarget(target);
        requireTask(target.getMonitorTaskId());
        rejectForbiddenText(joinText(target.getAccountName(), target.getAccountUid(), target.getLinkUrl(),
                target.getAuthorizationScope(), target.getKeywordScope(), target.getRemark()));
        if (target.getTargetId() == null) {
            target.setTargetId(SnowflakeUtil.getId());
            target.setTargetStatus(StringUtils.defaultIfBlank(target.getTargetStatus(), WATCH_STATUS_ACTIVE));
            target.setDeleted(0);
            target.setCreateUserId(operatorUserId);
            target.setUpdateUserId(operatorUserId);
            campusMonitorWatchTargetDao.insert(target);
            return campusMonitorWatchTargetDao.selectByTargetId(target.getTargetId());
        }
        requireWatchTarget(target.getTargetId());
        target.setUpdateUserId(operatorUserId);
        campusMonitorWatchTargetDao.update(target);
        return campusMonitorWatchTargetDao.selectByTargetId(target.getTargetId());
    }

    @Override
    public CampusMonitorWatchTarget createWatchTargetFromResult(Long monitorResultId,
                                                                Long monitorTaskId,
                                                                String targetType,
                                                                Long operatorUserId) {
        CampusMonitorResult result = requireResult(monitorResultId);
        CampusMonitorWatchTarget target = new CampusMonitorWatchTarget();
        target.setMonitorTaskId(monitorTaskId == null ? result.getMonitorTaskId() : monitorTaskId);
        target.setTargetType(StringUtils.defaultIfBlank(targetType, WATCH_TARGET_ACCOUNT));
        target.setPlatform(result.getPlatform());
        target.setAccountName(result.getAuthorName());
        target.setLinkUrl(result.getOriginalUrl());
        target.setSourceObjectType("result");
        target.setSourceObjectId(result.getMonitorResultId());
        target.setAuthorizationScope("由监测结果加入重点监控，请人工复核授权范围");
        target.setKeywordScope(joinTokens(result.getMatchedKeywords(), result.getMatchedNegativeWords()));
        target.setRemark("监测结果一键加入");
        return saveWatchTarget(target, operatorUserId);
    }

    @Override
    public void deleteWatchTarget(Long targetId, Long operatorUserId) {
        requireWatchTarget(targetId);
        campusMonitorWatchTargetDao.logicalDelete(targetId, operatorUserId);
    }

    @Override
    public PageInfo<CampusAlert> listAlerts(Integer pageNum,
                                            Integer pageSize,
                                            Long monitorTaskId,
                                            String keyword,
                                            String riskLevel,
                                            String alertStatus) {
        PageHelper.startPage(defaultPageNum(pageNum), defaultPageSize(pageSize));
        return new PageInfo<>(campusAlertDao.listMonitorAlerts(monitorTaskId, keyword,
                CampusRiskLevel.normalizeForQuery(riskLevel), alertStatus));
    }

    @Override
    public CampusAlert handleAlert(Long alertId,
                                   String alertStatus,
                                   String handleOpinion,
                                   Long operatorUserId) {
        CampusAlert alert = campusAlertService.handleAlert(alertId, alertStatus, handleOpinion, operatorUserId);
        if (alert != null && ALERT_SOURCE_MONITOR.equals(alert.getAlertSource())
                && alert.getSourceObjectId() != null) {
            String resultStatus = RESULT_HANDLED;
            if (RESULT_IGNORED.equals(alertStatus)) {
                resultStatus = RESULT_IGNORED;
            }
            campusMonitorResultDao.updateStatus(alert.getSourceObjectId(), resultStatus, alert.getAlertId(), operatorUserId);
        }
        return alert;
    }

    @Override
    public PageInfo<CampusMonitorRunLog> listRunLogs(Integer pageNum, Integer pageSize, Long monitorTaskId) {
        if (monitorTaskId == null) {
            throw new IllegalArgumentException("监测任务ID不能为空");
        }
        PageHelper.startPage(defaultPageNum(pageNum), defaultPageSize(pageSize));
        return new PageInfo<>(campusMonitorRunLogDao.listByTaskId(monitorTaskId));
    }

    @Override
    public Map<String, Integer> overview() {
        Map<String, Integer> overview = new HashMap<>();
        overview.put("activeTaskCount", campusMonitorTaskDao.countByStatus(TASK_ACTIVE));
        overview.put("todayResultCount", campusMonitorResultDao.countToday());
        overview.put("pendingAlertCount", campusAlertDao.countBySourceAndStatus(ALERT_SOURCE_MONITOR, "pending"));
        return overview;
    }

    private void scanRecord(CampusMonitorTask task,
                            CampusIngestRecord record,
                            List<CampusMonitorWatchTarget> watchTargets,
                            Long operatorUserId,
                            MonitorCounter counter) {
        counter.scannedCount++;
        if (record == null || record.getRecordId() == null) {
            return;
        }
        if (!platformMatches(task.getPlatformScope(), record.getPlatform())) {
            return;
        }
        Set<String> matchedWatchTargets = matchedWatchTargetLabels(record, watchTargets);
        boolean watchTargetScoped = watchTargets != null && !watchTargets.isEmpty();
        if (watchTargetScoped && matchedWatchTargets.isEmpty()) {
            return;
        }
        String text = joinText(record.getTitle(), record.getContent(), record.getKeywords(), record.getAuthorName());
        if (containsAny(text, taskTokens(task.getExcludeWords(), task.getExcludeWordsI18n(), record.getLanguage()))) {
            return;
        }

        Set<String> subjectTokens = splitTokens(joinTokens(task.getMonitorSubject(), task.getSubjectAliases()));
        Set<String> keywordTokens = taskTokens(task.getKeywords(), task.getKeywordsI18n(), record.getLanguage());
        Set<String> negativeTokens = taskTokens(task.getNegativeWords(), task.getNegativeWordsI18n(), record.getLanguage());
        negativeTokens.addAll(dictTokens(DICT_NEGATIVE_WORD, record.getLanguage()));
        negativeTokens.addAll(dictTokens(DICT_RISK_WORD, record.getLanguage()));
        addWatchTargetKeywordTokens(keywordTokens, watchTargets, record);
        if (keywordTokens.isEmpty()) {
            return;
        }
        Set<String> matchedSubjects = matchTokens(text, subjectTokens);
        if (matchedSubjects.isEmpty() && watchTargetScoped) {
            matchedSubjects.addAll(matchedWatchTargets);
        }
        Set<String> matchedKeywords = matchTokens(text, keywordTokens);
        if (matchedKeywords.isEmpty()) {
            return;
        }
        Set<String> matchedNegativeWords = matchTokens(text, negativeTokens);

        counter.matchCount++;
        boolean negative = isNegative(record, matchedNegativeWords);
        if (negative) {
            counter.negativeCount++;
        }
        CampusMonitorResult existing = campusMonitorResultDao.selectByTaskAndRecord(task.getMonitorTaskId(), record.getRecordId());
        if (existing != null) {
            campusMonitorResultDao.updateSnapshot(existing.getMonitorResultId(), record, operatorUserId);
            return;
        }

        CampusMonitorResult result = buildResult(task, record, matchedSubjects, matchedKeywords,
                matchedNegativeWords, negative, operatorUserId);
        try {
            campusMonitorResultDao.insert(result);
        } catch (DuplicateKeyException ex) {
            return;
        }
        CampusMonitorResult saved = campusMonitorResultDao.selectByResultId(result.getMonitorResultId());
        if (shouldAlert(task, saved, negative)) {
            CampusAlert alert = createAlertForResult(saved, operatorUserId);
            campusMonitorResultDao.updateStatus(saved.getMonitorResultId(), RESULT_ALERTED, alert.getAlertId(), operatorUserId);
            counter.alertCount++;
        }
    }

    private CampusMonitorResult buildResult(CampusMonitorTask task,
                                            CampusIngestRecord record,
                                            Set<String> matchedSubjects,
                                            Set<String> matchedKeywords,
                                            Set<String> matchedNegativeWords,
                                            boolean negative,
                                            Long operatorUserId) {
        int riskScore = calculateRiskScore(record, matchedNegativeWords, negative);
        CampusMonitorResult result = new CampusMonitorResult();
        result.setMonitorResultId(SnowflakeUtil.getId());
        result.setMonitorTaskId(task.getMonitorTaskId());
        result.setIngestRecordId(record.getRecordId());
        result.setTitle(summary(defaultTitle(record.getTitle()), 512));
        result.setContent(summary(record.getContent(), 4000));
        result.setOriginalUrl(summary(record.getOriginalUrl(), 1024));
        result.setPlatform(summary(record.getPlatform(), 64));
        result.setAuthorName(summary(record.getAuthorName(), 255));
        result.setPublishTime(record.getPublishTime());
        result.setLanguage(record.getLanguage());
        result.setMatchedSubjects(summary(StringUtils.join(matchedSubjects, ","), 512));
        result.setMatchedKeywords(summary(StringUtils.join(matchedKeywords, ","), 1024));
        result.setMatchedNegativeWords(summary(StringUtils.join(matchedNegativeWords, ","), 1024));
        result.setSentiment(negative ? "negative" : CampusSentimentNormalizer.normalizeOrDefault(record.getSentiment(), "neutral"));
        result.setRiskLevel(resolveRiskLevel(record.getRiskLevel(), riskScore, negative));
        result.setRiskScore(riskScore);
        CampusSchoolRelevance relevance = schoolRelevanceService.evaluate(task, record, matchedSubjects, matchedKeywords);
        result.setSchoolRelevanceScore(relevance.getScore());
        result.setSchoolRelevanceReason(summary(relevance.getReason(), 1024));
        result.setMatchedSchoolTerms(summary(relevance.getMatchedSchoolTerms(), 512));
        result.setExcludedReason(summary(relevance.getExcludedReason(), 512));
        CampusTopicClassification topic = topicClassifier.classify(record.getTitle(), record.getContent(),
                joinTokens(record.getKeywords(), result.getMatchedKeywords(), result.getMatchedNegativeWords()),
                campusDictDao.enabledItems(CampusTopicClassifier.DICT_TYPE));
        result.setTopicCategory(summary(topic.getTopicCategory(), 64));
        result.setTopicSubCategory(summary(topic.getTopicSubCategory(), 64));
        result.setTopicReason(summary(topic.getReason(), 1024));
        result.setAiAnalysisStatus(AI_STATUS_PENDING);
        result.setAiAnalysisTrigger(AI_TRIGGER_AUTO);
        result.setAiAnalysisError(null);
        result.setAiLastAttemptTime(null);
        result.setResultStatus(RESULT_PENDING);
        result.setLikeCount(record.getLikeCount());
        result.setCommentCount(record.getCommentCount());
        result.setShareCount(record.getShareCount());
        result.setCollectCount(record.getCollectCount());
        result.setViewCount(record.getViewCount());
        result.setDeleted(0);
        result.setCreateUserId(operatorUserId);
        result.setUpdateUserId(operatorUserId);
        return result;
    }

    private CampusAlert createAlertForResult(CampusMonitorResult result, Long operatorUserId) {
        CampusAlert alert = new CampusAlert();
        alert.setAlertTitle(summary("监测告警：" + defaultTitle(result.getTitle()), 255));
        alert.setAlertContent(summary(buildAlertContent(result), 4000));
        alert.setAlertSource(ALERT_SOURCE_MONITOR);
        alert.setSourceObjectId(result.getMonitorResultId());
        alert.setRiskLevel(CampusRiskLevel.normalizeOrDefault(StringUtils.defaultIfBlank(result.getRiskLevel(), RISK_CONCERN)));
        alert.setMatchedKeywords(summary(joinTokens(result.getMatchedKeywords(), result.getMatchedNegativeWords()), 512));
        alert.setEvidenceJson(summary(buildAlertEvidenceJson(result), 4000));
        alert.setAlertStatus("pending");
        return campusAlertService.createAlert(alert, operatorUserId);
    }

    private String buildAlertContent(CampusMonitorResult result) {
        StringBuilder builder = new StringBuilder();
        builder.append("命中主体：").append(StringUtils.defaultString(result.getMatchedSubjects(), "-")).append('\n');
        builder.append("命中关键词：").append(StringUtils.defaultString(result.getMatchedKeywords(), "-")).append('\n');
        builder.append("命中负面词：").append(StringUtils.defaultString(result.getMatchedNegativeWords(), "-")).append('\n');
        builder.append("学校相关性：").append(StringUtils.defaultString(result.getSchoolRelevanceScore() == null
                ? null : String.valueOf(result.getSchoolRelevanceScore()), "-"))
                .append("（").append(StringUtils.defaultString(result.getSchoolRelevanceReason(), "-")).append("）").append('\n');
        builder.append("主题分类：").append(StringUtils.defaultString(result.getTopicCategory(), "-")).append('\n');
        builder.append("来源平台：").append(StringUtils.defaultString(result.getPlatform(), "-")).append('\n');
        if (StringUtils.isNotBlank(result.getOriginalUrl())) {
            builder.append("原文链接：").append(result.getOriginalUrl()).append('\n');
        }
        builder.append('\n').append(StringUtils.defaultString(result.getContent(), ""));
        return builder.toString();
    }

    private String buildAlertEvidenceJson(CampusMonitorResult result) {
        JSONObject evidence = new JSONObject();
        evidence.put("source", ALERT_SOURCE_MONITOR);
        evidence.put("sourceObjectId", result.getMonitorResultId());
        evidence.put("riskLevel", result.getRiskLevel());
        evidence.put("riskScore", result.getRiskScore());
        evidence.put("schoolRelevanceScore", result.getSchoolRelevanceScore());
        evidence.put("schoolRelevanceReason", result.getSchoolRelevanceReason());
        evidence.put("matchedSchoolTerms", result.getMatchedSchoolTerms());
        evidence.put("topicCategory", result.getTopicCategory());
        evidence.put("topicSubCategory", result.getTopicSubCategory());
        evidence.put("topicReason", result.getTopicReason());
        evidence.put("matchedKeywords", result.getMatchedKeywords());
        evidence.put("matchedNegativeWords", result.getMatchedNegativeWords());
        evidence.put("platform", result.getPlatform());
        evidence.put("originalUrl", result.getOriginalUrl());
        return evidence.toJSONString();
    }

    private boolean shouldAlert(CampusMonitorTask task, CampusMonitorResult result, boolean negative) {
        String alertMode = StringUtils.defaultIfBlank(task.getAlertMode(), ALERT_MODE_NEGATIVE_ONLY);
        if (ALERT_MODE_MANUAL.equals(alertMode)) {
            return false;
        }
        if (ALERT_MODE_ALL_HITS.equals(alertMode)) {
            return negative || isRiskResult(result);
        }
        return negative;
    }

    private boolean isRiskResult(CampusMonitorResult result) {
        if (result == null) {
            return false;
        }
        if (CampusRiskLevel.isNonNormal(result.getRiskLevel())) {
            return true;
        }
        Integer riskScore = result.getRiskScore();
        return riskScore != null && riskScore >= 45;
    }

    private boolean isNegative(CampusIngestRecord record, Set<String> matchedNegativeWords) {
        if (matchedNegativeWords != null && !matchedNegativeWords.isEmpty()) {
            return true;
        }
        if ("negative".equalsIgnoreCase(StringUtils.defaultString(record.getSentiment()))) {
            return true;
        }
        return CampusRiskLevel.isNonNormal(record.getRiskLevel());
    }

    private int calculateRiskScore(CampusIngestRecord record,
                                   Set<String> matchedNegativeWords,
                                   boolean negative) {
        int score = 0;
        if (matchedNegativeWords != null && !matchedNegativeWords.isEmpty()) {
            score += Math.min(60, matchedNegativeWords.size() * 20);
        }
        if ("negative".equalsIgnoreCase(StringUtils.defaultString(record.getSentiment()))) {
            score += 30;
        }
        String normalizedRecordRiskLevel = CampusRiskLevel.normalizeOrDefault(record.getRiskLevel());
        if (RISK_URGENT.equals(normalizedRecordRiskLevel)) {
            score = Math.max(score, 90);
        } else if (RISK_MAJOR.equals(normalizedRecordRiskLevel)) {
            score = Math.max(score, 70);
        } else if (RISK_CONCERN.equals(normalizedRecordRiskLevel)) {
            score = Math.max(score, 45);
        }
        if (negative) {
            score = Math.max(score, 45);
        }
        return Math.min(score, 100);
    }

    private String resolveRiskLevel(String recordRiskLevel, int riskScore, boolean negative) {
        String normalizedRecordRiskLevel = CampusRiskLevel.normalizeOrDefault(recordRiskLevel);
        if (RISK_URGENT.equals(normalizedRecordRiskLevel) || riskScore >= 90) {
            return RISK_URGENT;
        }
        if (RISK_MAJOR.equals(normalizedRecordRiskLevel) || riskScore >= 70) {
            return RISK_MAJOR;
        }
        if (RISK_CONCERN.equals(normalizedRecordRiskLevel) || negative || riskScore >= 45) {
            return RISK_CONCERN;
        }
        return RISK_NORMAL;
    }

    private boolean platformMatches(String platformScope, String platform) {
        Set<String> scopes = splitTokens(platformScope);
        if (scopes.isEmpty()) {
            return true;
        }
        String normalizedPlatform = normalizePlatformCode(platform);
        for (String scope : scopes) {
            String normalizedScope = normalizePlatformCode(scope);
            if ("*".equals(normalizedScope) || "all".equalsIgnoreCase(normalizedScope)) {
                return true;
            }
            if (StringUtils.equalsIgnoreCase(normalizedScope, normalizedPlatform)) {
                return true;
            }
        }
        return false;
    }

    private boolean watchTargetMatches(CampusIngestRecord record, List<CampusMonitorWatchTarget> watchTargets) {
        if (watchTargets == null || watchTargets.isEmpty()) {
            return true;
        }
        for (CampusMonitorWatchTarget target : watchTargets) {
            if (singleWatchTargetMatches(record, target)) {
                return true;
            }
        }
        return false;
    }

    private Set<String> matchedWatchTargetLabels(CampusIngestRecord record, List<CampusMonitorWatchTarget> watchTargets) {
        Set<String> labels = new LinkedHashSet<>();
        if (watchTargets == null || watchTargets.isEmpty()) {
            return labels;
        }
        for (CampusMonitorWatchTarget target : watchTargets) {
            if (singleWatchTargetMatches(record, target)) {
                labels.add(watchTargetLabel(target));
            }
        }
        return labels;
    }

    private boolean singleWatchTargetMatches(CampusIngestRecord record, CampusMonitorWatchTarget target) {
        if (record == null || target == null) {
            return false;
        }
        if (StringUtils.isNotBlank(target.getPlatform())
                && StringUtils.isNotBlank(record.getPlatform())
                && !StringUtils.equalsIgnoreCase(target.getPlatform(), record.getPlatform())) {
            return false;
        }
        String haystack = joinText(record.getAuthorName(), record.getOriginalUrl(), record.getTitle(),
                record.getContent(), record.getRawData());
        if (WATCH_TARGET_ACCOUNT.equals(target.getTargetType())) {
            if (target.getAccountId() != null && target.getAccountId().equals(record.getAccountId())) {
                return true;
            }
            return containsAny(haystack, splitTokens(joinTokens(target.getAccountName(), target.getAccountUid(), target.getLinkUrl())));
        }
        if (WATCH_TARGET_LINK.equals(target.getTargetType())) {
            return linkMatches(record.getOriginalUrl(), target.getLinkUrl());
        }
        return containsAny(haystack, splitTokens(joinTokens(target.getAccountName(), target.getAccountUid(), target.getLinkUrl())));
    }

    private boolean linkMatches(String recordUrl, String targetUrl) {
        String normalizedRecordUrl = normalizeUrl(recordUrl);
        String normalizedTargetUrl = normalizeUrl(targetUrl);
        if (StringUtils.isBlank(normalizedRecordUrl) || StringUtils.isBlank(normalizedTargetUrl)) {
            return false;
        }
        return StringUtils.containsIgnoreCase(normalizedRecordUrl, normalizedTargetUrl)
                || StringUtils.containsIgnoreCase(normalizedTargetUrl, normalizedRecordUrl);
    }

    private String normalizeUrl(String value) {
        String normalized = StringUtils.trimToEmpty(value);
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private String watchTargetLabel(CampusMonitorWatchTarget target) {
        if (target == null) {
            return "重点目标";
        }
        if (WATCH_TARGET_ACCOUNT.equals(target.getTargetType())) {
            return "重点账号:" + StringUtils.defaultIfBlank(target.getAccountName(),
                    StringUtils.defaultIfBlank(target.getAccountUid(), String.valueOf(target.getTargetId())));
        }
        if (WATCH_TARGET_LINK.equals(target.getTargetType())) {
            return "指定链接:" + StringUtils.defaultIfBlank(target.getLinkUrl(), String.valueOf(target.getTargetId()));
        }
        return "重点目标:" + StringUtils.defaultIfBlank(target.getTargetType(), String.valueOf(target.getTargetId()));
    }

    private void addWatchTargetKeywordTokens(Set<String> keywordTokens,
                                             List<CampusMonitorWatchTarget> watchTargets,
                                             CampusIngestRecord record) {
        if (keywordTokens == null || watchTargets == null || watchTargets.isEmpty()) {
            return;
        }
        for (CampusMonitorWatchTarget target : watchTargets) {
            if (singleWatchTargetMatches(record, target)) {
                keywordTokens.addAll(splitTokens(target.getKeywordScope()));
            }
        }
    }

    private Date resolveStartTime(CampusMonitorTask task, Date endTime) {
        Date safeEndTime = endTime == null ? new Date() : endTime;
        Date lastRunTime = task == null ? null : task.getLastRunTime();
        long endMillis = safeEndTime.getTime();
        long startMillis;
        if (lastRunTime != null && lastRunTime.before(safeEndTime)) {
            startMillis = lastRunTime.getTime() - safeScanOverlapMinutes() * 60L * 1000L;
        } else {
            startMillis = endMillis - safeInitialScanWindowHours() * 60L * 60L * 1000L;
        }
        int maxHours = safeMaxScanWindowHours();
        if (maxHours > 0) {
            long earliest = endMillis - maxHours * 60L * 60L * 1000L;
            startMillis = Math.max(startMillis, earliest);
        }
        return new Date(Math.min(startMillis, endMillis));
    }

    private int safeScanFrequencyMinutes(CampusMonitorTask task) {
        int minutes = task == null || task.getScanFrequencyMinutes() == null || task.getScanFrequencyMinutes() <= 0
                ? DEFAULT_SCAN_FREQUENCY_MINUTES
                : task.getScanFrequencyMinutes();
        return Math.max(minutes, MIN_SCAN_FREQUENCY_MINUTES);
    }

    private int safeScanOverlapMinutes() {
        if (scanOverlapMinutes == null) {
            return DEFAULT_SCAN_OVERLAP_MINUTES;
        }
        return Math.max(0, Math.min(scanOverlapMinutes, 60));
    }

    private int safeInitialScanWindowHours() {
        if (initialScanWindowHours == null || initialScanWindowHours <= 0) {
            return DEFAULT_INITIAL_SCAN_WINDOW_HOURS;
        }
        return Math.min(initialScanWindowHours, 168);
    }

    private int safeMaxScanWindowHours() {
        if (maxScanWindowHours == null) {
            return DEFAULT_MAX_SCAN_WINDOW_HOURS;
        }
        return Math.max(0, Math.min(maxScanWindowHours, 168));
    }

    private int cleanupExpiredResults(Integer retentionDays, int batchSize) {
        Date expireBefore = expireBefore(retentionDays, DEFAULT_RESULT_RETENTION_DAYS);
        if (expireBefore == null) {
            return 0;
        }
        int total = 0;
        for (int i = 0; i < MAX_CLEANUP_BATCHES; i++) {
            int affected = campusMonitorResultDao.logicalDeleteBefore(expireBefore, batchSize, SYSTEM_USER_ID);
            if (affected <= 0) {
                break;
            }
            total += affected;
            if (affected < batchSize) {
                break;
            }
        }
        return total;
    }

    private int cleanupExpiredRunLogs(Integer retentionDays, int batchSize) {
        Date expireBefore = expireBefore(retentionDays, DEFAULT_RUN_LOG_RETENTION_DAYS);
        if (expireBefore == null) {
            return 0;
        }
        int total = 0;
        for (int i = 0; i < MAX_CLEANUP_BATCHES; i++) {
            int affected = campusMonitorRunLogDao.deleteBefore(expireBefore, batchSize);
            if (affected <= 0) {
                break;
            }
            total += affected;
            if (affected < batchSize) {
                break;
            }
        }
        return total;
    }

    private Date expireBefore(Integer retentionDays, int defaultDays) {
        int days;
        if (retentionDays == null) {
            days = defaultDays;
        } else if (retentionDays <= 0) {
            return null;
        } else {
            days = Math.min(retentionDays, 3650);
        }
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -days);
        return calendar.getTime();
    }

    private int safeCleanupBatchSize(Integer batchSize) {
        if (batchSize == null || batchSize <= 0) {
            return DEFAULT_CLEANUP_BATCH_SIZE;
        }
        return Math.min(batchSize, MAX_CLEANUP_BATCH_SIZE);
    }

    private void validateTask(CampusMonitorTask task) {
        if (task == null) {
            throw new IllegalArgumentException("监测任务不能为空");
        }
        if (StringUtils.isBlank(task.getTaskName())) {
            throw new IllegalArgumentException("任务名称不能为空");
        }
        if (StringUtils.isBlank(task.getMonitorSubject())) {
            throw new IllegalArgumentException("监测主体不能为空");
        }
        if (StringUtils.isBlank(task.getKeywords()) && StringUtils.isBlank(task.getKeywordsI18n())) {
            throw new IllegalArgumentException("关键词不能为空");
        }
        if (StringUtils.isNotBlank(task.getTaskStatus())) {
            validateTaskStatus(task.getTaskStatus());
        }
        if (StringUtils.isNotBlank(task.getAlertMode())) {
            validateAlertMode(task.getAlertMode());
        }
        if (task.getScheduleEnabled() != null && task.getScheduleEnabled() != 0 && task.getScheduleEnabled() != 1) {
            throw new IllegalArgumentException("自动扫描开关不合法");
        }
        if (task.getScanFrequencyMinutes() != null && task.getScanFrequencyMinutes() < MIN_SCAN_FREQUENCY_MINUTES) {
            throw new IllegalArgumentException("自动扫描频率不能小于5分钟");
        }
    }

    private void validateTaskStatus(String taskStatus) {
        if (!TASK_ACTIVE.equals(taskStatus) && !TASK_PAUSED.equals(taskStatus) && !TASK_DISABLED.equals(taskStatus)) {
            throw new IllegalArgumentException("监测任务状态不合法");
        }
    }

    private void validateAlertMode(String alertMode) {
        if (!ALERT_MODE_NEGATIVE_ONLY.equals(alertMode)
                && !ALERT_MODE_ALL_HITS.equals(alertMode)
                && !ALERT_MODE_MANUAL.equals(alertMode)) {
            throw new IllegalArgumentException("告警模式不合法");
        }
    }

    private CampusMonitorTask requireTask(Long monitorTaskId) {
        if (monitorTaskId == null) {
            throw new IllegalArgumentException("监测任务ID不能为空");
        }
        CampusMonitorTask task = campusMonitorTaskDao.selectByTaskId(monitorTaskId);
        if (task == null) {
            throw new IllegalArgumentException("监测任务不存在");
        }
        return task;
    }

    private CampusMonitorTask resolveTaskForSave(CampusMonitorTask task) {
        if (task.getMonitorTaskId() != null) {
            CampusMonitorTask existing = campusMonitorTaskDao.selectByTaskId(task.getMonitorTaskId());
            if (existing != null) {
                return existing;
            }
        }
        if (task.getId() != null) {
            CampusMonitorTask existing = campusMonitorTaskDao.selectById(task.getId());
            if (existing != null) {
                return existing;
            }
        }
        throw new IllegalArgumentException("监测任务不存在");
    }

    private CampusMonitorResult requireResult(Long monitorResultId) {
        if (monitorResultId == null) {
            throw new IllegalArgumentException("监测结果ID不能为空");
        }
        CampusMonitorResult result = campusMonitorResultDao.selectByResultId(monitorResultId);
        if (result == null) {
            throw new IllegalArgumentException("监测结果不存在");
        }
        return result;
    }

    private CampusMonitorWatchTarget requireWatchTarget(Long targetId) {
        if (targetId == null) {
            throw new IllegalArgumentException("监控目标ID不能为空");
        }
        CampusMonitorWatchTarget target = campusMonitorWatchTargetDao.selectByTargetId(targetId);
        if (target == null) {
            throw new IllegalArgumentException("监控目标不存在");
        }
        return target;
    }

    private void setTaskDefaults(CampusMonitorTask task) {
        if (task.getScanFrequencyMinutes() == null || task.getScanFrequencyMinutes() <= 0) {
            task.setScanFrequencyMinutes(DEFAULT_SCAN_FREQUENCY_MINUTES);
        }
        if (task.getScheduleEnabled() == null) {
            task.setScheduleEnabled(0);
        } else {
            task.setScheduleEnabled(task.getScheduleEnabled() == 1 ? 1 : 0);
        }
        task.setDisplayEnabled(task.getDisplayEnabled() == null || task.getDisplayEnabled() != 0 ? 1 : 0);
        task.setAutoIngestEnabled(task.getAutoIngestEnabled() == null || task.getAutoIngestEnabled() != 0 ? 1 : 0);
        if (StringUtils.isBlank(task.getAlertMode())) {
            task.setAlertMode(ALERT_MODE_NEGATIVE_ONLY);
        }
        if (StringUtils.isBlank(task.getTaskStatus())) {
            task.setTaskStatus(TASK_ACTIVE);
        }
        if (task.getScheduleEnabled() == 1 && task.getNextRunTime() == null) {
            task.setNextRunTime(new Date());
        }
        if (StringUtils.isBlank(task.getIngestCapabilityStatus())) {
            task.setIngestCapabilityStatus(AUTO_INGEST_PENDING);
        }
        task.setDeleted(0);
    }

    private AutoIngestOutcome syncTaskIngestScope(CampusMonitorTask task, Long operatorUserId, boolean runNow) {
        if (task == null || task.getMonitorTaskId() == null) {
            return AutoIngestOutcome.unsupported("监测任务不存在");
        }
        if (!isAutoIngestEnabled(task)) {
            syncIngestTaskBindings(task.getMonitorTaskId(), task.getIngestTaskIds(), operatorUserId);
            return AutoIngestOutcome.ready();
        }
        AutoIngestOutcome outcome = ensureAutoIngestTasks(task, operatorUserId, runNow);
        campusMonitorTaskDao.updateIngestCapability(task.getMonitorTaskId(), outcome.getLastCollectTime(),
                outcome.getStatus(), outcome.success() ? null : summary(outcome.errorMessage(), 1024), operatorUserId);
        return outcome;
    }

    private AutoIngestOutcome ensureAutoIngestTasks(CampusMonitorTask task, Long operatorUserId, boolean runNow) {
        AutoIngestSelection selection = resolveAutoIngestSelection(task.getPlatformScope());
        AutoIngestOutcome outcome = new AutoIngestOutcome();
        outcome.addUnsupported(selection.unsupportedPlatforms);
        if (selection.specs.isEmpty()) {
            campusMonitorIngestTaskRelationDao.softDeleteAll(task.getMonitorTaskId(), operatorUserId);
            outcome.setStatus(AUTO_INGEST_UNSUPPORTED);
            if (!selection.unsupportedPlatforms.isEmpty()) {
                outcome.addError("当前平台暂未配置可自动触发的接入适配器：" + StringUtils.join(selection.unsupportedPlatforms, "、"));
            } else {
                outcome.addError("未选择可自动触发的接入平台");
            }
            return outcome;
        }

        List<Long> boundTaskIds = new ArrayList<>();
        for (AutoIngestPlatformSpec spec : selection.specs) {
            try {
                CampusIngestSource source = ensureAutoIngestSource(spec, operatorUserId);
                CampusIngestTask ingestTask = ensureAutoIngestTask(task, spec, source, operatorUserId);
                if (ingestTask == null || ingestTask.getTaskId() == null) {
                    outcome.addError(spec.label + "接入任务创建失败");
                    continue;
                }
                boundTaskIds.add(ingestTask.getTaskId());
                if (runNow) {
                    runAutoIngestTask(spec, ingestTask, operatorUserId, outcome);
                }
            } catch (RuntimeException ex) {
                outcome.addError(spec.label + "：" + summary(ex.getMessage(), 200));
            }
        }

        if (!boundTaskIds.isEmpty()) {
            campusMonitorIngestTaskRelationDao.softDeleteMissing(task.getMonitorTaskId(), boundTaskIds, operatorUserId);
            for (Long ingestTaskId : boundTaskIds) {
                campusMonitorIngestTaskRelationDao.upsert(SnowflakeUtil.getId(), task.getMonitorTaskId(), ingestTaskId, operatorUserId);
            }
        }

        if (boundTaskIds.isEmpty()) {
            outcome.setStatus(AUTO_INGEST_FAILED);
        } else if (outcome.hasErrors() || !selection.unsupportedPlatforms.isEmpty()) {
            outcome.setStatus(AUTO_INGEST_PARTIAL);
        } else {
            outcome.setStatus(AUTO_INGEST_READY);
        }
        return outcome;
    }

    private CampusIngestSource ensureAutoIngestSource(AutoIngestPlatformSpec spec, Long operatorUserId) {
        List<CampusIngestSource> sources = campusIngestSourceDao.list(null, spec.sourceType, spec.sourcePlatform, 1);
        if (sources != null) {
            for (CampusIngestSource source : sources) {
                if (source != null && source.getSourceId() != null) {
                    return source;
                }
            }
        }
        CampusIngestSource source = new CampusIngestSource();
        source.setSourceName(spec.sourceName);
        source.setSourceType(spec.sourceType);
        source.setPlatform(spec.sourcePlatform);
        source.setAccessEndpoint(spec.accessEndpoint);
        source.setAuthorizationBasis(spec.authorizationBasis);
        source.setAuthorizationScope(spec.authorizationScope);
        source.setEnabled(1);
        source.setRemark("由监测任务自动创建，用于关键词驱动采集");
        return campusIngestService.saveSource(source, operatorUserId);
    }

    private CampusIngestTask ensureAutoIngestTask(CampusMonitorTask monitorTask,
                                                  AutoIngestPlatformSpec spec,
                                                  CampusIngestSource source,
                                                  Long operatorUserId) {
        String autoTaskName = "自动监测-" + monitorTask.getMonitorTaskId() + "-" + spec.label;
        CampusIngestTask task = findExactIngestTask(autoTaskName, source.getSourceId());
        if (task == null) {
            task = new CampusIngestTask();
        }
        task.setSourceId(source.getSourceId());
        task.setTaskName(autoTaskName);
        task.setTargetType(INGEST_TARGET_MONITOR_SCAN);
        task.setAdapterType(spec.adapterType);
        task.setScheduleEnabled(0);
        task.setFetchConfig(buildAutoFetchConfig(monitorTask, spec));
        task.setTaskStatus(INGEST_TASK_ACTIVE);
        task.setAutoDetectEnabled(0);
        task.setDailyQuotaLimit(0);
        task.setAutoPauseAfterFailCount(0);
        task.setRetentionDays(DEFAULT_RESULT_RETENTION_DAYS);
        task.setAuthorizationScope("自动接入：监测任务 " + monitorTask.getTaskName());
        task.setGovernanceRemark("由监测任务自动维护；关键词变更时同步更新 fetch_config");
        return campusIngestService.saveTask(task, operatorUserId);
    }

    private CampusIngestTask findExactIngestTask(String taskName, Long sourceId) {
        List<CampusIngestTask> tasks = campusIngestTaskDao.list(taskName, sourceId, null, null);
        if (tasks == null) {
            return null;
        }
        for (CampusIngestTask task : tasks) {
            if (task != null && StringUtils.equals(taskName, task.getTaskName())) {
                return task;
            }
        }
        return null;
    }

    private void runAutoIngestTask(AutoIngestPlatformSpec spec,
                                   CampusIngestTask ingestTask,
                                   Long operatorUserId,
                                   AutoIngestOutcome outcome) {
        try {
            CampusIngestRunLog runLog = campusIngestService.runTask(ingestTask.getTaskId(), operatorUserId);
            if (runLog != null) {
                outcome.markCollected(runLog.getEndTime() == null ? new Date() : runLog.getEndTime());
                if (!RUN_SUCCESS.equals(runLog.getRunStatus())) {
                    outcome.addError(spec.label + "采集失败：" + summary(runLog.getErrorMessage(), 200));
                }
            } else {
                outcome.markCollected(new Date());
            }
        } catch (RuntimeException ex) {
            outcome.addError(spec.label + "采集失败：" + summary(ex.getMessage(), 200));
        }
    }

    private String buildAutoFetchConfig(CampusMonitorTask task, AutoIngestPlatformSpec spec) {
        JSONObject config = new JSONObject();
        if ("baidu_search".equals(spec.adapterType)) {
            config.put("provider", "baidu");
            config.put("query", buildBaiduQuery(task));
            config.put("topK", 20);
            List<String> resourceTypes = new ArrayList<>();
            resourceTypes.add("web");
            config.put("resourceTypes", resourceTypes);
            config.put("credentialRef", "BAIDU_API_KEY");
            config.put("readerEnabled", true);
            config.put("readerProvider", "jina");
            config.put("maxReaderCalls", 5);
            config.put("fallbackToSnippet", true);
            return JSON.toJSONString(config);
        }
        config.put("provider", "tikhub");
        config.put("endpointKey", spec.endpointKey);
        config.put("platform", spec.sourcePlatform);
        config.put("query", buildSocialQuery(task));
        config.put("limit", 20);
        config.put("page", 1);
        config.put("credentialRef", "TIKHUB_API_KEY");
        config.put("timeoutMs", 30000);
        if ("weibo_search_all".equals(spec.endpointKey)) {
            config.put("searchType", "1");
            config.put("detailEnabled", true);
            config.put("maxDetailCalls", 10);
        }
        if ("xiaohongshu_search_notes".equals(spec.endpointKey)) {
            config.put("sortType", "general");
            config.put("contentType", "不限");
            config.put("detailEnabled", true);
            config.put("maxDetailCalls", 20);
        }
        if ("bilibili_search_by_type".equals(spec.endpointKey)) {
            config.put("searchType", "video");
            config.put("sortType", "0");
            config.put("detailEnabled", true);
            config.put("maxDetailCalls", 10);
        }
        if ("zhihu_article_search_v3".equals(spec.endpointKey)) {
            config.put("searchType", "Normal");
            config.put("contentType", "");
            config.put("sortType", "created_time");
            config.put("publishTime", "");
        }
        if ("wechat_mp_search_article".equals(spec.endpointKey)) {
            config.put("query", buildPrimarySocialQuery(task));
            config.put("sortType", "_0");
        }
        if ("kuaishou_search_comprehensive".equals(spec.endpointKey)) {
            config.put("searchType", "all");
            config.put("sortType", "newest");
            config.put("publishTime", "all");
            config.put("filterDuration", "all");
        }
        return JSON.toJSONString(config);
    }

    private String buildBaiduQuery(CampusMonitorTask task) {
        Set<String> terms = monitorSearchTerms(task);
        StringBuilder builder = new StringBuilder();
        for (String term : terms) {
            if (builder.length() > 0) {
                builder.append(" OR ");
            }
            builder.append(term);
            if (builder.length() >= 420) {
                break;
            }
        }
        Set<String> excludes = taskTokens(task.getExcludeWords(), task.getExcludeWordsI18n(), null);
        for (String exclude : excludes) {
            if (builder.length() + exclude.length() + 2 >= 500) {
                break;
            }
            builder.append(" -").append(exclude);
        }
        return summary(builder.length() == 0 ? task.getTaskName() : builder.toString(), 500);
    }

    private String buildSocialQuery(CampusMonitorTask task) {
        StringBuilder builder = new StringBuilder();
        for (String term : monitorSearchTerms(task)) {
            if (builder.length() > 0) {
                builder.append(' ');
            }
            if (builder.length() + term.length() + 1 > 110) {
                break;
            }
            builder.append(term);
        }
        return summary(builder.length() == 0 ? task.getTaskName() : builder.toString(), 120);
    }

    private String buildPrimarySocialQuery(CampusMonitorTask task) {
        for (String term : splitTokens(task.getMonitorSubject())) {
            return summary(term, 120);
        }
        for (String term : splitTokens(task.getSubjectAliases())) {
            return summary(term, 120);
        }
        return buildSocialQuery(task);
    }

    private Set<String> monitorSearchTerms(CampusMonitorTask task) {
        Set<String> terms = new LinkedHashSet<>();
        terms.addAll(splitTokens(task.getMonitorSubject()));
        terms.addAll(splitTokens(task.getSubjectAliases()));
        terms.addAll(splitTokens(task.getKeywords()));
        terms.addAll(languageTokens(task.getKeywordsI18n(), null));
        if (terms.isEmpty()) {
            terms.add(task.getTaskName());
        }
        return terms;
    }

    private AutoIngestSelection resolveAutoIngestSelection(String platformScope) {
        Set<String> requested = splitTokens(platformScope);
        if (requested.isEmpty() || requested.contains("*") || requested.contains("all")) {
            requested = new LinkedHashSet<>();
            requested.add("news");
            requested.add("douyin");
            requested.add("xiaohongshu");
            requested.add("bilibili");
            requested.add("weibo");
            requested.add("zhihu");
            requested.add("wechat");
            requested.add("kuaishou");
        }
        AutoIngestSelection selection = new AutoIngestSelection();
        Set<String> addedPlatforms = new LinkedHashSet<>();
        for (String rawPlatform : requested) {
            String platform = normalizePlatformCode(rawPlatform);
            AutoIngestPlatformSpec spec = platformSpec(platform);
            if (spec == null) {
                selection.unsupportedPlatforms.add(platformLabel(platform));
                continue;
            }
            if (addedPlatforms.add(spec.sourcePlatform + ":" + spec.adapterType)) {
                selection.specs.add(spec);
            }
        }
        return selection;
    }

    private String normalizePlatformCode(String platform) {
        String value = StringUtils.defaultString(platform).trim().toLowerCase();
        if ("web".equals(value) || "public_web".equals(value) || "baidu".equals(value)) {
            return "news";
        }
        if ("新闻媒体".equals(value) || "公开网页".equals(value) || "百度".equals(value)) {
            return "news";
        }
        if ("抖音".equals(value) || "抖音短视频".equals(value)) {
            return "douyin";
        }
        if ("小红书".equals(value)) {
            return "xiaohongshu";
        }
        if ("b站".equals(value)) {
            return "bilibili";
        }
        if ("微博".equals(value) || "新浪微博".equals(value)) {
            return "weibo";
        }
        if ("wechat_official".equals(value) || "wechat".equals(value)) {
            return "wechat";
        }
        if ("微信公众号".equals(value) || "微信".equals(value)) {
            return "wechat";
        }
        if ("知乎".equals(value)) {
            return "zhihu";
        }
        if ("快手".equals(value)) {
            return "kuaishou";
        }
        if ("论坛".equals(value) || "公开论坛".equals(value) || "forum".equals(value)
                || "tieba".equals(value) || "贴吧".equals(value) || "百度贴吧".equals(value)
                || "douban".equals(value) || "豆瓣".equals(value)) {
            return "news";
        }
        if ("xhs".equals(value) || "red".equals(value)) {
            return "xiaohongshu";
        }
        return value;
    }

    private AutoIngestPlatformSpec platformSpec(String platform) {
        if ("news".equals(platform)) {
            return new AutoIngestPlatformSpec("新闻/网页", "news", "public_search",
                    "百度新闻/公开网页自动接入", "https://qianfan.baidu.com/",
                    "百度搜索/公开网页授权接入", "公开网络信息关键词检索",
                    "baidu_search", null);
        }
        if ("douyin".equals(platform)) {
            return tikhubSpec("抖音", "douyin", "douyin_search_video_v2");
        }
        if ("xiaohongshu".equals(platform)) {
            return tikhubSpec("小红书", "xiaohongshu", "xiaohongshu_search_notes");
        }
        if ("bilibili".equals(platform)) {
            return tikhubSpec("B站", "bilibili", "bilibili_search_by_type");
        }
        if ("weibo".equals(platform)) {
            return tikhubSpec("微博", "weibo", "weibo_search_all");
        }
        if ("zhihu".equals(platform)) {
            return tikhubSpec("知乎", "zhihu", "zhihu_article_search_v3");
        }
        if ("wechat".equals(platform)) {
            return tikhubSpec("微信公众号", "wechat_official", "wechat_mp_search_article");
        }
        if ("kuaishou".equals(platform)) {
            return tikhubSpec("快手", "kuaishou", "kuaishou_search_comprehensive");
        }
        return null;
    }

    private AutoIngestPlatformSpec tikhubSpec(String label, String platform, String endpointKey) {
        return new AutoIngestPlatformSpec(label, platform, "third_party_api",
                "TikHub " + label + "自动接入", "https://api.tikhub.io/",
                "TikHub API 授权接入", "公开平台关键词检索",
                "third_party_api", endpointKey);
    }

    private String platformLabel(String platform) {
        if ("zhihu".equals(platform)) {
            return "知乎";
        }
        if ("wechat".equals(platform)) {
            return "微信公众号";
        }
        if ("kuaishou".equals(platform)) {
            return "快手";
        }
        if ("news".equals(platform)) {
            return "新闻/网页";
        }
        return StringUtils.defaultIfBlank(platform, "未知平台");
    }

    private boolean isAutoIngestEnabled(CampusMonitorTask task) {
        return task == null || task.getAutoIngestEnabled() == null || task.getAutoIngestEnabled() != 0;
    }

    private void syncIngestTaskBindings(Long monitorTaskId, String ingestTaskIds, Long operatorUserId) {
        if (monitorTaskId == null || ingestTaskIds == null) {
            return;
        }
        List<Long> parsedIds = parseIdList(ingestTaskIds);
        if (parsedIds.isEmpty()) {
            campusMonitorIngestTaskRelationDao.softDeleteAll(monitorTaskId, operatorUserId);
            return;
        }
        for (Long ingestTaskId : parsedIds) {
            if (campusIngestTaskDao.selectByTaskId(ingestTaskId) == null) {
                throw new IllegalArgumentException("绑定的接入任务不存在：" + ingestTaskId);
            }
        }
        campusMonitorIngestTaskRelationDao.softDeleteMissing(monitorTaskId, parsedIds, operatorUserId);
        for (Long ingestTaskId : parsedIds) {
            campusMonitorIngestTaskRelationDao.upsert(SnowflakeUtil.getId(), monitorTaskId, ingestTaskId, operatorUserId);
        }
    }

    private List<Long> parseIdList(String value) {
        List<Long> ids = new ArrayList<>();
        Set<String> rawIds = splitTokens(value);
        for (String rawId : rawIds) {
            try {
                Long id = Long.valueOf(rawId);
                if (id > 0 && !ids.contains(id)) {
                    ids.add(id);
                }
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("接入任务ID格式不正确：" + rawId);
            }
        }
        return ids;
    }

    private void validateWatchTarget(CampusMonitorWatchTarget target) {
        if (target == null) {
            throw new IllegalArgumentException("重点监控目标不能为空");
        }
        if (target.getMonitorTaskId() == null) {
            throw new IllegalArgumentException("监测任务ID不能为空");
        }
        if (!WATCH_TARGET_ACCOUNT.equals(target.getTargetType()) && !WATCH_TARGET_LINK.equals(target.getTargetType())) {
            throw new IllegalArgumentException("重点监控目标类型只能为 account 或 link");
        }
        if (WATCH_TARGET_ACCOUNT.equals(target.getTargetType())
                && target.getAccountId() == null
                && StringUtils.isBlank(target.getAccountName())
                && StringUtils.isBlank(target.getAccountUid())
                && StringUtils.isBlank(target.getLinkUrl())) {
            throw new IllegalArgumentException("账号监控目标至少需要账号ID、账号名称、平台账号ID或主页链接之一");
        }
        if (WATCH_TARGET_LINK.equals(target.getTargetType()) && StringUtils.isBlank(target.getLinkUrl())) {
            throw new IllegalArgumentException("链接监控目标必须填写链接");
        }
        if (StringUtils.isBlank(target.getAuthorizationScope())) {
            target.setAuthorizationScope("待人工确认授权范围");
        }
        if (StringUtils.isBlank(target.getTargetStatus())) {
            target.setTargetStatus(WATCH_STATUS_ACTIVE);
        }
    }

    private void rejectForbiddenText(String value) {
        String lower = StringUtils.defaultString(value).toLowerCase();
        String[] forbidden = new String[]{
                "apikey", "api_key", "token", "cookie", "session", "password",
                "deviceid", "device_id", "fingerprint", "signature", "xbogus", "abogus"
        };
        for (String item : forbidden) {
            if (lower.contains(item)) {
                throw new IllegalArgumentException("监测任务配置不能包含密钥、Cookie、设备指纹或签名参数");
            }
        }
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

    private Set<String> taskTokens(String raw, String i18nRaw, String language) {
        Set<String> tokens = splitTokens(raw);
        tokens.addAll(languageTokens(i18nRaw, language));
        return tokens;
    }

    private Set<String> languageTokens(String i18nRaw, String language) {
        Set<String> tokens = new LinkedHashSet<>();
        if (StringUtils.isBlank(i18nRaw)) {
            return tokens;
        }
        try {
            JSONObject jsonObject = JSON.parseObject(i18nRaw);
            if (StringUtils.isNotBlank(language)) {
                tokens.addAll(splitTokens(jsonObject.getString(language)));
            }
            tokens.addAll(splitTokens(jsonObject.getString("all")));
            if (tokens.isEmpty()) {
                for (Object value : jsonObject.values()) {
                    tokens.addAll(splitTokens(value == null ? null : String.valueOf(value)));
                }
            }
            return tokens;
        } catch (RuntimeException ignored) {
            return splitTokens(i18nRaw);
        }
    }

    private Set<String> dictTokens(String dictType, String language) {
        Set<String> tokens = new LinkedHashSet<>();
        List<CampusDictItem> items = campusDictDao.enabledItems(dictType);
        if (items == null || items.isEmpty()) {
            return tokens;
        }
        for (CampusDictItem item : items) {
            if (item == null) {
                continue;
            }
            String desc = StringUtils.defaultString(item.getDescription());
            if (StringUtils.isNotBlank(language) && StringUtils.isNotBlank(desc)
                    && !StringUtils.containsIgnoreCase(desc, language)
                    && !StringUtils.containsIgnoreCase(desc, "all")) {
                continue;
            }
            tokens.addAll(splitTokens(StringUtils.defaultIfBlank(item.getItemValue(), item.getItemName())));
        }
        return tokens;
    }

    private Set<String> matchTokens(String text, Set<String> tokens) {
        Set<String> matched = new LinkedHashSet<>();
        String safeText = StringUtils.defaultString(text);
        String lowerText = safeText.toLowerCase();
        for (String token : tokens) {
            if (StringUtils.isBlank(token)) {
                continue;
            }
            if (lowerText.contains(token.toLowerCase())) {
                matched.add(token);
            }
        }
        return matched;
    }

    private boolean containsAny(String text, Set<String> tokens) {
        return !matchTokens(text, tokens).isEmpty();
    }

    private String joinText(String... values) {
        StringBuilder builder = new StringBuilder();
        if (values == null) {
            return "";
        }
        for (String value : values) {
            if (StringUtils.isNotBlank(value)) {
                if (builder.length() > 0) {
                    builder.append('\n');
                }
                builder.append(value);
            }
        }
        return builder.toString();
    }

    private String joinTokens(String... values) {
        StringBuilder builder = new StringBuilder();
        if (values == null) {
            return "";
        }
        for (String value : values) {
            if (StringUtils.isNotBlank(value)) {
                if (builder.length() > 0) {
                    builder.append(',');
                }
                builder.append(value);
            }
        }
        return builder.toString();
    }

    private String defaultTitle(String title) {
        return StringUtils.defaultIfBlank(title, "未命名内容");
    }

    private String summary(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private int defaultPageNum(Integer pageNum) {
        return pageNum == null || pageNum < 1 ? 1 : pageNum;
    }

    private int defaultPageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return 10;
        }
        return Math.min(pageSize, 100);
    }

    private static class AutoIngestPlatformSpec {
        private final String label;
        private final String sourcePlatform;
        private final String sourceType;
        private final String sourceName;
        private final String accessEndpoint;
        private final String authorizationBasis;
        private final String authorizationScope;
        private final String adapterType;
        private final String endpointKey;

        private AutoIngestPlatformSpec(String label,
                                       String sourcePlatform,
                                       String sourceType,
                                       String sourceName,
                                       String accessEndpoint,
                                       String authorizationBasis,
                                       String authorizationScope,
                                       String adapterType,
                                       String endpointKey) {
            this.label = label;
            this.sourcePlatform = sourcePlatform;
            this.sourceType = sourceType;
            this.sourceName = sourceName;
            this.accessEndpoint = accessEndpoint;
            this.authorizationBasis = authorizationBasis;
            this.authorizationScope = authorizationScope;
            this.adapterType = adapterType;
            this.endpointKey = endpointKey;
        }
    }

    private static class AutoIngestSelection {
        private final List<AutoIngestPlatformSpec> specs = new ArrayList<>();
        private final List<String> unsupportedPlatforms = new ArrayList<>();
    }

    private static class AutoIngestOutcome {
        private String status = AUTO_INGEST_PENDING;
        private Date lastCollectTime;
        private final List<String> errors = new ArrayList<>();

        private static AutoIngestOutcome pending() {
            return new AutoIngestOutcome();
        }

        private static AutoIngestOutcome ready() {
            AutoIngestOutcome outcome = new AutoIngestOutcome();
            outcome.status = AUTO_INGEST_READY;
            return outcome;
        }

        private static AutoIngestOutcome unsupported(String message) {
            AutoIngestOutcome outcome = new AutoIngestOutcome();
            outcome.status = AUTO_INGEST_UNSUPPORTED;
            outcome.addError(message);
            return outcome;
        }

        private void setStatus(String status) {
            this.status = status;
        }

        private String getStatus() {
            return status;
        }

        private Date getLastCollectTime() {
            return lastCollectTime;
        }

        private void markCollected(Date collectTime) {
            if (collectTime == null) {
                return;
            }
            if (lastCollectTime == null || collectTime.after(lastCollectTime)) {
                lastCollectTime = collectTime;
            }
        }

        private void addUnsupported(List<String> platforms) {
            if (platforms == null || platforms.isEmpty()) {
                return;
            }
            errors.add("暂未接入：" + StringUtils.join(platforms, "、"));
        }

        private void addError(String error) {
            if (StringUtils.isNotBlank(error)) {
                errors.add(error);
            }
        }

        private boolean hasErrors() {
            return !errors.isEmpty();
        }

        private boolean success() {
            return AUTO_INGEST_READY.equals(status) && errors.isEmpty();
        }

        private String errorMessage() {
            return StringUtils.join(errors, "；");
        }
    }

    private static class AiResultAnalysis {
        private String analysisBasis;
        private String sentiment;
        private String summary;
        private String hitRecommendation;
        private String hitReason;
        private Integer confidence;
        private Integer schoolRelevanceScore;
        private String schoolRelevanceReason;
        private String matchedSchoolTerms;
        private String topicCategory;
        private String topicSubCategory;
        private String topicReason;
        private String riskLevel;
        private String riskReason;
        private String providerCode;
        private String modelCode;
    }

    private static class AiAnalysisText {
        private final String title;
        private final String content;
        private final String primaryText;
        private final String secondaryTitle;
        private final String analysisBasis;
        private final boolean contentUsable;
        private final String contentQualityReason;
        private final int titleSignalScore;
        private final int contentSignalScore;
        private final String selectionReason;

        private AiAnalysisText(String title,
                               String content,
                               String primaryText,
                               String secondaryTitle,
                               String analysisBasis,
                               boolean contentUsable,
                               String contentQualityReason,
                               int titleSignalScore,
                               int contentSignalScore,
                               String selectionReason) {
            this.title = title;
            this.content = content;
            this.primaryText = primaryText;
            this.secondaryTitle = secondaryTitle;
            this.analysisBasis = analysisBasis;
            this.contentUsable = contentUsable;
            this.contentQualityReason = contentQualityReason;
            this.titleSignalScore = titleSignalScore;
            this.contentSignalScore = contentSignalScore;
            this.selectionReason = selectionReason;
        }
    }

    private static class MonitorCounter {
        private int scannedCount;
        private int matchCount;
        private int negativeCount;
        private int alertCount;
    }
}
