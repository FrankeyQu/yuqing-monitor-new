package com.stonedt.intelligence.service.impl.campus;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.stonedt.intelligence.dao.campus.CampusDictDao;
import com.stonedt.intelligence.dao.campus.CampusEducationDao;
import com.stonedt.intelligence.dao.campus.CampusSchoolSubjectDao;
import com.stonedt.intelligence.entity.campus.CampusClue;
import com.stonedt.intelligence.entity.campus.CampusDictItem;
import com.stonedt.intelligence.entity.campus.CampusEducationBaiduTaskRequest;
import com.stonedt.intelligence.entity.campus.CampusIngestRunLog;
import com.stonedt.intelligence.entity.campus.CampusIngestTask;
import com.stonedt.intelligence.entity.campus.CampusSchoolSubject;
import com.stonedt.intelligence.service.campus.CampusEducationService;
import com.stonedt.intelligence.service.campus.CampusIngestService;
import com.stonedt.intelligence.util.SnowflakeUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class CampusEducationServiceImpl implements CampusEducationService {

    private static final String DICT_EDUCATION_NEWS = "campus_education_news_word";
    private static final String DICT_POLICY = "campus_policy_word";
    private static final String DICT_ADMISSION = "campus_admission_policy_word";
    private static final int DEFAULT_TOPIC_LIMIT = 20;
    private static final int DEFAULT_RANKING_LIMIT = 20;
    private static final int MAX_QUERY_CLUE_LIMIT = 5000;
    private static final String INGEST_TASK_ACTIVE = "active";
    private static final String INGEST_TASK_PAUSED = "paused";

    private final CampusSchoolSubjectDao campusSchoolSubjectDao;
    private final CampusEducationDao campusEducationDao;
    private final CampusDictDao campusDictDao;
    private final CampusIngestService campusIngestService;

    public CampusEducationServiceImpl(CampusSchoolSubjectDao campusSchoolSubjectDao,
                                      CampusEducationDao campusEducationDao,
                                      CampusDictDao campusDictDao,
                                      CampusIngestService campusIngestService) {
        this.campusSchoolSubjectDao = campusSchoolSubjectDao;
        this.campusEducationDao = campusEducationDao;
        this.campusDictDao = campusDictDao;
        this.campusIngestService = campusIngestService;
    }

    @Override
    public CampusSchoolSubject saveSchool(CampusSchoolSubject school, Long operatorUserId) {
        validateSchool(school);
        if (school.getSchoolId() == null) {
            school.setSchoolId(SnowflakeUtil.getId());
            school.setStatus(school.getStatus() == null ? 1 : school.getStatus());
            school.setDeleted(0);
            school.setCreateUserId(operatorUserId);
            school.setUpdateUserId(operatorUserId);
            campusSchoolSubjectDao.insert(school);
            return campusSchoolSubjectDao.selectBySchoolId(school.getSchoolId());
        }
        requireSchool(school.getSchoolId());
        school.setUpdateUserId(operatorUserId);
        campusSchoolSubjectDao.update(school);
        return campusSchoolSubjectDao.selectBySchoolId(school.getSchoolId());
    }

    @Override
    public void deleteSchool(Long schoolId, Long operatorUserId) {
        requireSchool(schoolId);
        campusSchoolSubjectDao.logicalDelete(schoolId, operatorUserId);
    }

    @Override
    public PageInfo<CampusSchoolSubject> listSchools(Integer pageNum,
                                                     Integer pageSize,
                                                     String keyword,
                                                     String region,
                                                     String educationStage,
                                                     Integer status) {
        PageHelper.startPage(defaultPageNum(pageNum), defaultPageSize(pageSize));
        return new PageInfo<>(campusSchoolSubjectDao.list(keyword, region, educationStage, status));
    }

    @Override
    public List<Map<String, Object>> listTopics(String topicType, Date startTime, Date endTime, Integer limit) {
        List<String> keywords = topicKeywords(topicType);
        int safeLimit = safeLimit(limit, DEFAULT_TOPIC_LIMIT, 100);
        List<CampusClue> clues = campusEducationDao.listTopicClues(keywords, startTime, endTime, safeLimit);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (CampusClue clue : clues) {
            Map<String, Object> row = new HashMap<>();
            row.put("clueId", clue.getClueId());
            row.put("title", clue.getClueTitle());
            row.put("content", clue.getClueContent());
            row.put("topicType", StringUtils.defaultIfBlank(topicType, "education_news"));
            row.put("sourcePlatform", clue.getSourcePlatform());
            row.put("originalUrl", clue.getOriginalUrl());
            row.put("publishTime", clue.getPublishTime());
            row.put("discoverTime", clue.getDiscoverTime());
            row.put("riskLevel", clue.getRiskLevel());
            row.put("sentiment", clue.getSentiment());
            row.put("keywords", clue.getKeywords());
            rows.add(row);
        }
        return rows;
    }

    @Override
    public List<Map<String, Object>> schoolSentimentRanking(String keyword, Date startTime, Date endTime, Integer limit) {
        List<CampusSchoolSubject> schools = campusSchoolSubjectDao.listActive();
        List<CampusClue> clues = campusEducationDao.listRankingClues(keyword, startTime, endTime, MAX_QUERY_CLUE_LIMIT);
        List<Map<String, Object>> ranking = new ArrayList<>();
        for (CampusSchoolSubject school : schools) {
            SchoolCounter counter = new SchoolCounter();
            Set<String> tokens = schoolTokens(school);
            if (tokens.isEmpty()) {
                continue;
            }
            for (CampusClue clue : clues) {
                String text = joinText(clue.getClueTitle(), clue.getClueContent(), clue.getKeywords(), clue.getInvolvedAccount());
                if (!containsAny(text, tokens)) {
                    continue;
                }
                counter.total++;
                String sentiment = StringUtils.defaultString(clue.getSentiment()).toLowerCase();
                if (sentiment.contains("negative") || sentiment.contains("负")) {
                    counter.negative++;
                } else if (sentiment.contains("positive") || sentiment.contains("正")) {
                    counter.positive++;
                } else {
                    counter.neutral++;
                }
                if ("major".equals(clue.getRiskLevel()) || "urgent".equals(clue.getRiskLevel())) {
                    counter.highRisk++;
                }
            }
            if (counter.total == 0) {
                continue;
            }
            Map<String, Object> row = new HashMap<>();
            row.put("schoolId", school.getSchoolId());
            row.put("schoolName", school.getSchoolName());
            row.put("region", school.getRegion());
            row.put("educationStage", school.getEducationStage());
            row.put("totalCount", counter.total);
            row.put("positiveCount", counter.positive);
            row.put("neutralCount", counter.neutral);
            row.put("negativeCount", counter.negative);
            row.put("highRiskCount", counter.highRisk);
            row.put("negativeRatio", counter.total == 0 ? 0 : counter.negative * 1.0 / counter.total);
            ranking.add(row);
        }
        ranking.sort(Comparator
                .comparing((Map<String, Object> row) -> ((Number) row.get("negativeCount")).intValue()).reversed()
                .thenComparing(row -> ((Number) row.get("totalCount")).intValue(), Comparator.reverseOrder()));
        int safeLimit = safeLimit(limit, DEFAULT_RANKING_LIMIT, 100);
        return ranking.size() <= safeLimit ? ranking : new ArrayList<>(ranking.subList(0, safeLimit));
    }

    @Override
    public CampusIngestTask createBaiduTask(CampusEducationBaiduTaskRequest request, Long operatorUserId) {
        return campusIngestService.saveTask(buildBaiduTask(request, INGEST_TASK_PAUSED), operatorUserId);
    }

    @Override
    public Map<String, Object> createAndRunBaiduTask(CampusEducationBaiduTaskRequest request, Long operatorUserId) {
        CampusIngestTask savedTask = campusIngestService.saveTask(buildBaiduTask(request, INGEST_TASK_ACTIVE), operatorUserId);
        CampusIngestRunLog runLog;
        try {
            runLog = campusIngestService.runTask(savedTask.getTaskId(), operatorUserId);
        } finally {
            savedTask = campusIngestService.updateTaskStatus(savedTask.getTaskId(), INGEST_TASK_PAUSED, operatorUserId);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("task", savedTask);
        result.put("runLog", runLog);
        return result;
    }

    @Override
    public Map<String, Integer> importSchools(String csvContent, Long operatorUserId) {
        if (StringUtils.isBlank(csvContent)) {
            throw new IllegalArgumentException("导入文件不能为空");
        }
        Map<String, Integer> result = new HashMap<>();
        result.put("inserted", 0);
        result.put("updated", 0);
        result.put("skipped", 0);
        result.put("failed", 0);
        String normalized = csvContent.replace("\uFEFF", "");
        String[] lines = normalized.split("\\r?\\n");
        for (int i = 0; i < lines.length; i++) {
            String line = StringUtils.trimToEmpty(lines[i]);
            if (StringUtils.isBlank(line)) {
                increment(result, "skipped");
                continue;
            }
            List<String> columns = parseCsvLine(line);
            if (i == 0 && isImportHeader(columns)) {
                continue;
            }
            try {
                CampusSchoolSubject school = schoolFromColumns(columns);
                if (StringUtils.isBlank(school.getSchoolName())) {
                    increment(result, "skipped");
                    continue;
                }
                CampusSchoolSubject old = school.getSchoolId() == null
                        ? campusSchoolSubjectDao.selectBySchoolName(school.getSchoolName())
                        : campusSchoolSubjectDao.selectBySchoolId(school.getSchoolId());
                if (old == null && school.getSchoolId() != null) {
                    old = campusSchoolSubjectDao.selectBySchoolName(school.getSchoolName());
                }
                if (old == null) {
                    saveSchool(school, operatorUserId);
                    increment(result, "inserted");
                } else {
                    school.setSchoolId(old.getSchoolId());
                    saveSchool(school, operatorUserId);
                    increment(result, "updated");
                }
            } catch (RuntimeException ex) {
                increment(result, "failed");
            }
        }
        return result;
    }

    @Override
    public String schoolImportTemplate() {
        return "\uFEFFschoolId,schoolName,schoolAliases,region,educationStage,schoolType,status,remark\n"
                + ",乌鲁木齐市第一中学,\"乌市一中,乌鲁木齐一中\",新疆,高中,公办,1,示例数据\n";
    }

    private CampusIngestTask buildBaiduTask(CampusEducationBaiduTaskRequest request, String taskStatus) {
        if (request == null || request.getSourceId() == null) {
            throw new IllegalArgumentException("百度接入任务必须指定来源ID");
        }
        String query = buildBaiduQuery(request);
        CampusIngestTask task = new CampusIngestTask();
        task.setSourceId(request.getSourceId());
        task.setTaskName(StringUtils.defaultIfBlank(request.getTaskName(), "教育专题百度搜索-" + query));
        task.setTargetType("clue");
        task.setAdapterType("baidu_search");
        task.setScheduleEnabled(0);
        task.setTaskStatus(taskStatus);
        task.setAuthorizationScope(StringUtils.defaultIfBlank(request.getAuthorizationScope(), "教育专题公开搜索"));
        task.setRetentionDays(180);
        Map<String, Object> fetchConfig = new HashMap<>();
        fetchConfig.put("provider", "baidu");
        fetchConfig.put("query", query);
        fetchConfig.put("resourceTypes", "web");
        fetchConfig.put("topK", request.getTopK() == null ? 20 : Math.min(Math.max(request.getTopK(), 1), 50));
        fetchConfig.put("credentialRef", StringUtils.defaultIfBlank(request.getCredentialRef(), "BAIDU_API_KEY"));
        task.setFetchConfig(JSON.toJSONString(fetchConfig));
        return task;
    }

    private CampusSchoolSubject schoolFromColumns(List<String> columns) {
        CampusSchoolSubject school = new CampusSchoolSubject();
        school.setSchoolId(parseLong(valueAt(columns, 0)));
        school.setSchoolName(valueAt(columns, 1));
        school.setSchoolAliases(valueAt(columns, 2));
        school.setRegion(StringUtils.defaultIfBlank(valueAt(columns, 3), "新疆"));
        school.setEducationStage(valueAt(columns, 4));
        school.setSchoolType(valueAt(columns, 5));
        school.setStatus(parseStatus(valueAt(columns, 6)));
        school.setRemark(valueAt(columns, 7));
        return school;
    }

    private List<String> parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                if (quoted && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    quoted = !quoted;
                }
                continue;
            }
            if (ch == ',' && !quoted) {
                values.add(StringUtils.trimToEmpty(current.toString()));
                current.setLength(0);
                continue;
            }
            current.append(ch);
        }
        values.add(StringUtils.trimToEmpty(current.toString()));
        return values;
    }

    private boolean isImportHeader(List<String> columns) {
        if (columns == null || columns.isEmpty()) {
            return false;
        }
        String joined = StringUtils.join(columns, ",").toLowerCase();
        return joined.contains("schoolname") || joined.contains("学校名称");
    }

    private String valueAt(List<String> values, int index) {
        if (values == null || index < 0 || index >= values.size()) {
            return null;
        }
        return StringUtils.trimToNull(values.get(index));
    }

    private Long parseLong(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Integer parseStatus(String value) {
        if (StringUtils.isBlank(value)) {
            return 1;
        }
        if ("停用".equals(value) || "disabled".equalsIgnoreCase(value)) {
            return 0;
        }
        if ("启用".equals(value) || "active".equalsIgnoreCase(value)) {
            return 1;
        }
        try {
            return Integer.valueOf(value) == 0 ? 0 : 1;
        } catch (NumberFormatException ex) {
            return 1;
        }
    }

    private void increment(Map<String, Integer> result, String key) {
        result.put(key, result.get(key) == null ? 1 : result.get(key) + 1);
    }

    private void validateSchool(CampusSchoolSubject school) {
        if (school == null) {
            throw new IllegalArgumentException("学校主体不能为空");
        }
        if (StringUtils.isBlank(school.getSchoolName())) {
            throw new IllegalArgumentException("学校名称不能为空");
        }
    }

    private CampusSchoolSubject requireSchool(Long schoolId) {
        if (schoolId == null) {
            throw new IllegalArgumentException("学校ID不能为空");
        }
        CampusSchoolSubject school = campusSchoolSubjectDao.selectBySchoolId(schoolId);
        if (school == null) {
            throw new IllegalArgumentException("学校主体不存在");
        }
        return school;
    }

    private List<String> topicKeywords(String topicType) {
        String dictType;
        if ("policy".equals(topicType)) {
            dictType = DICT_POLICY;
        } else if ("admission".equals(topicType)) {
            dictType = DICT_ADMISSION;
        } else {
            dictType = DICT_EDUCATION_NEWS;
        }
        Set<String> keywords = new LinkedHashSet<>();
        List<CampusDictItem> items = campusDictDao.enabledItems(dictType);
        if (items != null) {
            for (CampusDictItem item : items) {
                keywords.add(StringUtils.defaultIfBlank(item.getItemValue(), item.getItemName()));
            }
        }
        if (keywords.isEmpty()) {
            if ("policy".equals(topicType)) {
                keywords.add("教育政策");
                keywords.add("通知");
            } else if ("admission".equals(topicType)) {
                keywords.add("招生");
                keywords.add("报名");
            } else {
                keywords.add("教育");
                keywords.add("学校");
            }
        }
        return new ArrayList<>(keywords);
    }

    private String buildBaiduQuery(CampusEducationBaiduTaskRequest request) {
        List<String> parts = new ArrayList<>();
        parts.add(StringUtils.defaultIfBlank(request.getRegion(), "新疆"));
        if (StringUtils.isNotBlank(request.getSchoolName())) {
            parts.add(request.getSchoolName());
        }
        if ("policy".equals(request.getTopicType())) {
            parts.add("教育 政策 通知");
        } else if ("admission".equals(request.getTopicType())) {
            parts.add("招生 报名 录取 学区");
        } else {
            parts.add("教育 学校 重点新闻");
        }
        if (StringUtils.isNotBlank(request.getKeyword())) {
            parts.add(request.getKeyword());
        }
        return StringUtils.join(parts, " ");
    }

    private Set<String> schoolTokens(CampusSchoolSubject school) {
        Set<String> tokens = splitTokens(school.getSchoolName());
        tokens.addAll(splitTokens(school.getSchoolAliases()));
        return tokens;
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

    private boolean containsAny(String text, Set<String> tokens) {
        String safeText = StringUtils.defaultString(text).toLowerCase();
        for (String token : tokens) {
            if (StringUtils.isNotBlank(token) && safeText.contains(token.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private String joinText(String... values) {
        StringBuilder builder = new StringBuilder();
        for (String value : values) {
            if (StringUtils.isNotBlank(value)) {
                builder.append(value).append('\n');
            }
        }
        return builder.toString();
    }

    private int safeLimit(Integer limit, int defaultLimit, int maxLimit) {
        if (limit == null || limit < 1) {
            return defaultLimit;
        }
        return Math.min(limit, maxLimit);
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

    private static class SchoolCounter {
        private int total;
        private int positive;
        private int neutral;
        private int negative;
        private int highRisk;
    }
}
