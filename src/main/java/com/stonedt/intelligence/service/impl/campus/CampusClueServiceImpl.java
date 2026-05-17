package com.stonedt.intelligence.service.impl.campus;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.stonedt.intelligence.dao.campus.CampusClueDao;
import com.stonedt.intelligence.dao.campus.CampusClueOperationLogDao;
import com.stonedt.intelligence.dao.campus.CampusDictDao;
import com.stonedt.intelligence.dao.campus.CampusIngestRecordDao;
import com.stonedt.intelligence.entity.campus.CampusClue;
import com.stonedt.intelligence.entity.campus.CampusClueOperationLog;
import com.stonedt.intelligence.entity.campus.CampusIngestRecord;
import com.stonedt.intelligence.service.campus.CampusClueService;
import com.stonedt.intelligence.service.campus.support.CampusRiskLevel;
import com.stonedt.intelligence.service.campus.support.CampusSchoolRelevance;
import com.stonedt.intelligence.service.campus.support.CampusSchoolRelevanceService;
import com.stonedt.intelligence.service.campus.support.CampusSentimentNormalizer;
import com.stonedt.intelligence.service.campus.support.CampusTopicClassification;
import com.stonedt.intelligence.service.campus.support.CampusTopicClassifier;
import com.stonedt.intelligence.util.MD5Util;
import com.stonedt.intelligence.util.SnowflakeUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class CampusClueServiceImpl implements CampusClueService {

    private static final String STATUS_PENDING_JUDGE = "pending_judge";
    private static final String STATUS_JUDGED = "judged";
    private static final String STATUS_ARCHIVED = "archived";

    private final CampusClueDao campusClueDao;
    private final CampusClueOperationLogDao campusClueOperationLogDao;
    private final CampusIngestRecordDao campusIngestRecordDao;
    private final CampusDictDao campusDictDao;
    private final CampusSchoolRelevanceService schoolRelevanceService = new CampusSchoolRelevanceService();
    private final CampusTopicClassifier topicClassifier = new CampusTopicClassifier();

    public CampusClueServiceImpl(CampusClueDao campusClueDao,
                                 CampusClueOperationLogDao campusClueOperationLogDao,
                                 CampusIngestRecordDao campusIngestRecordDao,
                                 CampusDictDao campusDictDao) {
        this.campusClueDao = campusClueDao;
        this.campusClueOperationLogDao = campusClueOperationLogDao;
        this.campusIngestRecordDao = campusIngestRecordDao;
        this.campusDictDao = campusDictDao;
    }

    @Override
    @Transactional
    public CampusClue save(CampusClue clue, Long operatorUserId, String operatorName) {
        validate(clue);
        if (clue.getClueId() == null) {
            clue.setClueId(SnowflakeUtil.getId());
            clue.setCreateUserId(operatorUserId);
            clue.setUpdateUserId(operatorUserId);
            setDefaults(clue);
            ensureDuplicateKey(clue);
            campusClueDao.insert(clue);
            addOperationLog(clue.getClueId(), "create", "新增线索", null, JSON.toJSONString(clue),
                    operatorUserId, operatorName);
            return campusClueDao.selectByClueId(clue.getClueId());
        }

        CampusClue old = requireClue(clue.getClueId());
        ensureNotArchived(old, "编辑线索");
        clue.setUpdateUserId(operatorUserId);
        setDefaultsForUpdate(clue);
        ensureDuplicateKey(clue);
        campusClueDao.update(clue);
        CampusClue saved = campusClueDao.selectByClueId(clue.getClueId());
        addOperationLog(clue.getClueId(), "update", "编辑线索", JSON.toJSONString(old), JSON.toJSONString(saved),
                operatorUserId, operatorName);
        return saved;
    }

    @Override
    public CampusClue detail(Long clueId) {
        CampusClue clue = requireClue(clueId);
        fillDetailFallback(clue);
        return clue;
    }

    @Override
    public PageInfo<CampusClue> list(Integer pageNum,
                                     Integer pageSize,
                                     String keyword,
                                     String clueSource,
                                     String sourcePlatform,
                                     String sourceSubPlatform,
                                     String riskLevel,
                                     String clueStatus,
                                     String language,
                                     String sentiment,
                                     String articleStatus,
                                     Date startTime,
                                     Date endTime,
                                     Date publishTimeStart,
                                     Date publishTimeEnd,
                                     Date collectTimeStart,
                                     Date collectTimeEnd,
                                     String matchScope,
                                     Boolean similarDedup,
                                     String sortBy) {
        PageHelper.startPage(defaultPageNum(pageNum), defaultPageSize(pageSize));
        return new PageInfo<>(campusClueDao.list(keyword, clueSource, sourcePlatform,
                sourceSubPlatform, CampusRiskLevel.normalizeForQuery(riskLevel), clueStatus, language, sentiment, articleStatus,
                startTime, endTime, publishTimeStart, publishTimeEnd,
                collectTimeStart, collectTimeEnd, matchScope, similarDedup, sortBy));
    }

    @Override
    @Transactional
    public CampusClue judge(Long clueId,
                            String riskLevel,
                            String judgeOpinion,
                            Long operatorUserId,
                            String operatorName) {
        if (StringUtils.isBlank(riskLevel)) {
            throw new IllegalArgumentException("风险等级不能为空");
        }
        String normalizedRiskLevel = CampusRiskLevel.requireValid(riskLevel);
        CampusClue old = requireClue(clueId);
        ensureStatus(old, "研判线索", STATUS_PENDING_JUDGE, STATUS_JUDGED);
        int updated = campusClueDao.judge(clueId, normalizedRiskLevel, judgeOpinion, operatorUserId, operatorUserId);
        if (updated != 1) {
            throw new IllegalArgumentException("线索状态已变化，不能继续研判");
        }
        CampusClue saved = campusClueDao.selectByClueId(clueId);
        addOperationLog(clueId, "judge", "研判线索", JSON.toJSONString(old), JSON.toJSONString(saved),
                operatorUserId, operatorName);
        return saved;
    }

    @Override
    @Transactional
    public CampusClue archive(Long clueId,
                              String archiveReason,
                              Long operatorUserId,
                              String operatorName) {
        CampusClue old = requireClue(clueId);
        ensureNotArchived(old, "归档线索");
        campusClueDao.archive(clueId, archiveReason, operatorUserId);
        CampusClue saved = campusClueDao.selectByClueId(clueId);
        addOperationLog(clueId, "archive", "归档线索", JSON.toJSONString(old), JSON.toJSONString(saved),
                operatorUserId, operatorName);
        return saved;
    }

    @Override
    @Transactional
    public CampusClue updateSentimentFromMonitor(Long clueId,
                                                 String sentiment,
                                                 Long monitorResultId,
                                                 Long operatorUserId,
                                                 String operatorName) {
        String normalized = requireSentiment(sentiment);
        CampusClue old = requireClue(clueId);
        ensureNotArchived(old, "修改情感");
        if (StringUtils.equals(normalized, old.getSentiment())) {
            return old;
        }
        int updated = campusClueDao.updateSentiment(clueId, normalized, operatorUserId);
        if (updated != 1) {
            throw new IllegalArgumentException("已归档线索不能修改情感");
        }
        CampusClue saved = campusClueDao.selectByClueId(clueId);
        addOperationLog(clueId, "sentiment_sync", "监测信息同步情感：" + monitorResultId,
                JSON.toJSONString(old), JSON.toJSONString(saved), operatorUserId, operatorName);
        return saved;
    }

    @Override
    @Transactional
    public void delete(Long clueId, Long operatorUserId, String operatorName) {
        CampusClue old = requireClue(clueId);
        campusClueDao.logicalDelete(clueId, operatorUserId);
        addOperationLog(clueId, "delete", "删除线索", JSON.toJSONString(old), null,
                operatorUserId, operatorName);
    }

    @Override
    public List<CampusClueOperationLog> operationLogs(Long clueId) {
        requireClue(clueId);
        return campusClueOperationLogDao.listByClueId(clueId);
    }

    @Override
    public List<Map<String, Object>> countByMediaType(String keyword,
                                                       String clueSource,
                                                       String sourcePlatform,
                                                       String sourceSubPlatform,
                                                       String riskLevel,
                                                       String clueStatus,
                                                       String language,
                                                       String sentiment,
                                                       String articleStatus,
                                                       Date startTime,
                                                       Date endTime,
                                                       Date publishTimeStart,
                                                       Date publishTimeEnd,
                                                       Date collectTimeStart,
                                                       Date collectTimeEnd,
                                                       String matchScope,
                                                       Boolean similarDedup) {
        return campusClueDao.countByMediaType(keyword, clueSource, sourcePlatform,
                sourceSubPlatform, CampusRiskLevel.normalizeForQuery(riskLevel), clueStatus, language, sentiment, articleStatus,
                startTime, endTime, publishTimeStart, publishTimeEnd,
                collectTimeStart, collectTimeEnd, matchScope, similarDedup);
    }

    @Override
    public List<Map<String, Object>> countBySubPlatform(String keyword,
                                                        String clueSource,
                                                        String sourcePlatform,
                                                        String sourceSubPlatform,
                                                        String riskLevel,
                                                        String clueStatus,
                                                        String language,
                                                        String sentiment,
                                                        String articleStatus,
                                                        Date startTime,
                                                        Date endTime,
                                                        Date publishTimeStart,
                                                        Date publishTimeEnd,
                                                        Date collectTimeStart,
                                                        Date collectTimeEnd,
                                                        String matchScope,
                                                        Boolean similarDedup) {
        return campusClueDao.countBySubPlatform(keyword, clueSource, sourcePlatform,
                sourceSubPlatform, CampusRiskLevel.normalizeForQuery(riskLevel), clueStatus, language, sentiment, articleStatus,
                startTime, endTime, publishTimeStart, publishTimeEnd,
                collectTimeStart, collectTimeEnd, matchScope, similarDedup);
    }

    private void validate(CampusClue clue) {
        if (clue == null) {
            throw new IllegalArgumentException("线索信息不能为空");
        }
        if (StringUtils.isBlank(clue.getClueTitle())) {
            throw new IllegalArgumentException("线索标题不能为空");
        }
    }

    private CampusClue requireClue(Long clueId) {
        if (clueId == null) {
            throw new IllegalArgumentException("线索ID不能为空");
        }
        CampusClue clue = campusClueDao.selectByClueId(clueId);
        if (clue == null) {
            throw new IllegalArgumentException("线索不存在");
        }
        return clue;
    }

    private String requireSentiment(String sentiment) {
        String normalized = CampusSentimentNormalizer.normalize(sentiment);
        if (StringUtils.isBlank(normalized)) {
            throw new IllegalArgumentException("情感类型只能为 positive、neutral、negative、none");
        }
        return normalized;
    }

    private void fillDetailFallback(CampusClue clue) {
        if (clue == null || clue.getClueId() == null || StringUtils.isNotBlank(clue.getClueContent())) {
            return;
        }
        CampusIngestRecord record = campusIngestRecordDao.selectByTarget("clue", clue.getClueId());
        if (record == null) {
            return;
        }
        if (StringUtils.isBlank(clue.getClueContent())) {
            clue.setClueContent(StringUtils.defaultIfBlank(record.getContent(), record.getTitle()));
        }
        if (StringUtils.isBlank(clue.getOriginalUrl())) {
            clue.setOriginalUrl(record.getOriginalUrl());
        }
        if (StringUtils.isBlank(clue.getInvolvedAccount())) {
            clue.setInvolvedAccount(record.getAuthorName());
        }
    }

    private void setDefaults(CampusClue clue) {
        if (clue.getDiscoverTime() == null) {
            clue.setDiscoverTime(new Date());
        }
        if (StringUtils.isBlank(clue.getRiskLevel())) {
            clue.setRiskLevel(CampusRiskLevel.normalCode());
        } else {
            clue.setRiskLevel(CampusRiskLevel.requireValid(clue.getRiskLevel()));
        }
        if (StringUtils.isBlank(clue.getClueStatus())) {
            clue.setClueStatus(STATUS_PENDING_JUDGE);
        }
        clue.setSentiment(CampusSentimentNormalizer.normalize(clue.getSentiment()));
        fillRelevanceAndTopic(clue, true);
        clue.setDeleted(0);
    }

    private void setDefaultsForUpdate(CampusClue clue) {
        if (StringUtils.isBlank(clue.getRiskLevel())) {
            clue.setRiskLevel(null);
        } else {
            clue.setRiskLevel(CampusRiskLevel.requireValid(clue.getRiskLevel()));
        }
        if (StringUtils.isBlank(clue.getClueStatus())) {
            clue.setClueStatus(null);
        }
        if (StringUtils.isNotBlank(clue.getSentiment())) {
            clue.setSentiment(CampusSentimentNormalizer.normalize(clue.getSentiment()));
        }
        fillRelevanceAndTopic(clue, false);
    }

    private void fillRelevanceAndTopic(CampusClue clue, boolean forceForCreate) {
        if (clue == null || (!forceForCreate && !hasClassificationText(clue))) {
            return;
        }
        if (clue.getSchoolRelevanceScore() == null) {
            CampusSchoolRelevance relevance = schoolRelevanceService.evaluateText(
                    clue.getClueTitle(), clue.getClueContent(), clue.getKeywords(), clue.getInvolvedAccount());
            clue.setSchoolRelevanceScore(relevance.getScore());
            clue.setSchoolRelevanceReason(limit(relevance.getReason(), 1024));
            clue.setMatchedSchoolTerms(limit(relevance.getMatchedSchoolTerms(), 512));
            clue.setExcludedReason(limit(relevance.getExcludedReason(), 512));
        }
        if (StringUtils.isBlank(clue.getTopicCategory())) {
            CampusTopicClassification topic = topicClassifier.classify(
                    clue.getClueTitle(), clue.getClueContent(), clue.getKeywords(),
                    campusDictDao.enabledItems(CampusTopicClassifier.DICT_TYPE));
            clue.setTopicCategory(limit(topic.getTopicCategory(), 64));
            clue.setTopicSubCategory(limit(topic.getTopicSubCategory(), 64));
            clue.setTopicReason(limit(topic.getReason(), 1024));
        }
    }

    private boolean hasClassificationText(CampusClue clue) {
        return StringUtils.isNotBlank(clue.getClueTitle())
                || StringUtils.isNotBlank(clue.getClueContent())
                || StringUtils.isNotBlank(clue.getKeywords())
                || StringUtils.isNotBlank(clue.getInvolvedAccount());
    }

    private void ensureDuplicateKey(CampusClue clue) {
        String duplicateKey = buildDuplicateKey(clue);
        clue.setDuplicateKey(duplicateKey);
        if (StringUtils.isBlank(duplicateKey)) {
            return;
        }
        int count = campusClueDao.countDuplicate(duplicateKey, clue.getClueId());
        if (count > 0) {
            throw new IllegalArgumentException("线索疑似重复，请确认后再入库");
        }
    }

    private String buildDuplicateKey(CampusClue clue) {
        String raw = StringUtils.defaultString(clue.getOriginalUrl()).trim();
        if (StringUtils.isBlank(raw)) {
            raw = StringUtils.defaultString(clue.getClueTitle()).trim()
                    + "|"
                    + StringUtils.defaultString(clue.getSourcePlatform()).trim();
        }
        if (StringUtils.isBlank(raw)) {
            return null;
        }
        return MD5Util.getMD5(raw);
    }

    private void addOperationLog(Long clueId,
                                 String operationType,
                                 String operationContent,
                                 String beforeValue,
                                 String afterValue,
                                 Long operatorUserId,
                                 String operatorName) {
        CampusClueOperationLog operationLog = new CampusClueOperationLog();
        operationLog.setLogId(SnowflakeUtil.getId());
        operationLog.setClueId(clueId);
        operationLog.setOperationType(operationType);
        operationLog.setOperationContent(operationContent);
        operationLog.setBeforeValue(limit(beforeValue, 4000));
        operationLog.setAfterValue(limit(afterValue, 4000));
        operationLog.setOperatorUserId(operatorUserId);
        operationLog.setOperatorName(operatorName);
        campusClueOperationLogDao.insert(operationLog);
    }

    private void ensureNotArchived(CampusClue clue, String operation) {
        if (clue != null && STATUS_ARCHIVED.equals(clue.getClueStatus())) {
            throw new IllegalArgumentException("已归档线索不能" + operation);
        }
    }

    private void ensureStatus(CampusClue clue, String operation, String... allowedStatuses) {
        if (clue == null) {
            throw new IllegalArgumentException("线索不存在");
        }
        String status = StringUtils.defaultString(clue.getClueStatus());
        for (String allowedStatus : allowedStatuses) {
            if (StringUtils.equals(status, allowedStatus)) {
                return;
            }
        }
        throw new IllegalArgumentException(operation + "不允许在当前线索状态执行");
    }

    private String limit(String value, int maxLength) {
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
}
