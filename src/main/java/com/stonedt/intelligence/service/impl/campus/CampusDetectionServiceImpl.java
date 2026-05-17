package com.stonedt.intelligence.service.impl.campus;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.stonedt.intelligence.dao.campus.CampusAccountContentDao;
import com.stonedt.intelligence.dao.campus.CampusClueDao;
import com.stonedt.intelligence.dao.campus.CampusDetectionHitDao;
import com.stonedt.intelligence.dao.campus.CampusDetectionRuleDao;
import com.stonedt.intelligence.dao.campus.CampusDetectionRunLogDao;
import com.stonedt.intelligence.dao.campus.CampusDetectionTaskDao;
import com.stonedt.intelligence.dao.campus.CampusDetectionTopicDao;
import com.stonedt.intelligence.dao.campus.CampusIngestRecordDao;
import com.stonedt.intelligence.entity.campus.CampusAccountContent;
import com.stonedt.intelligence.entity.campus.CampusAlert;
import com.stonedt.intelligence.entity.campus.CampusClue;
import com.stonedt.intelligence.entity.campus.CampusDetectionHit;
import com.stonedt.intelligence.entity.campus.CampusDetectionRule;
import com.stonedt.intelligence.entity.campus.CampusDetectionRunLog;
import com.stonedt.intelligence.entity.campus.CampusDetectionTask;
import com.stonedt.intelligence.entity.campus.CampusDetectionTopic;
import com.stonedt.intelligence.entity.campus.CampusIngestRecord;
import com.stonedt.intelligence.service.campus.CampusAlertService;
import com.stonedt.intelligence.service.campus.CampusDetectionService;
import com.stonedt.intelligence.service.campus.support.CampusRiskLevel;
import com.stonedt.intelligence.util.SnowflakeUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@Service
public class CampusDetectionServiceImpl implements CampusDetectionService {

    private static final String OBJECT_INGEST_RECORD = "ingest_record";
    private static final String OBJECT_INGEST_RUN = "ingest_run";
    private static final String OBJECT_CLUE = "clue";
    private static final String OBJECT_ACCOUNT_CONTENT = "account_content";
    private static final String TASK_ACTIVE = "active";
    private static final String TASK_PAUSED = "paused";
    private static final String TASK_DISABLED = "disabled";
    private static final String HIT_PENDING = "pending";
    private static final String HIT_ALERTED = "alerted";
    private static final String HIT_IGNORED = "ignored";
    private static final String RUN_RUNNING = "running";
    private static final String RUN_SUCCESS = "success";
    private static final String RUN_FAILED = "failed";
    private static final String RISK_NORMAL = CampusRiskLevel.normalCode();
    private static final String RISK_CONCERN = CampusRiskLevel.concernCode();
    private static final String RULE_KEYWORD_ANY = "keyword_any";
    private static final String RULE_KEYWORD_ALL = "keyword_all";
    private static final String RULE_EXACT = "exact";
    private static final String RULE_REGEX = "regex";
    private static final String RULE_RISK_LEVEL = "risk_level";
    private static final String TRIGGER_MANUAL = "manual";
    private static final String TRIGGER_INGEST_RUN = "ingest_run";

    private final CampusDetectionTopicDao campusDetectionTopicDao;
    private final CampusDetectionRuleDao campusDetectionRuleDao;
    private final CampusDetectionTaskDao campusDetectionTaskDao;
    private final CampusDetectionHitDao campusDetectionHitDao;
    private final CampusDetectionRunLogDao campusDetectionRunLogDao;
    private final CampusIngestRecordDao campusIngestRecordDao;
    private final CampusClueDao campusClueDao;
    private final CampusAccountContentDao campusAccountContentDao;
    private final CampusAlertService campusAlertService;

    public CampusDetectionServiceImpl(CampusDetectionTopicDao campusDetectionTopicDao,
                                      CampusDetectionRuleDao campusDetectionRuleDao,
                                      CampusDetectionTaskDao campusDetectionTaskDao,
                                      CampusDetectionHitDao campusDetectionHitDao,
                                      CampusDetectionRunLogDao campusDetectionRunLogDao,
                                      CampusIngestRecordDao campusIngestRecordDao,
                                      CampusClueDao campusClueDao,
                                      CampusAccountContentDao campusAccountContentDao,
                                      CampusAlertService campusAlertService) {
        this.campusDetectionTopicDao = campusDetectionTopicDao;
        this.campusDetectionRuleDao = campusDetectionRuleDao;
        this.campusDetectionTaskDao = campusDetectionTaskDao;
        this.campusDetectionHitDao = campusDetectionHitDao;
        this.campusDetectionRunLogDao = campusDetectionRunLogDao;
        this.campusIngestRecordDao = campusIngestRecordDao;
        this.campusClueDao = campusClueDao;
        this.campusAccountContentDao = campusAccountContentDao;
        this.campusAlertService = campusAlertService;
    }

    @Override
    public CampusDetectionTopic saveTopic(CampusDetectionTopic topic, Long operatorUserId) {
        validateTopic(topic);
        if (topic.getTopicId() == null) {
            topic.setTopicId(SnowflakeUtil.getId());
            topic.setCreateUserId(operatorUserId);
            topic.setUpdateUserId(operatorUserId);
            setTopicDefaults(topic);
            campusDetectionTopicDao.insert(topic);
            return campusDetectionTopicDao.selectByTopicId(topic.getTopicId());
        }
        requireTopic(topic.getTopicId());
        topic.setUpdateUserId(operatorUserId);
        normalizeTopicRisk(topic);
        campusDetectionTopicDao.update(topic);
        return campusDetectionTopicDao.selectByTopicId(topic.getTopicId());
    }

    @Override
    public void deleteTopic(Long topicId, Long operatorUserId) {
        requireTopic(topicId);
        campusDetectionTopicDao.logicalDelete(topicId, operatorUserId);
    }

    @Override
    public PageInfo<CampusDetectionTopic> listTopics(Integer pageNum,
                                                    Integer pageSize,
                                                    String keyword,
                                                    String topicCategory,
                                                    Integer enabled) {
        PageHelper.startPage(defaultPageNum(pageNum), defaultPageSize(pageSize));
        return new PageInfo<>(campusDetectionTopicDao.list(keyword, topicCategory, enabled));
    }

    @Override
    public CampusDetectionRule saveRule(CampusDetectionRule rule, Long operatorUserId) {
        validateRule(rule);
        requireTopic(rule.getTopicId());
        if (rule.getRuleId() == null) {
            rule.setRuleId(SnowflakeUtil.getId());
            rule.setCreateUserId(operatorUserId);
            rule.setUpdateUserId(operatorUserId);
            setRuleDefaults(rule);
            campusDetectionRuleDao.insert(rule);
            return campusDetectionRuleDao.selectByRuleId(rule.getRuleId());
        }
        requireRule(rule.getRuleId());
        rule.setUpdateUserId(operatorUserId);
        normalizeRuleRisk(rule);
        campusDetectionRuleDao.update(rule);
        return campusDetectionRuleDao.selectByRuleId(rule.getRuleId());
    }

    @Override
    public void deleteRule(Long ruleId, Long operatorUserId) {
        requireRule(ruleId);
        campusDetectionRuleDao.logicalDelete(ruleId, operatorUserId);
    }

    @Override
    public PageInfo<CampusDetectionRule> listRules(Integer pageNum,
                                                  Integer pageSize,
                                                  Long topicId,
                                                  String ruleType,
                                                  Integer enabled) {
        PageHelper.startPage(defaultPageNum(pageNum), defaultPageSize(pageSize));
        return new PageInfo<>(campusDetectionRuleDao.list(topicId, ruleType, enabled));
    }

    @Override
    public CampusDetectionTask saveTask(CampusDetectionTask task, Long operatorUserId) {
        validateTask(task);
        requireTopic(task.getTopicId());
        if (task.getDetectionTaskId() == null) {
            task.setDetectionTaskId(SnowflakeUtil.getId());
            task.setCreateUserId(operatorUserId);
            task.setUpdateUserId(operatorUserId);
            setTaskDefaults(task);
            campusDetectionTaskDao.insert(task);
            return campusDetectionTaskDao.selectByTaskId(task.getDetectionTaskId());
        }
        requireTask(task.getDetectionTaskId());
        task.setUpdateUserId(operatorUserId);
        campusDetectionTaskDao.update(task);
        return campusDetectionTaskDao.selectByTaskId(task.getDetectionTaskId());
    }

    @Override
    public CampusDetectionTask updateTaskStatus(Long detectionTaskId, String taskStatus, Long operatorUserId) {
        requireTask(detectionTaskId);
        validateTaskStatus(taskStatus);
        campusDetectionTaskDao.updateStatus(detectionTaskId, taskStatus, operatorUserId);
        return campusDetectionTaskDao.selectByTaskId(detectionTaskId);
    }

    @Override
    public void deleteTask(Long detectionTaskId, Long operatorUserId) {
        requireTask(detectionTaskId);
        campusDetectionTaskDao.logicalDelete(detectionTaskId, operatorUserId);
    }

    @Override
    public PageInfo<CampusDetectionTask> listTasks(Integer pageNum,
                                                  Integer pageSize,
                                                  String keyword,
                                                  Long topicId,
                                                  String taskStatus) {
        PageHelper.startPage(defaultPageNum(pageNum), defaultPageSize(pageSize));
        return new PageInfo<>(campusDetectionTaskDao.list(keyword, topicId, taskStatus));
    }

    @Override
    public CampusDetectionRunLog runTask(Long detectionTaskId, Long operatorUserId) {
        return runTaskInternal(detectionTaskId, operatorUserId, DetectionRunContext.manual());
    }

    @Override
    public CampusDetectionRunLog runIngestRecordTask(Long detectionTaskId, Long ingestRunId, Long operatorUserId) {
        if (ingestRunId == null) {
            throw new IllegalArgumentException("接入运行ID不能为空");
        }
        return runTaskInternal(detectionTaskId, operatorUserId, DetectionRunContext.ingestRun(ingestRunId));
    }

    private CampusDetectionRunLog runTaskInternal(Long detectionTaskId,
                                                  Long operatorUserId,
                                                  DetectionRunContext runContext) {
        CampusDetectionTask task = requireTask(detectionTaskId);
        if (runContext != null && runContext.isIngestRun()) {
            if (!TASK_ACTIVE.equals(task.getTaskStatus())) {
                throw new IllegalArgumentException("自动检测仅允许启用状态的检测任务");
            }
        } else if (TASK_DISABLED.equals(task.getTaskStatus())) {
            throw new IllegalArgumentException("检测任务已禁用，不能运行");
        }
        CampusDetectionTopic topic = requireTopic(task.getTopicId());
        if (topic.getEnabled() != null && topic.getEnabled() == 0) {
            throw new IllegalArgumentException("检测主题已停用，不能运行");
        }

        Long runLogId = SnowflakeUtil.getId();
        CampusDetectionRunLog runLog = new CampusDetectionRunLog();
        runLog.setRunLogId(runLogId);
        runLog.setDetectionTaskId(detectionTaskId);
        runLog.setRunStatus(RUN_RUNNING);
        runLog.setTriggerType(runContext == null ? TRIGGER_MANUAL : runContext.getTriggerType());
        runLog.setTriggerObjectType(runContext == null ? null : runContext.getTriggerObjectType());
        runLog.setTriggerObjectId(runContext == null ? null : runContext.getTriggerObjectId());
        runLog.setStartTime(new Date());
        runLog.setScannedCount(0);
        runLog.setHitCount(0);
        runLog.setAlertCount(0);
        runLog.setCreateUserId(operatorUserId);
        campusDetectionRunLogDao.insert(runLog);

        DetectionCounter counter = new DetectionCounter();
        try {
            Date endTime = new Date();
            Date startTime = resolveStartTime(task, endTime);
            List<CampusDetectionRule> rules = campusDetectionRuleDao.listEnabledByTopicId(topic.getTopicId());
            Set<String> objectTypes = splitTokens(StringUtils.defaultIfBlank(task.getObjectTypes(),
                    OBJECT_INGEST_RECORD + "," + OBJECT_CLUE + "," + OBJECT_ACCOUNT_CONTENT));

            if (runContext != null && runContext.isIngestRun()) {
                if (!objectTypes.contains(OBJECT_INGEST_RECORD)) {
                    throw new IllegalArgumentException("检测任务未包含接入记录对象，不能用于接入联动");
                }
                scanIngestRecordsByRunId(task, topic, rules, runContext.getTriggerObjectId(), operatorUserId, counter);
            } else if (objectTypes.contains(OBJECT_INGEST_RECORD)) {
                scanIngestRecords(task, topic, rules, startTime, endTime, operatorUserId, counter);
            }
            if ((runContext == null || !runContext.isIngestRun()) && objectTypes.contains(OBJECT_CLUE)) {
                scanClues(task, topic, rules, startTime, endTime, operatorUserId, counter);
            }
            if ((runContext == null || !runContext.isIngestRun()) && objectTypes.contains(OBJECT_ACCOUNT_CONTENT)) {
                scanAccountContents(task, topic, rules, startTime, endTime, operatorUserId, counter);
            }

            campusDetectionTaskDao.updateLastRunTime(detectionTaskId, operatorUserId);
            campusDetectionRunLogDao.finish(runLogId, RUN_SUCCESS, counter.scannedCount,
                    counter.hitCount, counter.alertCount, null);
            return campusDetectionRunLogDao.selectByRunLogId(runLogId);
        } catch (RuntimeException e) {
            campusDetectionRunLogDao.finish(runLogId, RUN_FAILED, counter.scannedCount,
                    counter.hitCount, counter.alertCount, summary(e.getMessage(), 2048));
            throw e;
        }
    }

    @Override
    public CampusDetectionHit alertHit(Long hitId, Long operatorUserId) {
        CampusDetectionHit hit = requireHit(hitId);
        if (HIT_ALERTED.equals(hit.getHitStatus()) && hit.getAlertId() != null) {
            return hit;
        }
        CampusAlert alert = createAlertForHit(hit, operatorUserId);
        campusDetectionHitDao.updateStatus(hitId, HIT_ALERTED, alert.getAlertId(), hit.getClueId(), operatorUserId);
        return campusDetectionHitDao.selectByHitId(hitId);
    }

    @Override
    public CampusDetectionHit ignoreHit(Long hitId, Long operatorUserId) {
        CampusDetectionHit hit = requireHit(hitId);
        campusDetectionHitDao.updateStatus(hitId, HIT_IGNORED, hit.getAlertId(), hit.getClueId(), operatorUserId);
        return campusDetectionHitDao.selectByHitId(hitId);
    }

    @Override
    public PageInfo<CampusDetectionHit> listHits(Integer pageNum,
                                                Integer pageSize,
                                                Long detectionTaskId,
                                                Long topicId,
                                                String objectType,
                                                String hitStatus,
                                                String riskLevel,
                                                String keyword) {
        PageHelper.startPage(defaultPageNum(pageNum), defaultPageSize(pageSize));
        return new PageInfo<>(campusDetectionHitDao.list(detectionTaskId, topicId,
                objectType, hitStatus, CampusRiskLevel.normalizeForQuery(riskLevel), keyword));
    }

    @Override
    public PageInfo<CampusDetectionRunLog> listRunLogs(Integer pageNum, Integer pageSize, Long detectionTaskId) {
        if (detectionTaskId == null) {
            throw new IllegalArgumentException("检测任务ID不能为空");
        }
        PageHelper.startPage(defaultPageNum(pageNum), defaultPageSize(pageSize));
        return new PageInfo<>(campusDetectionRunLogDao.listByTaskId(detectionTaskId));
    }

    private void scanIngestRecords(CampusDetectionTask task,
                                   CampusDetectionTopic topic,
                                   List<CampusDetectionRule> rules,
                                   Date startTime,
                                   Date endTime,
                                   Long operatorUserId,
                                   DetectionCounter counter) {
        List<CampusIngestRecord> records = campusIngestRecordDao.listForDetection(startTime, endTime);
        for (CampusIngestRecord record : records) {
            DetectionObject object = DetectionObject.fromIngestRecord(record);
            scanObject(task, topic, rules, object, operatorUserId, counter);
        }
    }

    private void scanIngestRecordsByRunId(CampusDetectionTask task,
                                          CampusDetectionTopic topic,
                                          List<CampusDetectionRule> rules,
                                          Long ingestRunId,
                                          Long operatorUserId,
                                          DetectionCounter counter) {
        List<CampusIngestRecord> records = campusIngestRecordDao.listForDetectionByRunId(ingestRunId);
        for (CampusIngestRecord record : records) {
            DetectionObject object = DetectionObject.fromIngestRecord(record);
            scanObject(task, topic, rules, object, operatorUserId, counter);
        }
    }

    private void scanClues(CampusDetectionTask task,
                           CampusDetectionTopic topic,
                           List<CampusDetectionRule> rules,
                           Date startTime,
                           Date endTime,
                           Long operatorUserId,
                           DetectionCounter counter) {
        List<CampusClue> clues = campusClueDao.listForDetection(startTime, endTime);
        for (CampusClue clue : clues) {
            DetectionObject object = DetectionObject.fromClue(clue);
            scanObject(task, topic, rules, object, operatorUserId, counter);
        }
    }

    private void scanAccountContents(CampusDetectionTask task,
                                     CampusDetectionTopic topic,
                                     List<CampusDetectionRule> rules,
                                     Date startTime,
                                     Date endTime,
                                     Long operatorUserId,
                                     DetectionCounter counter) {
        List<CampusAccountContent> contents = campusAccountContentDao.listForDetection(startTime, endTime);
        for (CampusAccountContent content : contents) {
            DetectionObject object = DetectionObject.fromAccountContent(content);
            scanObject(task, topic, rules, object, operatorUserId, counter);
        }
    }

    private void scanObject(CampusDetectionTask task,
                            CampusDetectionTopic topic,
                            List<CampusDetectionRule> rules,
                            DetectionObject object,
                            Long operatorUserId,
                            DetectionCounter counter) {
        counter.scannedCount++;
        if (!scopeMatches(topic.getPlatformScope(), object.platform)
                || !scopeMatches(topic.getSourceScope(), object.sourceScope)) {
            return;
        }
        String text = joinText(object.title, object.content, object.keywords, object.authorName);
        if (containsAny(text, splitTokens(topic.getExcludeWords()))) {
            return;
        }

        List<DetectionMatch> matches = new ArrayList<>();
        if (rules == null || rules.isEmpty()) {
            DetectionMatch match = matchKeywordAny(topic.getKeywords(), text,
                    CampusRiskLevel.normalizeOrDefault(StringUtils.defaultIfBlank(topic.getRiskLevel(), RISK_CONCERN)), null);
            if (match.matched) {
                matches.add(match);
            }
        } else {
            for (CampusDetectionRule rule : rules) {
                DetectionMatch match = matchRule(rule, topic, object, text);
                if (match.matched) {
                    matches.add(match);
                }
            }
        }

        for (DetectionMatch match : matches) {
            CampusDetectionHit hit = createHitIfAbsent(task, topic, object, match, operatorUserId);
            if (hit == null) {
                continue;
            }
            counter.hitCount++;
            if (task.getAutoAlert() != null && task.getAutoAlert() == 1) {
                CampusAlert alert = createAlertForHit(hit, operatorUserId);
                campusDetectionHitDao.updateStatus(hit.getHitId(), HIT_ALERTED,
                        alert.getAlertId(), hit.getClueId(), operatorUserId);
                counter.alertCount++;
            }
        }
    }

    private DetectionMatch matchRule(CampusDetectionRule rule,
                                     CampusDetectionTopic topic,
                                     DetectionObject object,
                                     String text) {
        if (rule == null || rule.getEnabled() == null || rule.getEnabled() == 0) {
            return DetectionMatch.none();
        }
        if (containsAny(text, splitTokens(rule.getExcludeWords()))) {
            return DetectionMatch.none();
        }
        String riskLevel = CampusRiskLevel.normalizeOrDefault(StringUtils.defaultIfBlank(rule.getRiskLevel(),
                StringUtils.defaultIfBlank(topic.getRiskLevel(), RISK_CONCERN)));
        String ruleType = StringUtils.defaultIfBlank(rule.getRuleType(), RULE_KEYWORD_ANY);
        if (RULE_KEYWORD_ALL.equals(ruleType)) {
            return matchKeywordAll(rule.getRuleCondition(), text, riskLevel, rule.getRuleId());
        }
        if (RULE_EXACT.equals(ruleType)) {
            return matchExact(rule.getRuleCondition(), object, text, riskLevel, rule.getRuleId());
        }
        if (RULE_REGEX.equals(ruleType)) {
            return matchRegex(rule.getRuleCondition(), text, riskLevel, rule.getRuleId());
        }
        if (RULE_RISK_LEVEL.equals(ruleType)) {
            return matchRiskLevel(rule.getRuleCondition(), object.riskLevel, riskLevel, rule.getRuleId());
        }
        return matchKeywordAny(rule.getRuleCondition(), text, riskLevel, rule.getRuleId());
    }

    private DetectionMatch matchKeywordAny(String condition, String text, String riskLevel, Long ruleId) {
        Set<String> tokens = splitTokens(condition);
        Set<String> matched = matchTokens(text, tokens);
        if (matched.isEmpty()) {
            return DetectionMatch.none();
        }
        return DetectionMatch.of(ruleId, StringUtils.join(matched, ","), riskLevel);
    }

    private DetectionMatch matchKeywordAll(String condition, String text, String riskLevel, Long ruleId) {
        Set<String> tokens = splitTokens(condition);
        if (tokens.isEmpty()) {
            return DetectionMatch.none();
        }
        Set<String> matched = matchTokens(text, tokens);
        if (matched.size() != tokens.size()) {
            return DetectionMatch.none();
        }
        return DetectionMatch.of(ruleId, StringUtils.join(matched, ","), riskLevel);
    }

    private DetectionMatch matchExact(String condition,
                                      DetectionObject object,
                                      String text,
                                      String riskLevel,
                                      Long ruleId) {
        Set<String> tokens = splitTokens(condition);
        for (String token : tokens) {
            if (token.equals(StringUtils.defaultString(object.title))
                    || token.equals(StringUtils.defaultString(object.content))
                    || token.equals(StringUtils.defaultString(text))) {
                return DetectionMatch.of(ruleId, token, riskLevel);
            }
        }
        return DetectionMatch.none();
    }

    private DetectionMatch matchRegex(String condition, String text, String riskLevel, Long ruleId) {
        if (StringUtils.isBlank(condition) || StringUtils.isBlank(text)) {
            return DetectionMatch.none();
        }
        try {
            Pattern pattern = Pattern.compile(condition);
            if (pattern.matcher(text).find()) {
                return DetectionMatch.of(ruleId, condition, riskLevel);
            }
            return DetectionMatch.none();
        } catch (PatternSyntaxException e) {
            return DetectionMatch.none();
        }
    }

    private DetectionMatch matchRiskLevel(String condition, String objectRiskLevel, String riskLevel, Long ruleId) {
        String normalizedObjectRiskLevel = CampusRiskLevel.normalizeForQuery(objectRiskLevel);
        if (StringUtils.isBlank(normalizedObjectRiskLevel)) {
            return DetectionMatch.none();
        }
        Set<String> configured = splitRiskTokens(condition);
        if (configured.isEmpty()) {
            if (CampusRiskLevel.isNonNormal(normalizedObjectRiskLevel)) {
                return DetectionMatch.of(ruleId, normalizedObjectRiskLevel,
                        CampusRiskLevel.normalizeOrDefault(normalizedObjectRiskLevel));
            }
            return DetectionMatch.none();
        }
        if (configured.contains(normalizedObjectRiskLevel)) {
            return DetectionMatch.of(ruleId, normalizedObjectRiskLevel,
                    CampusRiskLevel.normalizeOrDefault(normalizedObjectRiskLevel));
        }
        return DetectionMatch.none();
    }

    private CampusDetectionHit createHitIfAbsent(CampusDetectionTask task,
                                                 CampusDetectionTopic topic,
                                                 DetectionObject object,
                                                 DetectionMatch match,
                                                 Long operatorUserId) {
        int count = campusDetectionHitDao.countExisting(task.getDetectionTaskId(), object.objectType,
                object.objectId, match.ruleId, match.matchedKeywords);
        if (count > 0) {
            return null;
        }
        CampusDetectionHit hit = new CampusDetectionHit();
        hit.setHitId(SnowflakeUtil.getId());
        hit.setDetectionTaskId(task.getDetectionTaskId());
        hit.setTopicId(topic.getTopicId());
        hit.setRuleId(match.ruleId);
        hit.setObjectType(object.objectType);
        hit.setObjectId(object.objectId);
        hit.setObjectTitle(summary(defaultTitle(object.title), 512));
        hit.setPlatform(summary(object.platform, 64));
        hit.setMatchedKeywords(summary(match.matchedKeywords, 1024));
        hit.setRiskLevel(CampusRiskLevel.normalizeOrDefault(StringUtils.defaultIfBlank(match.riskLevel,
                StringUtils.defaultIfBlank(topic.getRiskLevel(), RISK_CONCERN))));
        hit.setHitContent(summary(joinText(object.title, object.content, object.keywords, object.authorName), 4000));
        hit.setHitStatus(HIT_PENDING);
        hit.setClueId(OBJECT_CLUE.equals(object.objectType) ? object.objectId : null);
        hit.setDeleted(0);
        hit.setCreateUserId(operatorUserId);
        hit.setUpdateUserId(operatorUserId);
        campusDetectionHitDao.insert(hit);
        return campusDetectionHitDao.selectByHitId(hit.getHitId());
    }

    private CampusAlert createAlertForHit(CampusDetectionHit hit, Long operatorUserId) {
        CampusAlert alert = new CampusAlert();
        alert.setAlertTitle(summary("检测预警：" + defaultTitle(hit.getObjectTitle()), 255));
        alert.setAlertContent(summary(hit.getHitContent(), 4000));
        alert.setAlertSource("detection");
        alert.setSourceObjectId(hit.getHitId());
        alert.setRuleId(hit.getRuleId());
        alert.setRiskLevel(CampusRiskLevel.normalizeOrDefault(StringUtils.defaultIfBlank(hit.getRiskLevel(), RISK_CONCERN)));
        alert.setMatchedKeywords(summary(hit.getMatchedKeywords(), 512));
        alert.setEvidenceJson(summary(buildHitEvidenceJson(hit), 4000));
        alert.setAlertStatus("pending");
        return campusAlertService.createAlert(alert, operatorUserId);
    }

    private String buildHitEvidenceJson(CampusDetectionHit hit) {
        JSONObject evidence = new JSONObject();
        evidence.put("source", "detection");
        evidence.put("hitId", hit.getHitId());
        evidence.put("detectionTaskId", hit.getDetectionTaskId());
        evidence.put("topicId", hit.getTopicId());
        evidence.put("ruleId", hit.getRuleId());
        evidence.put("objectType", hit.getObjectType());
        evidence.put("objectId", hit.getObjectId());
        evidence.put("riskLevel", hit.getRiskLevel());
        evidence.put("matchedKeywords", hit.getMatchedKeywords());
        evidence.put("platform", hit.getPlatform());
        return evidence.toJSONString();
    }

    private Date resolveStartTime(CampusDetectionTask task, Date endTime) {
        Integer hours = task.getScanWindowHours();
        if (hours == null || hours <= 0) {
            hours = 24;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(endTime);
        calendar.add(Calendar.HOUR, -hours);
        return calendar.getTime();
    }

    private boolean scopeMatches(String configuredScope, String candidateScope) {
        Set<String> configured = splitTokens(configuredScope);
        if (configured.isEmpty()) {
            return true;
        }
        for (String token : configured) {
            if ("*".equals(token) || "all".equalsIgnoreCase(token)) {
                return true;
            }
        }
        Set<String> candidates = splitTokens(candidateScope);
        for (String token : configured) {
            for (String candidate : candidates) {
                if (token.equalsIgnoreCase(candidate)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean containsAny(String text, Set<String> tokens) {
        if (tokens.isEmpty()) {
            return false;
        }
        String safeText = StringUtils.defaultString(text);
        for (String token : tokens) {
            if (StringUtils.isNotBlank(token) && safeText.contains(token)) {
                return true;
            }
        }
        return false;
    }

    private Set<String> matchTokens(String text, Set<String> tokens) {
        Set<String> matched = new LinkedHashSet<>();
        String safeText = StringUtils.defaultString(text);
        for (String token : tokens) {
            if (StringUtils.isNotBlank(token) && safeText.contains(token)) {
                matched.add(token);
            }
        }
        return matched;
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

    private Set<String> splitRiskTokens(String raw) {
        Set<String> tokens = new LinkedHashSet<>();
        for (String token : splitTokens(raw)) {
            String normalized = CampusRiskLevel.normalizeForQuery(token);
            if (StringUtils.isNotBlank(normalized)) {
                tokens.add(normalized);
            }
        }
        return tokens;
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

    private void validateTopic(CampusDetectionTopic topic) {
        if (topic == null) {
            throw new IllegalArgumentException("检测主题不能为空");
        }
        if (StringUtils.isBlank(topic.getTopicName())) {
            throw new IllegalArgumentException("检测主题名称不能为空");
        }
    }

    private void validateRule(CampusDetectionRule rule) {
        if (rule == null) {
            throw new IllegalArgumentException("检测规则不能为空");
        }
        if (rule.getTopicId() == null) {
            throw new IllegalArgumentException("检测主题ID不能为空");
        }
        if (StringUtils.isBlank(rule.getRuleName())) {
            throw new IllegalArgumentException("检测规则名称不能为空");
        }
        if (StringUtils.isBlank(rule.getRuleType())) {
            throw new IllegalArgumentException("检测规则类型不能为空");
        }
    }

    private void validateTask(CampusDetectionTask task) {
        if (task == null) {
            throw new IllegalArgumentException("检测任务不能为空");
        }
        if (task.getTopicId() == null) {
            throw new IllegalArgumentException("检测主题ID不能为空");
        }
        if (StringUtils.isBlank(task.getTaskName())) {
            throw new IllegalArgumentException("检测任务名称不能为空");
        }
        if (StringUtils.isNotBlank(task.getTaskStatus())) {
            validateTaskStatus(task.getTaskStatus());
        }
    }

    private void validateTaskStatus(String taskStatus) {
        if (!TASK_ACTIVE.equals(taskStatus) && !TASK_PAUSED.equals(taskStatus) && !TASK_DISABLED.equals(taskStatus)) {
            throw new IllegalArgumentException("检测任务状态不合法");
        }
    }

    private CampusDetectionTopic requireTopic(Long topicId) {
        if (topicId == null) {
            throw new IllegalArgumentException("检测主题ID不能为空");
        }
        CampusDetectionTopic topic = campusDetectionTopicDao.selectByTopicId(topicId);
        if (topic == null) {
            throw new IllegalArgumentException("检测主题不存在");
        }
        return topic;
    }

    private CampusDetectionRule requireRule(Long ruleId) {
        if (ruleId == null) {
            throw new IllegalArgumentException("检测规则ID不能为空");
        }
        CampusDetectionRule rule = campusDetectionRuleDao.selectByRuleId(ruleId);
        if (rule == null) {
            throw new IllegalArgumentException("检测规则不存在");
        }
        return rule;
    }

    private CampusDetectionTask requireTask(Long detectionTaskId) {
        if (detectionTaskId == null) {
            throw new IllegalArgumentException("检测任务ID不能为空");
        }
        CampusDetectionTask task = campusDetectionTaskDao.selectByTaskId(detectionTaskId);
        if (task == null) {
            throw new IllegalArgumentException("检测任务不存在");
        }
        return task;
    }

    private CampusDetectionHit requireHit(Long hitId) {
        if (hitId == null) {
            throw new IllegalArgumentException("检测命中ID不能为空");
        }
        CampusDetectionHit hit = campusDetectionHitDao.selectByHitId(hitId);
        if (hit == null) {
            throw new IllegalArgumentException("检测命中不存在");
        }
        return hit;
    }

    private void setTopicDefaults(CampusDetectionTopic topic) {
        normalizeTopicRisk(topic);
        if (topic.getEnabled() == null) {
            topic.setEnabled(1);
        }
        topic.setDeleted(0);
    }

    private void setRuleDefaults(CampusDetectionRule rule) {
        normalizeRuleRisk(rule);
        if (rule.getEnabled() == null) {
            rule.setEnabled(1);
        }
        if (rule.getSortNo() == null) {
            rule.setSortNo(0);
        }
        rule.setDeleted(0);
    }

    private void normalizeTopicRisk(CampusDetectionTopic topic) {
        if (StringUtils.isBlank(topic.getRiskLevel())) {
            topic.setRiskLevel(RISK_CONCERN);
        } else {
            topic.setRiskLevel(CampusRiskLevel.requireValid(topic.getRiskLevel()));
        }
    }

    private void normalizeRuleRisk(CampusDetectionRule rule) {
        if (StringUtils.isBlank(rule.getRiskLevel())) {
            rule.setRiskLevel(RISK_CONCERN);
        } else {
            rule.setRiskLevel(CampusRiskLevel.requireValid(rule.getRiskLevel()));
        }
    }

    private void setTaskDefaults(CampusDetectionTask task) {
        if (StringUtils.isBlank(task.getObjectTypes())) {
            task.setObjectTypes(OBJECT_INGEST_RECORD + "," + OBJECT_CLUE + "," + OBJECT_ACCOUNT_CONTENT);
        }
        if (StringUtils.isBlank(task.getTaskStatus())) {
            task.setTaskStatus(TASK_PAUSED);
        }
        if (task.getScanWindowHours() == null || task.getScanWindowHours() <= 0) {
            task.setScanWindowHours(24);
        }
        if (task.getAutoAlert() == null) {
            task.setAutoAlert(1);
        }
        task.setDeleted(0);
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

    private static class DetectionRunContext {

        private final String triggerType;
        private final String triggerObjectType;
        private final Long triggerObjectId;

        private DetectionRunContext(String triggerType, String triggerObjectType, Long triggerObjectId) {
            this.triggerType = triggerType;
            this.triggerObjectType = triggerObjectType;
            this.triggerObjectId = triggerObjectId;
        }

        private static DetectionRunContext manual() {
            return new DetectionRunContext(TRIGGER_MANUAL, null, null);
        }

        private static DetectionRunContext ingestRun(Long ingestRunId) {
            return new DetectionRunContext(TRIGGER_INGEST_RUN, OBJECT_INGEST_RUN, ingestRunId);
        }

        private boolean isIngestRun() {
            return TRIGGER_INGEST_RUN.equals(triggerType);
        }

        private String getTriggerType() {
            return triggerType;
        }

        private String getTriggerObjectType() {
            return triggerObjectType;
        }

        private Long getTriggerObjectId() {
            return triggerObjectId;
        }
    }

    private static class DetectionObject {

        private String objectType;
        private Long objectId;
        private String title;
        private String content;
        private String platform;
        private String riskLevel;
        private String keywords;
        private String authorName;
        private String sourceScope;

        private static DetectionObject fromIngestRecord(CampusIngestRecord record) {
            DetectionObject object = new DetectionObject();
            object.objectType = OBJECT_INGEST_RECORD;
            object.objectId = record.getRecordId();
            object.title = record.getTitle();
            object.content = record.getContent();
            object.platform = record.getPlatform();
            object.riskLevel = record.getRiskLevel();
            object.keywords = record.getKeywords();
            object.authorName = record.getAuthorName();
            object.sourceScope = joinScope(record.getPlatform(), record.getSourceId(), record.getTaskId(), record.getTargetType());
            return object;
        }

        private static DetectionObject fromClue(CampusClue clue) {
            DetectionObject object = new DetectionObject();
            object.objectType = OBJECT_CLUE;
            object.objectId = clue.getClueId();
            object.title = clue.getClueTitle();
            object.content = clue.getClueContent();
            object.platform = clue.getSourcePlatform();
            object.riskLevel = clue.getRiskLevel();
            object.keywords = clue.getKeywords();
            object.authorName = clue.getInvolvedAccount();
            object.sourceScope = joinScope(clue.getClueSource(), clue.getSourcePlatform(), clue.getInvolvedDepartmentId());
            return object;
        }

        private static DetectionObject fromAccountContent(CampusAccountContent content) {
            DetectionObject object = new DetectionObject();
            object.objectType = OBJECT_ACCOUNT_CONTENT;
            object.objectId = content.getContentId();
            object.title = content.getContentTitle();
            object.content = content.getContentText();
            object.platform = content.getPlatform();
            object.riskLevel = content.getRiskLevel();
            object.keywords = content.getKeywords();
            object.sourceScope = joinScope(content.getPlatform(), content.getAccountId(), content.getTaskId());
            return object;
        }

        private static String joinScope(Object... values) {
            StringBuilder builder = new StringBuilder();
            if (values == null) {
                return "";
            }
            for (Object value : values) {
                if (value != null && StringUtils.isNotBlank(String.valueOf(value))) {
                    if (builder.length() > 0) {
                        builder.append(',');
                    }
                    builder.append(value);
                }
            }
            return builder.toString();
        }
    }

    private static class DetectionMatch {

        private final boolean matched;
        private final Long ruleId;
        private final String matchedKeywords;
        private final String riskLevel;

        private DetectionMatch(boolean matched, Long ruleId, String matchedKeywords, String riskLevel) {
            this.matched = matched;
            this.ruleId = ruleId;
            this.matchedKeywords = matchedKeywords;
            this.riskLevel = riskLevel;
        }

        private static DetectionMatch of(Long ruleId, String matchedKeywords, String riskLevel) {
            return new DetectionMatch(true, ruleId, matchedKeywords, riskLevel);
        }

        private static DetectionMatch none() {
            return new DetectionMatch(false, null, null, RISK_NORMAL);
        }
    }

    private static class DetectionCounter {

        private int scannedCount;
        private int hitCount;
        private int alertCount;
    }
}
