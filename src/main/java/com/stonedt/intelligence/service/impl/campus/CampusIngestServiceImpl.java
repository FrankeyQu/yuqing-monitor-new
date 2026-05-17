package com.stonedt.intelligence.service.impl.campus;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.stonedt.intelligence.dao.campus.CampusIngestApiCallLogDao;
import com.stonedt.intelligence.dao.campus.CampusIngestRecordDao;
import com.stonedt.intelligence.dao.campus.CampusIngestRunLogDao;
import com.stonedt.intelligence.dao.campus.CampusIngestSourceDao;
import com.stonedt.intelligence.dao.campus.CampusIngestTaskDao;
import com.stonedt.intelligence.entity.campus.CampusAccountContent;
import com.stonedt.intelligence.entity.campus.CampusClue;
import com.stonedt.intelligence.entity.campus.CampusIngestApiCallLog;
import com.stonedt.intelligence.entity.campus.CampusIngestRecord;
import com.stonedt.intelligence.entity.campus.CampusIngestRunLog;
import com.stonedt.intelligence.entity.campus.CampusIngestSource;
import com.stonedt.intelligence.entity.campus.CampusIngestTask;
import com.stonedt.intelligence.service.campus.CampusAccountService;
import com.stonedt.intelligence.service.campus.CampusClueService;
import com.stonedt.intelligence.service.campus.CampusIngestService;
import com.stonedt.intelligence.service.campus.support.CampusRiskLevel;
import com.stonedt.intelligence.service.campus.ingest.CampusIngestAdapter;
import com.stonedt.intelligence.service.campus.ingest.CampusIngestAdapterRegistry;
import com.stonedt.intelligence.service.campus.ingest.CampusIngestFetchRequest;
import com.stonedt.intelligence.service.campus.ingest.CampusIngestFetchResponse;
import com.stonedt.intelligence.service.campus.ingest.CampusIngestItem;
import com.stonedt.intelligence.service.campus.ingest.governance.CampusIngestGovernanceService;
import com.stonedt.intelligence.service.campus.ingest.scheduler.CampusIngestRunContext;
import com.stonedt.intelligence.service.campus.ingest.scheduler.CampusIngestSchedulePolicy;
import com.stonedt.intelligence.service.campus.ingest.linkage.CampusIngestDetectionLinkageResult;
import com.stonedt.intelligence.service.campus.ingest.linkage.CampusIngestDetectionLinkageService;
import com.stonedt.intelligence.service.campus.ingest.normalize.CampusIngestDedupResult;
import com.stonedt.intelligence.service.campus.ingest.normalize.CampusIngestRecordNormalizer;
import com.stonedt.intelligence.service.campus.ingest.publicweb.PublicWebFetchConfig;
import com.stonedt.intelligence.service.campus.ingest.publicweb.PublicWebWhitelistValidator;
import com.stonedt.intelligence.dao.campus.CampusClueDao;
import com.stonedt.intelligence.service.campus.judgment.ClueJudgmentService;
import com.stonedt.intelligence.service.minority.util.MinorityLanguageUtil;
import com.stonedt.intelligence.util.SnowflakeUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class CampusIngestServiceImpl implements CampusIngestService {

    private static final String STATUS_PENDING = "pending";
    private static final String STATUS_CONVERTED = "converted";
    private static final String STATUS_FAILED = "failed";
    private static final String TARGET_CLUE = "clue";
    private static final String TASK_PAUSED = "paused";
    private static final String TASK_ACTIVE = "active";
    private static final String ADAPTER_MANUAL_PUSH = "manual_push";
    private static final String ADAPTER_THIRD_PARTY_API = "third_party_api";
    private static final String ADAPTER_PUBLIC_WEB_PULL = "public_web_pull";
    private static final String RISK_NORMAL = CampusRiskLevel.normalCode();
    private static final String RUN_RUNNING = "running";
    private static final String RUN_SUCCESS = "success";
    private static final String RUN_FAILED = "failed";
    private static final Long SYSTEM_USER_ID = 0L;
    private static final int DEFAULT_LOCK_MINUTES = 10;
    private static final int DEFAULT_RETRY_INTERVAL_MINUTES = 10;
    private static final String ERROR_CREDENTIAL_MISSING = "credential_missing";
    private static final String ERROR_ADAPTER_UNSUPPORTED = "adapter_unsupported";
    private static final String ERROR_VALIDATION = "validation_error";
    private static final String ERROR_REQUEST_FAILED = "request_failed";
    private static final String ERROR_NORMALIZE_FAILED = "normalize_failed";
    private static final String ERROR_QUOTA_EXCEEDED = "quota_exceeded";
    private static final String ERROR_UNKNOWN = "unknown";
    private static final int DEFAULT_RECORD_RETENTION_DAYS = 180;
    private static final int DEFAULT_RUN_LOG_RETENTION_DAYS = 90;
    private static final int DEFAULT_API_CALL_LOG_RETENTION_DAYS = 90;
    private static final int DEFAULT_CLEANUP_BATCH_SIZE = 1000;
    private static final int MAX_CLEANUP_BATCH_SIZE = 5000;
    private static final int MAX_CLEANUP_BATCHES = 20;
    private static final Pattern INLINE_SECRET_PATTERN = Pattern.compile(
            "(?i)(\"?(api[_-]?key|access[_-]?token|refresh[_-]?token|authorization|cookie|cookies|password|session|session[_-]?id|secret|token|device[_-]?id|fingerprint|msToken|ttwid|xBogus|x_bogus|aBogus|a_bogus|sign|signature)\"?\\s*[:=]\\s*\"?)([^\"\\s,;}]+)");

    private static final Logger log = LoggerFactory.getLogger(CampusIngestServiceImpl.class);

    private final CampusIngestSourceDao campusIngestSourceDao;
    private final CampusIngestTaskDao campusIngestTaskDao;
    private final CampusIngestApiCallLogDao campusIngestApiCallLogDao;
    private final CampusIngestRecordDao campusIngestRecordDao;
    private final CampusIngestRunLogDao campusIngestRunLogDao;
    private final CampusClueService campusClueService;
    private final CampusAccountService campusAccountService;
    private final CampusIngestAdapterRegistry campusIngestAdapterRegistry;
    private final CampusIngestSchedulePolicy campusIngestSchedulePolicy;
    private final CampusIngestRecordNormalizer campusIngestRecordNormalizer;
    private final CampusIngestDetectionLinkageService campusIngestDetectionLinkageService;
    private final CampusIngestGovernanceService campusIngestGovernanceService;

    @Autowired
    private ClueJudgmentService clueJudgmentService;

    @Autowired
    private CampusClueDao campusClueDao;

    public CampusIngestServiceImpl(CampusIngestSourceDao campusIngestSourceDao,
                                   CampusIngestTaskDao campusIngestTaskDao,
                                   CampusIngestApiCallLogDao campusIngestApiCallLogDao,
                                   CampusIngestRecordDao campusIngestRecordDao,
                                   CampusIngestRunLogDao campusIngestRunLogDao,
                                   CampusClueService campusClueService,
                                   CampusAccountService campusAccountService,
                                   CampusIngestAdapterRegistry campusIngestAdapterRegistry,
                                   CampusIngestSchedulePolicy campusIngestSchedulePolicy,
                                   CampusIngestRecordNormalizer campusIngestRecordNormalizer,
                                   CampusIngestDetectionLinkageService campusIngestDetectionLinkageService,
                                   CampusIngestGovernanceService campusIngestGovernanceService) {
        this.campusIngestSourceDao = campusIngestSourceDao;
        this.campusIngestTaskDao = campusIngestTaskDao;
        this.campusIngestApiCallLogDao = campusIngestApiCallLogDao;
        this.campusIngestRecordDao = campusIngestRecordDao;
        this.campusIngestRunLogDao = campusIngestRunLogDao;
        this.campusClueService = campusClueService;
        this.campusAccountService = campusAccountService;
        this.campusIngestAdapterRegistry = campusIngestAdapterRegistry;
        this.campusIngestSchedulePolicy = campusIngestSchedulePolicy;
        this.campusIngestRecordNormalizer = campusIngestRecordNormalizer;
        this.campusIngestDetectionLinkageService = campusIngestDetectionLinkageService;
        this.campusIngestGovernanceService = campusIngestGovernanceService;
    }

    @Override
    public CampusIngestSource saveSource(CampusIngestSource source, Long operatorUserId) {
        validateSource(source);
        if (source.getSourceId() == null) {
            source.setSourceId(SnowflakeUtil.getId());
            source.setEnabled(source.getEnabled() == null ? 1 : source.getEnabled());
            source.setDeleted(0);
            source.setCreateUserId(operatorUserId);
            source.setUpdateUserId(operatorUserId);
            campusIngestSourceDao.insert(source);
            return campusIngestSourceDao.selectBySourceId(source.getSourceId());
        }
        requireSource(source.getSourceId());
        source.setUpdateUserId(operatorUserId);
        campusIngestSourceDao.update(source);
        return campusIngestSourceDao.selectBySourceId(source.getSourceId());
    }

    @Override
    public void deleteSource(Long sourceId, Long operatorUserId) {
        requireSource(sourceId);
        campusIngestSourceDao.logicalDelete(sourceId, operatorUserId);
    }

    @Override
    public PageInfo<CampusIngestSource> listSources(Integer pageNum,
                                                    Integer pageSize,
                                                    String keyword,
                                                    String sourceType,
                                                    String platform,
                                                    Integer enabled) {
        PageHelper.startPage(defaultPageNum(pageNum), defaultPageSize(pageSize));
        return new PageInfo<>(campusIngestSourceDao.list(keyword, sourceType, platform, enabled));
    }

    @Override
    public CampusIngestTask saveTask(CampusIngestTask task, Long operatorUserId) {
        validateTask(task);
        requireSource(task.getSourceId());
        applyScheduleDefaults(task);
        if (task.getTaskId() == null) {
            task.setTaskId(SnowflakeUtil.getId());
            task.setTaskStatus(StringUtils.defaultIfBlank(task.getTaskStatus(), TASK_PAUSED));
            task.setAdapterType(StringUtils.defaultIfBlank(task.getAdapterType(), ADAPTER_MANUAL_PUSH));
            task.setRetentionDays(task.getRetentionDays() == null ? 180 : task.getRetentionDays());
            task.setDeleted(0);
            task.setCreateUserId(operatorUserId);
            task.setUpdateUserId(operatorUserId);
            campusIngestTaskDao.insert(task);
            return campusIngestTaskDao.selectByTaskId(task.getTaskId());
        }
        requireTask(task.getTaskId());
        task.setUpdateUserId(operatorUserId);
        campusIngestTaskDao.update(task);
        return campusIngestTaskDao.selectByTaskId(task.getTaskId());
    }

    @Override
    public CampusIngestTask updateTaskStatus(Long taskId, String taskStatus, Long operatorUserId) {
        requireTask(taskId);
        if (StringUtils.isBlank(taskStatus)) {
            throw new IllegalArgumentException("任务状态不能为空");
        }
        campusIngestTaskDao.updateStatus(taskId, taskStatus, operatorUserId);
        return campusIngestTaskDao.selectByTaskId(taskId);
    }

    @Override
    public void deleteTask(Long taskId, Long operatorUserId) {
        requireTask(taskId);
        campusIngestTaskDao.logicalDelete(taskId, operatorUserId);
    }

    @Override
    public PageInfo<CampusIngestTask> listTasks(Integer pageNum,
                                                Integer pageSize,
                                                String keyword,
                                                Long sourceId,
                                                String targetType,
                                                String taskStatus) {
        PageHelper.startPage(defaultPageNum(pageNum), defaultPageSize(pageSize));
        return new PageInfo<>(campusIngestTaskDao.list(keyword, sourceId, targetType, taskStatus));
    }

    @Override
    public CampusIngestRecord submitRecord(CampusIngestRecord record, Long operatorUserId) {
        validateRecord(record);
        requireSource(record.getSourceId());
        if (record.getTaskId() != null) {
            requireTask(record.getTaskId());
        }
        record.setRecordId(SnowflakeUtil.getId());
        record.setNormalizedStatus(STATUS_PENDING);
        record.setRiskLevel(CampusRiskLevel.normalizeOrDefault(record.getRiskLevel()));
        record.setDeleted(0);
        record.setCreateUserId(operatorUserId);
        record.setUpdateUserId(operatorUserId);
        campusIngestRecordDao.insert(record);
        return campusIngestRecordDao.selectByRecordId(record.getRecordId());
    }

    @Override
    public PageInfo<CampusIngestRecord> listRecords(Integer pageNum,
                                                    Integer pageSize,
                                                    String keyword,
                                                    Long sourceId,
                                                    Long taskId,
                                                    String normalizedStatus,
                                                    String targetType,
                                                    Date startTime,
                                                    Date endTime) {
        PageHelper.startPage(defaultPageNum(pageNum), defaultPageSize(pageSize));
        return new PageInfo<>(campusIngestRecordDao.list(keyword, sourceId, taskId,
                normalizedStatus, targetType, startTime, endTime));
    }

    @Override
    public CampusClue convertRecordToClue(Long recordId, Long operatorUserId, String operatorName) {
        CampusIngestRecord record = requireRecord(recordId);
        try {
            CampusClue clue = new CampusClue();
            clue.setClueTitle(StringUtils.defaultIfBlank(record.getTitle(), "未命名接入线索"));
            clue.setClueContent(record.getContent());
            clue.setClueSource("ingest");
            clue.setSourcePlatform(record.getPlatform());
            clue.setOriginalUrl(record.getOriginalUrl());
            clue.setPublishTime(record.getPublishTime());
            clue.setDiscoverTime(new Date());
            clue.setInvolvedAccount(record.getAuthorName());
            clue.setKeywords(record.getKeywords());
            clue.setRiskLevel(record.getRiskLevel());
            clue.setSentiment(record.getSentiment());
            clue.setLanguage(record.getLanguage());
            CampusClue saved = campusClueService.save(clue, operatorUserId, operatorName);
            campusIngestRecordDao.updateStatus(recordId, STATUS_CONVERTED, "clue",
                    saved.getClueId(), null, operatorUserId);
            return saved;
        } catch (Exception e) {
            campusIngestRecordDao.updateStatus(recordId, STATUS_FAILED, "clue", null, e.getMessage(), operatorUserId);
            throw asRuntimeException(e);
        }
    }

    @Override
    public CampusAccountContent convertRecordToAccountContent(Long recordId,
                                                              Long accountId,
                                                              Long operatorUserId) {
        CampusIngestRecord record = requireRecord(recordId);
        Long resolvedAccountId = accountId == null ? record.getAccountId() : accountId;
        if (resolvedAccountId == null) {
            throw new IllegalArgumentException("转换为账号动态时账号ID不能为空");
        }
        try {
            CampusAccountContent content = new CampusAccountContent();
            content.setAccountId(resolvedAccountId);
            content.setTaskId(record.getAccountTaskId());
            content.setPlatform(record.getPlatform());
            content.setContentType(record.getContentType());
            content.setContentTitle(record.getTitle());
            content.setContentText(record.getContent());
            content.setOriginalUrl(record.getOriginalUrl());
            content.setPublishTime(record.getPublishTime());
            content.setCaptureTime(new Date());
            content.setRiskLevel(record.getRiskLevel());
            content.setSentiment(record.getSentiment());
            content.setKeywords(record.getKeywords());
            content.setLikeCount(record.getLikeCount());
            content.setCommentCount(record.getCommentCount());
            content.setShareCount(record.getShareCount());
            content.setCollectCount(record.getCollectCount());
            content.setViewCount(record.getViewCount());
            content.setRawData(record.getRawData());
            CampusAccountContent saved = campusAccountService.addContent(content, operatorUserId);
            campusIngestRecordDao.updateStatus(recordId, STATUS_CONVERTED, "account_content",
                    saved.getContentId(), null, operatorUserId);
            return saved;
        } catch (Exception e) {
            campusIngestRecordDao.updateStatus(recordId, STATUS_FAILED, "account_content", null, e.getMessage(), operatorUserId);
            throw asRuntimeException(e);
        }
    }

    @Override
    public CampusIngestRunLog startRun(Long taskId, Long operatorUserId) {
        requireTask(taskId);
        CampusIngestRunLog runLog = createRunLog(taskId, operatorUserId, CampusIngestRunContext.manual());
        campusIngestTaskDao.updateLastRunTime(taskId, operatorUserId);
        return campusIngestRunLogDao.selectByRunId(runLog.getRunId());
    }

    @Override
    public CampusIngestRunLog finishRun(Long runId,
                                        String runStatus,
                                        Integer fetchedCount,
                                        Integer successCount,
                                        Integer failCount,
                                        String errorMessage,
                                        Long operatorUserId) {
        CampusIngestRunLog runLog = requireRun(runId);
        String resolvedStatus = StringUtils.defaultIfBlank(runStatus, RUN_SUCCESS);
        String errorType = RUN_FAILED.equals(resolvedStatus) ? classifyErrorType(errorMessage, 0) : null;
        campusIngestRunLogDao.finish(runId, StringUtils.defaultIfBlank(runStatus, "success"),
                defaultCount(fetchedCount), defaultCount(successCount), 0, 0,
                defaultCount(failCount), errorMessage, durationSince(runLog.getStartTime()), errorType);
        campusIngestTaskDao.updateLastRunTime(runLog.getTaskId(), operatorUserId);
        return campusIngestRunLogDao.selectByRunId(runId);
    }

    @Override
    public CampusIngestRunLog runTask(Long taskId, Long operatorUserId) {
        CampusIngestTask task = requireTask(taskId);
        CampusIngestSource source = requireSource(task.getSourceId());
        validateRunnable(task, source);
        Date now = new Date();
        Date lockUntil = new Date(now.getTime() + DEFAULT_LOCK_MINUTES * 60L * 1000L);
        if (campusIngestTaskDao.acquireExecutionLock(taskId, now, lockUntil, operatorUserId) != 1) {
            throw new IllegalStateException("接入任务正在运行，请稍后再试");
        }
        try {
            return runTaskInternal(taskId, operatorUserId, CampusIngestRunContext.manual(), lockUntil);
        } catch (Exception ex) {
            campusIngestTaskDao.releaseScheduleLockBefore(taskId, lockUntil);
            throw asRuntimeException(ex);
        }
    }

    @Override
    public CampusIngestRunLog runScheduledTask(Long taskId, String triggerType, Integer retryCount, String schedulerNode) {
        return runScheduledTask(taskId, triggerType, retryCount, schedulerNode, null);
    }

    @Override
    public CampusIngestRunLog runScheduledTask(Long taskId,
                                               String triggerType,
                                               Integer retryCount,
                                               String schedulerNode,
                                               Date lockUntil) {
        return runTaskInternal(taskId, SYSTEM_USER_ID,
                CampusIngestRunContext.scheduled(triggerType, retryCount, schedulerNode), lockUntil);
    }

    @Override
    public List<CampusIngestTask> listDueTasks(Date now, Integer limit) {
        return campusIngestTaskDao.listDueTasks(now == null ? new Date() : now,
                limit == null || limit < 1 ? 5 : Math.min(limit, 20));
    }

    @Override
    public boolean acquireScheduleLock(Long taskId, Date now, Date lockUntil) {
        return campusIngestTaskDao.acquireScheduleLock(taskId,
                now == null ? new Date() : now,
                lockUntil == null ? new Date(System.currentTimeMillis() + DEFAULT_LOCK_MINUTES * 60L * 1000L) : lockUntil) == 1;
    }

    @Override
    public void releaseScheduleLock(Long taskId) {
        if (taskId != null) {
            campusIngestTaskDao.releaseScheduleLock(taskId);
        }
    }

    @Override
    public void releaseScheduleLock(Long taskId, Date lockUntil) {
        if (taskId != null) {
            campusIngestTaskDao.releaseScheduleLockBefore(taskId, lockUntil);
        }
    }

    @Override
    public Map<String, Integer> cleanupExpiredData(Integer recordRetentionDays,
                                                   Integer runLogRetentionDays,
                                                   Integer apiCallLogRetentionDays,
                                                   Integer batchSize) {
        Map<String, Integer> cleanup = new HashMap<>();
        int safeBatchSize = safeCleanupBatchSize(batchSize);
        cleanup.put("expiredRecordCount", cleanupExpiredRecords(recordRetentionDays, safeBatchSize));
        cleanup.put("expiredRunLogCount", cleanupExpiredRunLogs(runLogRetentionDays, safeBatchSize));
        cleanup.put("expiredApiCallLogCount", cleanupExpiredApiCallLogs(apiCallLogRetentionDays, safeBatchSize));
        return cleanup;
    }

    private CampusIngestRunLog runTaskInternal(Long taskId,
                                               Long operatorUserId,
                                               CampusIngestRunContext runContext,
                                               Date lockUntil) {
        CampusIngestTask task = requireTask(taskId);
        CampusIngestRunLog runLog = createRunLog(taskId, operatorUserId, runContext);
        long startMillis = System.currentTimeMillis();
        int fetchedCount = 0;
        int successCount = 0;
        int duplicateCount = 0;
        int invalidCount = 0;
        int failCount = 0;
        String errorMessage = null;

        try {
            CampusIngestSource source = requireSource(task.getSourceId());
            validateRunnable(task, source);
            campusIngestGovernanceService.ensureQuotaAvailable(task, operatorUserId);
            CampusIngestAdapter adapter = campusIngestAdapterRegistry.getAdapter(task.getAdapterType());
            CampusIngestFetchResponse response = adapter.fetch(buildFetchRequest(runLog.getRunId(), task, source, operatorUserId));
            if (response == null) {
                response = CampusIngestFetchResponse.empty("adapter returned empty response");
            }
            if (!response.isSupported()) {
                throw new IllegalStateException(StringUtils.defaultIfBlank(response.getMessage(), "接入适配器暂不支持"));
            }
            List<CampusIngestItem> items = response.getRecords() == null ? Collections.<CampusIngestItem>emptyList() : response.getRecords();
            fetchedCount = items.size();
            if (fetchedCount == 0) {
                errorMessage = appendError(errorMessage, StringUtils.defaultIfBlank(response.getMessage(),
                        "接口请求成功，但未返回可入库内容"));
            }
            List<Long> insertedRecordIds = new ArrayList<>();
            for (CampusIngestItem item : items) {
                try {
                    CampusIngestDedupResult result = insertAdapterRecord(runLog.getRunId(), task, source, item, operatorUserId);
                    if (result.isInserted()) {
                        successCount++;
                        insertedRecordIds.add(result.getRecordId());
                    } else if (result.isDuplicate()) {
                        duplicateCount++;
                    } else if (result.isInvalid()) {
                        invalidCount++;
                    }
                } catch (Exception e) {
                    failCount++;
                    errorMessage = appendError(errorMessage, e.getMessage());
                }
            }
            if (failCount > 0) {
                throw new IllegalStateException(StringUtils.defaultIfBlank(errorMessage, "接入任务运行失败"));
            }
            campusIngestRunLogDao.finish(runLog.getRunId(), RUN_SUCCESS, fetchedCount, successCount,
                    duplicateCount, invalidCount, failCount, errorMessage, elapsedMillis(startMillis), null);
            CampusIngestRunLog finishedRunLog = campusIngestRunLogDao.selectByRunId(runLog.getRunId());
            if (successCount > 0 && !insertedRecordIds.isEmpty() && shouldAutoConvertToClue(task)) {
                int clueCount = autoConvertRecordsToClues(task, insertedRecordIds, operatorUserId);
                log.info("Auto-converted {} ingest records to clues for task {}", clueCount, task.getTaskId());
            }
            linkDetectionAfterSuccess(task, finishedRunLog, successCount, operatorUserId);
            completeTaskAfterRun(task, runContext, true, null, operatorUserId, lockUntil);
            return campusIngestRunLogDao.selectByRunId(runLog.getRunId());
        } catch (Exception e) {
            errorMessage = appendError(errorMessage, e.getMessage());
            String errorType = classifyErrorType(errorMessage, failCount);
            campusIngestRunLogDao.finish(runLog.getRunId(), RUN_FAILED, fetchedCount, successCount,
                    duplicateCount, invalidCount, failCount, errorMessage, elapsedMillis(startMillis), errorType);
            completeTaskAfterRun(task, runContext, false, errorType, operatorUserId, lockUntil);
            throw asRuntimeException(e);
        }
    }

    private void completeTaskAfterRun(CampusIngestTask task,
                                      CampusIngestRunContext runContext,
                                      boolean success,
                                      String errorType,
                                      Long operatorUserId,
                                      Date lockUntil) {
        if (runContext == null || runContext.isManual()) {
            campusIngestTaskDao.updateLastRunTime(task.getTaskId(), operatorUserId);
            refreshManualNextRunTimeIfDue(task, operatorUserId);
            campusIngestTaskDao.releaseScheduleLockBefore(task.getTaskId(), lockUntil);
            return;
        }
        if (success) {
            Date nextRunTime = nextCronRunTime(task);
            campusIngestTaskDao.markScheduleSuccess(task.getTaskId(), nextRunTime, lockUntil, SYSTEM_USER_ID);
            return;
        }
        int currentRetry = task.getCurrentRetryCount() == null ? 0 : task.getCurrentRetryCount();
        int maxRetry = task.getMaxRetryCount() == null ? 0 : Math.max(task.getMaxRetryCount(), 0);
        int nextRetryCount;
        Date nextRunTime;
        if (currentRetry < maxRetry) {
            nextRetryCount = currentRetry + 1;
            nextRunTime = campusIngestSchedulePolicy.nextRetryTime(new Date(), task.getRetryIntervalMinutes());
        } else {
            nextRetryCount = 0;
            nextRunTime = nextCronRunTime(task);
        }
        String nextTaskStatus = shouldAutoPauseAfterFailure(task) ? TASK_PAUSED : null;
        campusIngestTaskDao.markScheduleFailure(task.getTaskId(), nextRunTime,
                StringUtils.defaultIfBlank(errorType, ERROR_UNKNOWN), nextRetryCount,
                nextTaskStatus, lockUntil, SYSTEM_USER_ID);
    }

    private boolean shouldAutoPauseAfterFailure(CampusIngestTask task) {
        if (task == null || task.getAutoPauseAfterFailCount() == null || task.getAutoPauseAfterFailCount() <= 0) {
            return false;
        }
        int consecutiveFailCount = task.getConsecutiveFailCount() == null ? 0 : Math.max(task.getConsecutiveFailCount(), 0);
        return consecutiveFailCount + 1 >= task.getAutoPauseAfterFailCount();
    }

    private boolean shouldAutoConvertToClue(CampusIngestTask task) {
        return task != null && TARGET_CLUE.equals(task.getTargetType());
    }

    private void refreshManualNextRunTimeIfDue(CampusIngestTask task, Long operatorUserId) {
        if (!isScheduleEnabled(task) || StringUtils.isBlank(task.getScheduleCron())) {
            return;
        }
        Date now = new Date();
        if (task.getNextRunTime() == null || !task.getNextRunTime().after(now)) {
            campusIngestTaskDao.updateNextRunTime(task.getTaskId(), nextCronRunTime(task), operatorUserId);
        }
    }

    private Date nextCronRunTime(CampusIngestTask task) {
        try {
            return campusIngestSchedulePolicy.nextRunTime(task.getScheduleCron(), new Date());
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private boolean isScheduleEnabled(CampusIngestTask task) {
        return task != null && task.getScheduleEnabled() != null && task.getScheduleEnabled() == 1;
    }

    @Override
    public List<CampusIngestRunLog> listRunLogs(Long taskId) {
        requireTask(taskId);
        return campusIngestRunLogDao.listByTaskId(taskId);
    }

    @Override
    public PageInfo<CampusIngestRunLog> listRunLogPage(Integer pageNum,
                                                       Integer pageSize,
                                                       Long taskId,
                                                       String runStatus,
                                                       String errorType,
                                                       String triggerType) {
        PageHelper.startPage(defaultPageNum(pageNum), defaultPageSize(pageSize));
        return new PageInfo<>(campusIngestRunLogDao.list(taskId, runStatus, errorType, triggerType));
    }

    @Override
    public List<CampusIngestApiCallLog> listApiCallLogs(Long taskId,
                                                        Long runId,
                                                        String provider,
                                                        String callStatus) {
        if (taskId != null) {
            requireTask(taskId);
        }
        return campusIngestApiCallLogDao.list(taskId, runId, provider, callStatus);
    }

    private int cleanupExpiredRecords(Integer retentionDays, int batchSize) {
        int total = 0;
        for (int i = 0; i < MAX_CLEANUP_BATCHES; i++) {
            int affected = campusIngestRecordDao.logicalDeleteByTaskRetention(batchSize, SYSTEM_USER_ID);
            if (affected <= 0) {
                break;
            }
            total += affected;
            if (affected < batchSize) {
                break;
            }
        }
        Date expireBefore = expireBefore(retentionDays, DEFAULT_RECORD_RETENTION_DAYS);
        if (expireBefore == null) {
            return total;
        }
        for (int i = 0; i < MAX_CLEANUP_BATCHES; i++) {
            int affected = campusIngestRecordDao.logicalDeleteBefore(expireBefore, batchSize, SYSTEM_USER_ID);
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
            int affected = campusIngestRunLogDao.deleteBefore(expireBefore, batchSize);
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

    private int cleanupExpiredApiCallLogs(Integer retentionDays, int batchSize) {
        Date expireBefore = expireBefore(retentionDays, DEFAULT_API_CALL_LOG_RETENTION_DAYS);
        if (expireBefore == null) {
            return 0;
        }
        int total = 0;
        for (int i = 0; i < MAX_CLEANUP_BATCHES; i++) {
            int affected = campusIngestApiCallLogDao.deleteBefore(expireBefore, batchSize);
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

    private void validateSource(CampusIngestSource source) {
        if (source == null) {
            throw new IllegalArgumentException("接入来源不能为空");
        }
        if (StringUtils.isBlank(source.getSourceName())) {
            throw new IllegalArgumentException("来源名称不能为空");
        }
        if (StringUtils.isBlank(source.getSourceType())) {
            throw new IllegalArgumentException("来源类型不能为空");
        }
        if (StringUtils.isBlank(source.getAuthorizationBasis())) {
            throw new IllegalArgumentException("授权或来源依据不能为空");
        }
        if (StringUtils.isBlank(source.getAuthorizationScope())) {
            throw new IllegalArgumentException("授权范围不能为空");
        }
    }

    private void validateTask(CampusIngestTask task) {
        if (task == null) {
            throw new IllegalArgumentException("接入任务不能为空");
        }
        if (task.getSourceId() == null) {
            throw new IllegalArgumentException("来源ID不能为空");
        }
        if (StringUtils.isBlank(task.getTaskName())) {
            throw new IllegalArgumentException("任务名称不能为空");
        }
        if (StringUtils.isBlank(task.getTargetType())) {
            throw new IllegalArgumentException("目标类型不能为空");
        }
        if (StringUtils.isBlank(task.getAuthorizationScope())) {
            throw new IllegalArgumentException("任务授权范围不能为空");
        }
        rejectInlineSecrets(task.getFetchConfig());
        validateAdapterFetchConfig(task);
    }

    private void rejectInlineSecrets(String fetchConfig) {
        if (StringUtils.isBlank(fetchConfig)) {
            return;
        }
        if (INLINE_SECRET_PATTERN.matcher(fetchConfig).find()) {
            throw new IllegalArgumentException("接入配置不能包含密钥、Cookie、Token、设备指纹或签名参数，请使用 credentialRef 引用");
        }
    }

    private void validateAdapterFetchConfig(CampusIngestTask task) {
        if (task == null || !ADAPTER_PUBLIC_WEB_PULL.equals(task.getAdapterType())) {
            return;
        }
        PublicWebFetchConfig config = PublicWebFetchConfig.fromJson(task.getFetchConfig());
        PublicWebWhitelistValidator.validateHttpUrl(config.getUrl());
    }

    private void applyScheduleDefaults(CampusIngestTask task) {
        task.setScheduleEnabled(task.getScheduleEnabled() == null ? 0 : task.getScheduleEnabled());
        task.setMaxRetryCount(task.getMaxRetryCount() == null ? 0 : Math.max(task.getMaxRetryCount(), 0));
        task.setRetryIntervalMinutes(task.getRetryIntervalMinutes() == null || task.getRetryIntervalMinutes() <= 0
                ? DEFAULT_RETRY_INTERVAL_MINUTES
                : task.getRetryIntervalMinutes());
        if (task.getConsecutiveFailCount() == null) {
            task.setConsecutiveFailCount(0);
        }
        if (task.getCurrentRetryCount() == null) {
            task.setCurrentRetryCount(0);
        }
        task.setAutoDetectEnabled(task.getAutoDetectEnabled() == null ? 0 : task.getAutoDetectEnabled());
        applyGovernanceDefaults(task);
        if (isScheduleEnabled(task)) {
            campusIngestSchedulePolicy.validateCronForSchedule(task.getScheduleCron());
            if (task.getNextRunTime() == null) {
                task.setNextRunTime(campusIngestSchedulePolicy.nextRunTime(task.getScheduleCron(), new Date()));
            }
        }
    }

    private void applyGovernanceDefaults(CampusIngestTask task) {
        task.setDailyQuotaLimit(task.getDailyQuotaLimit() == null ? 0 : Math.max(task.getDailyQuotaLimit(), 0));
        task.setDailyQuotaUsed(task.getDailyQuotaUsed() == null ? 0 : Math.max(task.getDailyQuotaUsed(), 0));
        task.setAutoPauseAfterFailCount(task.getAutoPauseAfterFailCount() == null
                ? 0
                : Math.max(task.getAutoPauseAfterFailCount(), 0));
        if (task.getRetentionDays() == null || task.getRetentionDays() <= 0) {
            task.setRetentionDays(DEFAULT_RECORD_RETENTION_DAYS);
        } else {
            task.setRetentionDays(Math.min(task.getRetentionDays(), 3650));
        }
    }

    private void validateRecord(CampusIngestRecord record) {
        if (record == null) {
            throw new IllegalArgumentException("接入记录不能为空");
        }
        if (record.getSourceId() == null) {
            throw new IllegalArgumentException("来源ID不能为空");
        }
        if (StringUtils.isBlank(record.getTitle())
                && StringUtils.isBlank(record.getContent())
                && StringUtils.isBlank(record.getOriginalUrl())
                && StringUtils.isBlank(record.getExternalId())) {
            throw new IllegalArgumentException("标题、内容、原始链接和外部ID不能同时为空");
        }
    }

    private CampusIngestSource requireSource(Long sourceId) {
        if (sourceId == null) {
            throw new IllegalArgumentException("来源ID不能为空");
        }
        CampusIngestSource source = campusIngestSourceDao.selectBySourceId(sourceId);
        if (source == null) {
            throw new IllegalArgumentException("接入来源不存在");
        }
        return source;
    }

    private CampusIngestTask requireTask(Long taskId) {
        if (taskId == null) {
            throw new IllegalArgumentException("任务ID不能为空");
        }
        CampusIngestTask task = campusIngestTaskDao.selectByTaskId(taskId);
        if (task == null) {
            throw new IllegalArgumentException("接入任务不存在");
        }
        return task;
    }

    private CampusIngestRecord requireRecord(Long recordId) {
        if (recordId == null) {
            throw new IllegalArgumentException("接入记录ID不能为空");
        }
        CampusIngestRecord record = campusIngestRecordDao.selectByRecordId(recordId);
        if (record == null) {
            throw new IllegalArgumentException("接入记录不存在");
        }
        return record;
    }

    private CampusIngestRunLog requireRun(Long runId) {
        if (runId == null) {
            throw new IllegalArgumentException("运行日志ID不能为空");
        }
        CampusIngestRunLog runLog = campusIngestRunLogDao.selectByRunId(runId);
        if (runLog == null) {
            throw new IllegalArgumentException("运行日志不存在");
        }
        return runLog;
    }

    private CampusIngestRunLog createRunLog(Long taskId, Long operatorUserId, CampusIngestRunContext runContext) {
        CampusIngestRunLog runLog = new CampusIngestRunLog();
        runLog.setRunId(SnowflakeUtil.getId());
        runLog.setTaskId(taskId);
        runLog.setRunStatus(RUN_RUNNING);
        runLog.setTriggerType(runContext == null ? CampusIngestRunContext.TRIGGER_MANUAL : runContext.getTriggerType());
        runLog.setStartTime(new Date());
        runLog.setFetchedCount(0);
        runLog.setSuccessCount(0);
        runLog.setDuplicateCount(0);
        runLog.setInvalidCount(0);
        runLog.setDetectionTriggerCount(0);
        runLog.setDetectionHitCount(0);
        runLog.setDetectionAlertCount(0);
        runLog.setFailCount(0);
        runLog.setRetryCount(runContext == null ? 0 : runContext.getRetryCount());
        runLog.setSchedulerNode(runContext == null ? null : runContext.getSchedulerNode());
        runLog.setCreateUserId(operatorUserId);
        campusIngestRunLogDao.insert(runLog);
        return runLog;
    }

    private void validateRunnable(CampusIngestTask task, CampusIngestSource source) {
        if (!TASK_ACTIVE.equals(task.getTaskStatus())) {
            throw new IllegalArgumentException("仅启用状态的接入任务允许手动运行");
        }
        if (source.getEnabled() == null || source.getEnabled() != 1) {
            throw new IllegalArgumentException("接入来源未启用");
        }
        if (StringUtils.isBlank(source.getAuthorizationBasis())) {
            throw new IllegalArgumentException("接入来源授权依据不能为空");
        }
        if (StringUtils.isBlank(source.getAuthorizationScope()) || StringUtils.isBlank(task.getAuthorizationScope())) {
            throw new IllegalArgumentException("接入授权范围不能为空");
        }
    }

    private CampusIngestFetchRequest buildFetchRequest(Long runId,
                                                       CampusIngestTask task,
                                                       CampusIngestSource source,
                                                       Long operatorUserId) {
        CampusIngestFetchRequest request = new CampusIngestFetchRequest();
        request.setRunId(runId);
        request.setTask(task);
        request.setSource(source);
        request.setOperatorUserId(operatorUserId);
        return request;
    }

    private CampusIngestDedupResult insertAdapterRecord(Long runId,
                                                        CampusIngestTask task,
                                                        CampusIngestSource source,
                                                        CampusIngestItem item,
                                                        Long operatorUserId) {
        if (item == null) {
            return CampusIngestDedupResult.invalid("接入记录为空");
        }
        CampusIngestRecord record = campusIngestRecordNormalizer.normalize(runId, task, source, item, operatorUserId);
        if (campusIngestRecordNormalizer.isInvalid(record)) {
            return CampusIngestDedupResult.invalid("标准化后无有效内容");
        }
        validateRecord(record);
        CampusIngestRecord externalDuplicate = findDuplicateByExternalId(record);
        if (externalDuplicate != null) {
            enhanceDuplicateRecord(externalDuplicate, record, operatorUserId);
            return CampusIngestDedupResult.duplicateExternalId(externalDuplicate.getRecordId());
        }
        CampusIngestRecord hashDuplicate = findDuplicateByContentHash(record);
        if (hashDuplicate != null) {
            enhanceDuplicateRecord(hashDuplicate, record, operatorUserId);
            return CampusIngestDedupResult.duplicateContentHash(hashDuplicate.getRecordId());
        }
        CampusIngestRecord nearDuplicate = findNearDuplicateByPlatformTitle(record);
        if (nearDuplicate != null) {
            enhanceDuplicateRecord(nearDuplicate, record, operatorUserId);
            return CampusIngestDedupResult.duplicateContentHash(nearDuplicate.getRecordId());
        }
        try {
            campusIngestRecordDao.insert(record);
            return CampusIngestDedupResult.inserted(record.getRecordId());
        } catch (DuplicateKeyException e) {
            CampusIngestRecord duplicate = findDuplicateByExternalId(record);
            if (duplicate != null) {
                enhanceDuplicateRecord(duplicate, record, operatorUserId);
                return CampusIngestDedupResult.duplicateExternalId(duplicate.getRecordId());
            }
            duplicate = findDuplicateByContentHash(record);
            if (duplicate != null) {
                enhanceDuplicateRecord(duplicate, record, operatorUserId);
                return CampusIngestDedupResult.duplicateContentHash(duplicate.getRecordId());
            }
            duplicate = findNearDuplicateByPlatformTitle(record);
            if (duplicate != null) {
                enhanceDuplicateRecord(duplicate, record, operatorUserId);
                return CampusIngestDedupResult.duplicateContentHash(duplicate.getRecordId());
            }
            throw e;
        }
    }

    private void enhanceDuplicateRecord(CampusIngestRecord existing,
                                        CampusIngestRecord incoming,
                                        Long operatorUserId) {
        if (existing == null || existing.getRecordId() == null || incoming == null) {
            return;
        }
        campusIngestRecordDao.updateEnhancement(existing.getRecordId(), incoming, operatorUserId);
    }

    private CampusIngestRecord findDuplicateByExternalId(CampusIngestRecord record) {
        if (record == null || StringUtils.isBlank(record.getExternalId())) {
            return null;
        }
        return campusIngestRecordDao.selectDuplicateByExternalId(record.getSourceId(), record.getExternalId());
    }

    private CampusIngestRecord findDuplicateByContentHash(CampusIngestRecord record) {
        if (record == null || StringUtils.isBlank(record.getContentHash())) {
            return null;
        }
        return campusIngestRecordDao.selectDuplicateByContentHash(record.getSourceId(), record.getContentHash());
    }

    private CampusIngestRecord findNearDuplicateByPlatformTitle(CampusIngestRecord record) {
        if (record == null
                || !"xiaohongshu".equals(StringUtils.defaultString(record.getPlatform()).toLowerCase())
                || StringUtils.isBlank(record.getTitle())) {
            return null;
        }
        CampusIngestRecord duplicate = campusIngestRecordDao.selectDuplicateByPlatformTitle(
                record.getSourceId(), record.getPlatform(), record.getTitle());
        if (duplicate == null || duplicate.getRecordId() == null) {
            return null;
        }
        if (record.getRecordId() != null && record.getRecordId().equals(duplicate.getRecordId())) {
            return null;
        }
        return duplicate;
    }

    /**
     * Auto-convert newly inserted ingest records to clues.
     * Detects language and sets it on both record and clue.
     * Triggers auto-judgment after clue creation.
     * Failure-tolerant: one record failing does not block others.
     */
    private int autoConvertRecordsToClues(CampusIngestTask task,
                                           List<Long> insertedRecordIds,
                                           Long operatorUserId) {
        if (insertedRecordIds == null || insertedRecordIds.isEmpty()) {
            return 0;
        }
        int converted = 0;
        String operatorName = (operatorUserId == null || operatorUserId == 0L) ? "系统自动" : null;
        for (Long recordId : insertedRecordIds) {
            try {
                CampusIngestRecord record = campusIngestRecordDao.selectByRecordId(recordId);
                if (record == null) {
                    continue;
                }

                // Detect language using MinorityLanguageUtil
                String text = (record.getTitle() != null ? record.getTitle() : "") + " " +
                             (record.getContent() != null ? record.getContent() : "");
                String detectedLang = MinorityLanguageUtil.detect(text);
                if ("unknown".equals(detectedLang)) {
                    detectedLang = "zh";
                }

                // Update record with detected language
                campusIngestRecordDao.updateLanguage(recordId, detectedLang);

                // Convert to clue
                CampusClue clue = new CampusClue();
                clue.setClueTitle(StringUtils.defaultIfBlank(record.getTitle(), "未命名舆情线索"));
                clue.setClueContent(record.getContent());
                clue.setClueSource(record.getPlatform());
                clue.setSourcePlatform(record.getPlatform());
                clue.setOriginalUrl(record.getOriginalUrl());
                clue.setPublishTime(record.getPublishTime());
                clue.setDiscoverTime(new Date());
                clue.setInvolvedAccount(record.getAuthorName());
                clue.setKeywords(record.getKeywords());
                clue.setRiskLevel(RISK_NORMAL);
                clue.setSentiment(record.getSentiment());
                clue.setLanguage(detectedLang);

                CampusClue saved = campusClueService.save(clue, operatorUserId, operatorName);

                // Update record status to converted
                campusIngestRecordDao.updateStatus(recordId, STATUS_CONVERTED, "clue",
                        saved.getClueId(), null, operatorUserId);

                // Trigger auto-judgment
                try {
                    clueJudgmentService.autoJudge(saved);
                    campusClueDao.updateAfterJudgment(saved);
                } catch (Exception e) {
                    log.warn("Auto-judgment failed for clue {}, will need manual judgment: {}",
                            saved.getClueId(), e.getMessage());
                }

                converted++;
            } catch (Exception e) {
                log.warn("Auto-convert failed for record {}: {}", recordId, e.getMessage());
            }
        }
        return converted;
    }

    private void linkDetectionAfterSuccess(CampusIngestTask task,
                                           CampusIngestRunLog runLog,
                                           int insertedCount,
                                           Long operatorUserId) {
        if (runLog == null) {
            return;
        }
        try {
            CampusIngestDetectionLinkageResult result = campusIngestDetectionLinkageService.linkAfterIngestRun(
                    task, runLog, insertedCount, operatorUserId);
            if (result.hasResult()) {
                campusIngestRunLogDao.updateDetectionSummary(runLog.getRunId(),
                        result.getTriggerCount(), result.getHitCount(), result.getAlertCount(),
                        result.getErrorMessage());
            }
        } catch (RuntimeException ex) {
            campusIngestRunLogDao.updateDetectionSummary(runLog.getRunId(),
                    0, 0, 0, StringUtils.left("检测联动失败: " + ex.getMessage(), 2048));
        }
    }

    private String appendError(String current, String message) {
        if (StringUtils.isBlank(message)) {
            return current;
        }
        if (StringUtils.isBlank(current)) {
            return StringUtils.left(message, 2048);
        }
        return StringUtils.left(current + "; " + message, 2048);
    }

    private String classifyErrorType(String errorMessage, int failCount) {
        if (failCount > 0) {
            return ERROR_NORMALIZE_FAILED;
        }
        String message = StringUtils.defaultString(errorMessage).toLowerCase();
        if (message.contains("credential") || message.contains("密钥") || message.contains("key")) {
            return ERROR_CREDENTIAL_MISSING;
        }
        if (message.contains("unsupported") || message.contains("not supported")
                || message.contains("not allowlisted") || message.contains("暂不支持")) {
            return ERROR_ADAPTER_UNSUPPORTED;
        }
        if (message.contains("授权") || message.contains("启用状态") || message.contains("授权范围")
                || message.contains("来源") || message.contains("不能为空")
                || message.contains("计划表达式") || message.contains("白名单")
                || message.contains("url") || message.contains("域名")
                || message.contains("路径") || message.contains("格式")
                || message.contains("配置")) {
            return ERROR_VALIDATION;
        }
        if (message.contains("request failed") || message.contains("timeout")
                || message.contains("http") || message.contains("网络")) {
            return ERROR_REQUEST_FAILED;
        }
        if (message.contains("额度") || message.contains("quota")) {
            return ERROR_QUOTA_EXCEEDED;
        }
        if (message.contains("json") || message.contains("parse") || message.contains("映射")
                || message.contains("标准化") || message.contains("入库")) {
            return ERROR_NORMALIZE_FAILED;
        }
        return ERROR_UNKNOWN;
    }

    private Long durationSince(Date startTime) {
        if (startTime == null) {
            return null;
        }
        return Math.max(System.currentTimeMillis() - startTime.getTime(), 0L);
    }

    private long elapsedMillis(long startMillis) {
        return Math.max(System.currentTimeMillis() - startMillis, 0L);
    }

    private int defaultCount(Integer count) {
        return count == null ? 0 : count;
    }

    private RuntimeException asRuntimeException(Exception e) {
        if (e instanceof RuntimeException) {
            return (RuntimeException) e;
        }
        return new IllegalStateException(e.getMessage(), e);
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
}
