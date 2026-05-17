package com.stonedt.intelligence.service.impl.campus;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.stonedt.intelligence.dao.campus.CampusAccountContentDao;
import com.stonedt.intelligence.dao.campus.CampusAnalysisResultDao;
import com.stonedt.intelligence.dao.campus.CampusAnalysisTaskDao;
import com.stonedt.intelligence.dao.campus.CampusClueDao;
import com.stonedt.intelligence.dao.campus.CampusEventDao;
import com.stonedt.intelligence.dao.campus.CampusSensitiveWordDao;
import com.stonedt.intelligence.entity.campus.CampusAccountContent;
import com.stonedt.intelligence.entity.campus.CampusAnalysisResult;
import com.stonedt.intelligence.entity.campus.CampusAnalysisTask;
import com.stonedt.intelligence.entity.campus.CampusClue;
import com.stonedt.intelligence.entity.campus.CampusEvent;
import com.stonedt.intelligence.entity.campus.CampusSensitiveWord;
import com.stonedt.intelligence.service.campus.CampusAnalysisService;
import com.stonedt.intelligence.util.SnowflakeUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class CampusAnalysisServiceImpl implements CampusAnalysisService {

    private static final String STATUS_PENDING = "pending";
    private static final String STATUS_RUNNING = "running";
    private static final String STATUS_COMPLETED = "completed";
    private static final String STATUS_FAILED = "failed";
    private static final String MODEL_PROVIDER = "local_heuristic";
    private static final String MODEL_NAME = "local_heuristic_v1";
    private static final String ASSISTIVE_LABEL = "仅供辅助研判";

    private final CampusAnalysisTaskDao campusAnalysisTaskDao;
    private final CampusAnalysisResultDao campusAnalysisResultDao;
    private final CampusClueDao campusClueDao;
    private final CampusEventDao campusEventDao;
    private final CampusAccountContentDao campusAccountContentDao;
    private final CampusSensitiveWordDao campusSensitiveWordDao;

    public CampusAnalysisServiceImpl(CampusAnalysisTaskDao campusAnalysisTaskDao,
                                     CampusAnalysisResultDao campusAnalysisResultDao,
                                     CampusClueDao campusClueDao,
                                     CampusEventDao campusEventDao,
                                     CampusAccountContentDao campusAccountContentDao,
                                     CampusSensitiveWordDao campusSensitiveWordDao) {
        this.campusAnalysisTaskDao = campusAnalysisTaskDao;
        this.campusAnalysisResultDao = campusAnalysisResultDao;
        this.campusClueDao = campusClueDao;
        this.campusEventDao = campusEventDao;
        this.campusAccountContentDao = campusAccountContentDao;
        this.campusSensitiveWordDao = campusSensitiveWordDao;
    }

    @Override
    public CampusAnalysisTask createTask(CampusAnalysisTask task, Long operatorUserId) {
        validateTask(task);
        SourceText sourceText = loadSourceText(task.getObjectType(), task.getObjectId());
        task.setAnalysisTaskId(SnowflakeUtil.getId());
        task.setTaskStatus(STATUS_PENDING);
        task.setModelProvider(StringUtils.defaultIfBlank(task.getModelProvider(), MODEL_PROVIDER));
        task.setModelName(StringUtils.defaultIfBlank(task.getModelName(), MODEL_NAME));
        task.setRequestPayload(StringUtils.defaultIfBlank(task.getRequestPayload(), sourceText.title));
        task.setDeleted(0);
        task.setCreateUserId(operatorUserId);
        task.setUpdateUserId(operatorUserId);
        campusAnalysisTaskDao.insert(task);
        return campusAnalysisTaskDao.selectByTaskId(task.getAnalysisTaskId());
    }

    @Override
    public PageInfo<CampusAnalysisTask> listTasks(Integer pageNum,
                                                  Integer pageSize,
                                                  String objectType,
                                                  Long objectId,
                                                  String analysisType,
                                                  String taskStatus) {
        PageHelper.startPage(defaultPageNum(pageNum), defaultPageSize(pageSize));
        return new PageInfo<>(campusAnalysisTaskDao.list(objectType, objectId, analysisType, taskStatus));
    }

    @Override
    public CampusAnalysisResult runTask(Long analysisTaskId, Long operatorUserId) {
        CampusAnalysisTask task = requireTask(analysisTaskId);
        campusAnalysisTaskDao.updateStatus(analysisTaskId, STATUS_RUNNING, null, operatorUserId);
        try {
            SourceText sourceText = loadSourceText(task.getObjectType(), task.getObjectId());
            CampusAnalysisResult result = analyze(task, sourceText, operatorUserId);
            campusAnalysisResultDao.insert(result);
            campusAnalysisTaskDao.updateStatus(analysisTaskId, STATUS_COMPLETED, null, operatorUserId);
            return campusAnalysisResultDao.selectByResultId(result.getAnalysisResultId());
        } catch (Exception e) {
            campusAnalysisTaskDao.updateStatus(analysisTaskId, STATUS_FAILED, e.getMessage(), operatorUserId);
            throw asRuntimeException(e);
        }
    }

    @Override
    public PageInfo<CampusAnalysisResult> listResults(Integer pageNum,
                                                      Integer pageSize,
                                                      Long analysisTaskId,
                                                      String objectType,
                                                      Long objectId,
                                                      String analysisType,
                                                      String adoptionStatus) {
        PageHelper.startPage(defaultPageNum(pageNum), defaultPageSize(pageSize));
        return new PageInfo<>(campusAnalysisResultDao.list(analysisTaskId, objectType,
                objectId, analysisType, adoptionStatus));
    }

    @Override
    public CampusAnalysisResult reviewResult(Long analysisResultId,
                                             String adoptionStatus,
                                             String reviewOpinion,
                                             Long operatorUserId) {
        requireResult(analysisResultId);
        if (!"adopted".equals(adoptionStatus) && !"rejected".equals(adoptionStatus)) {
            throw new IllegalArgumentException("采纳状态只能为 adopted 或 rejected");
        }
        campusAnalysisResultDao.review(analysisResultId, adoptionStatus, reviewOpinion,
                operatorUserId, operatorUserId);
        return campusAnalysisResultDao.selectByResultId(analysisResultId);
    }

    private CampusAnalysisResult analyze(CampusAnalysisTask task, SourceText sourceText, Long operatorUserId) {
        String text = joinText(sourceText.title, sourceText.content, sourceText.keywords);
        Set<String> matchedKeywords = extractKeywords(text);
        String sentiment = detectSentiment(text);
        String suggestedRiskLevel = suggestRisk(text, matchedKeywords);
        String summary = summarize(text);
        JSONObject payload = new JSONObject();
        payload.put("sourceTitle", sourceText.title);
        payload.put("sentiment", sentiment);
        payload.put("suggestedRiskLevel", suggestedRiskLevel);
        payload.put("keywords", StringUtils.join(matchedKeywords, ","));
        payload.put("summary", summary);
        payload.put("assistiveLabel", ASSISTIVE_LABEL);

        CampusAnalysisResult result = new CampusAnalysisResult();
        result.setAnalysisResultId(SnowflakeUtil.getId());
        result.setAnalysisTaskId(task.getAnalysisTaskId());
        result.setObjectType(task.getObjectType());
        result.setObjectId(task.getObjectId());
        result.setAnalysisType(task.getAnalysisType());
        result.setSentiment(sentiment);
        result.setSuggestedRiskLevel(suggestedRiskLevel);
        result.setSummary(summary);
        result.setKeywords(StringUtils.join(matchedKeywords, ","));
        result.setConfidence(new BigDecimal("0.65"));
        result.setResultPayload(payload.toJSONString());
        result.setAssistiveLabel(ASSISTIVE_LABEL);
        result.setAdoptionStatus(STATUS_PENDING);
        result.setDeleted(0);
        result.setCreateUserId(operatorUserId);
        result.setUpdateUserId(operatorUserId);
        return result;
    }

    private SourceText loadSourceText(String objectType, Long objectId) {
        if ("clue".equals(objectType)) {
            CampusClue clue = campusClueDao.selectByClueId(objectId);
            if (clue == null) {
                throw new IllegalArgumentException("线索不存在");
            }
            return new SourceText(clue.getClueTitle(), clue.getClueContent(), clue.getKeywords());
        }
        if ("event".equals(objectType)) {
            CampusEvent event = campusEventDao.selectByEventId(objectId);
            if (event == null) {
                throw new IllegalArgumentException("事件不存在");
            }
            return new SourceText(event.getEventTitle(), event.getEventSummary(), event.getRiskLevel());
        }
        if ("account_content".equals(objectType)) {
            CampusAccountContent content = campusAccountContentDao.selectByContentId(objectId);
            if (content == null) {
                throw new IllegalArgumentException("账号动态不存在");
            }
            return new SourceText(content.getContentTitle(), content.getContentText(), content.getKeywords());
        }
        throw new IllegalArgumentException("暂不支持的分析对象类型");
    }

    private Set<String> extractKeywords(String text) {
        Set<String> keywords = new LinkedHashSet<>();
        List<CampusSensitiveWord> words = campusSensitiveWordDao.listEnabled();
        for (CampusSensitiveWord word : words) {
            if (word != null && StringUtils.isNotBlank(word.getWordText())
                    && StringUtils.defaultString(text).contains(word.getWordText())) {
                keywords.add(word.getWordText());
            }
        }
        for (String token : new String[]{"学生", "教师", "食堂", "宿舍", "安全", "考试", "处分", "聚集", "投诉", "网络"}) {
            if (StringUtils.defaultString(text).contains(token)) {
                keywords.add(token);
            }
        }
        return keywords;
    }

    private String detectSentiment(String text) {
        String value = StringUtils.defaultString(text);
        if (containsAny(value, "感谢", "表扬", "满意", "澄清", "解决")) {
            return "positive";
        }
        if (containsAny(value, "投诉", "举报", "不满", "冲突", "聚集", "伤害", "事故", "舆情", "曝光")) {
            return "negative";
        }
        return "neutral";
    }

    private String suggestRisk(String text, Set<String> matchedKeywords) {
        String value = StringUtils.defaultString(text);
        if (containsAny(value, "伤亡", "自伤", "失联", "群体", "聚集", "极端", "爆炸", "重大事故")) {
            return "urgent";
        }
        if (containsAny(value, "食品安全", "校园安全", "网暴", "欺凌", "举报", "冲突")) {
            return "major";
        }
        if (!matchedKeywords.isEmpty() || containsAny(value, "投诉", "不满", "舆情", "曝光")) {
            return "concern";
        }
        return "normal";
    }

    private boolean containsAny(String text, String... tokens) {
        for (String token : tokens) {
            if (text.contains(token)) {
                return true;
            }
        }
        return false;
    }

    private String summarize(String text) {
        String value = StringUtils.defaultString(text).replaceAll("\\s+", " ").trim();
        if (value.length() <= 300) {
            return value;
        }
        return value.substring(0, 300);
    }

    private void validateTask(CampusAnalysisTask task) {
        if (task == null) {
            throw new IllegalArgumentException("分析任务不能为空");
        }
        if (StringUtils.isBlank(task.getObjectType())) {
            throw new IllegalArgumentException("分析对象类型不能为空");
        }
        if (task.getObjectId() == null) {
            throw new IllegalArgumentException("分析对象ID不能为空");
        }
        if (StringUtils.isBlank(task.getAnalysisType())) {
            throw new IllegalArgumentException("分析类型不能为空");
        }
    }

    private CampusAnalysisTask requireTask(Long analysisTaskId) {
        if (analysisTaskId == null) {
            throw new IllegalArgumentException("分析任务ID不能为空");
        }
        CampusAnalysisTask task = campusAnalysisTaskDao.selectByTaskId(analysisTaskId);
        if (task == null) {
            throw new IllegalArgumentException("分析任务不存在");
        }
        return task;
    }

    private CampusAnalysisResult requireResult(Long analysisResultId) {
        if (analysisResultId == null) {
            throw new IllegalArgumentException("分析结果ID不能为空");
        }
        CampusAnalysisResult result = campusAnalysisResultDao.selectByResultId(analysisResultId);
        if (result == null) {
            throw new IllegalArgumentException("分析结果不存在");
        }
        return result;
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

    private static class SourceText {

        private final String title;
        private final String content;
        private final String keywords;

        private SourceText(String title, String content, String keywords) {
            this.title = title;
            this.content = content;
            this.keywords = keywords;
        }
    }
}
