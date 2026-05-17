package com.stonedt.intelligence.service.impl.campus;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.stonedt.intelligence.dao.campus.CampusAccountContentDao;
import com.stonedt.intelligence.dao.campus.CampusAlertDao;
import com.stonedt.intelligence.dao.campus.CampusAlertRuleDao;
import com.stonedt.intelligence.dao.campus.CampusClueDao;
import com.stonedt.intelligence.dao.campus.CampusSensitiveWordDao;
import com.stonedt.intelligence.entity.campus.CampusAccountContent;
import com.stonedt.intelligence.entity.campus.CampusAlert;
import com.stonedt.intelligence.entity.campus.CampusAlertRule;
import com.stonedt.intelligence.entity.campus.CampusClue;
import com.stonedt.intelligence.entity.campus.CampusSensitiveWord;
import com.stonedt.intelligence.service.campus.CampusAlertService;
import com.stonedt.intelligence.service.campus.support.CampusRiskLevel;
import com.stonedt.intelligence.util.SnowflakeUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@Service
public class CampusAlertServiceImpl implements CampusAlertService {

    private static final String STATUS_PENDING = "pending";
    private static final String STATUS_HANDLED = "handled";
    private static final String STATUS_IGNORED = "ignored";
    private static final String RISK_NORMAL = CampusRiskLevel.normalCode();
    private static final String RISK_CONCERN = CampusRiskLevel.concernCode();
    private static final String MATCH_CONTAINS = "contains";
    private static final String MATCH_EXACT = "exact";
    private static final String MATCH_REGEX = "regex";

    private final CampusSensitiveWordDao campusSensitiveWordDao;
    private final CampusAlertRuleDao campusAlertRuleDao;
    private final CampusAlertDao campusAlertDao;
    private final CampusClueDao campusClueDao;
    private final CampusAccountContentDao campusAccountContentDao;

    public CampusAlertServiceImpl(CampusSensitiveWordDao campusSensitiveWordDao,
                                  CampusAlertRuleDao campusAlertRuleDao,
                                  CampusAlertDao campusAlertDao,
                                  CampusClueDao campusClueDao,
                                  CampusAccountContentDao campusAccountContentDao) {
        this.campusSensitiveWordDao = campusSensitiveWordDao;
        this.campusAlertRuleDao = campusAlertRuleDao;
        this.campusAlertDao = campusAlertDao;
        this.campusClueDao = campusClueDao;
        this.campusAccountContentDao = campusAccountContentDao;
    }

    @Override
    public CampusSensitiveWord saveSensitiveWord(CampusSensitiveWord word, Long operatorUserId) {
        validateSensitiveWord(word);
        if (word.getWordId() == null) {
            word.setWordId(SnowflakeUtil.getId());
            word.setCreateUserId(operatorUserId);
            word.setUpdateUserId(operatorUserId);
            setSensitiveWordDefaults(word);
            campusSensitiveWordDao.insert(word);
            return campusSensitiveWordDao.selectByWordId(word.getWordId());
        }
        requireSensitiveWord(word.getWordId());
        word.setUpdateUserId(operatorUserId);
        normalizeSensitiveWordRisk(word);
        campusSensitiveWordDao.update(word);
        return campusSensitiveWordDao.selectByWordId(word.getWordId());
    }

    @Override
    public void deleteSensitiveWord(Long wordId, Long operatorUserId) {
        requireSensitiveWord(wordId);
        campusSensitiveWordDao.logicalDelete(wordId, operatorUserId);
    }

    @Override
    public PageInfo<CampusSensitiveWord> listSensitiveWords(Integer pageNum,
                                                           Integer pageSize,
                                                           String keyword,
                                                           String wordCategory,
                                                           String riskLevel,
                                                           Integer status) {
        PageHelper.startPage(defaultPageNum(pageNum), defaultPageSize(pageSize));
        return new PageInfo<>(campusSensitiveWordDao.list(keyword, wordCategory,
                CampusRiskLevel.normalizeForQuery(riskLevel), status));
    }

    @Override
    public CampusAlertRule saveRule(CampusAlertRule rule, Long operatorUserId) {
        validateRule(rule);
        if (rule.getRuleId() == null) {
            rule.setRuleId(SnowflakeUtil.getId());
            rule.setCreateUserId(operatorUserId);
            rule.setUpdateUserId(operatorUserId);
            setRuleDefaults(rule);
            campusAlertRuleDao.insert(rule);
            return campusAlertRuleDao.selectByRuleId(rule.getRuleId());
        }
        requireRule(rule.getRuleId());
        rule.setUpdateUserId(operatorUserId);
        normalizeRuleRisk(rule);
        campusAlertRuleDao.update(rule);
        return campusAlertRuleDao.selectByRuleId(rule.getRuleId());
    }

    @Override
    public void deleteRule(Long ruleId, Long operatorUserId) {
        requireRule(ruleId);
        campusAlertRuleDao.logicalDelete(ruleId, operatorUserId);
    }

    @Override
    public PageInfo<CampusAlertRule> listRules(Integer pageNum,
                                               Integer pageSize,
                                               String keyword,
                                               String ruleType,
                                               Integer enabled) {
        PageHelper.startPage(defaultPageNum(pageNum), defaultPageSize(pageSize));
        return new PageInfo<>(campusAlertRuleDao.list(keyword, ruleType, enabled));
    }

    @Override
    public CampusAlert createAlert(CampusAlert alert, Long operatorUserId) {
        validateAlert(alert);
        alert.setAlertId(SnowflakeUtil.getId());
        alert.setCreateUserId(operatorUserId);
        alert.setUpdateUserId(operatorUserId);
        setAlertDefaults(alert);
        campusAlertDao.insert(alert);
        return campusAlertDao.selectByAlertId(alert.getAlertId());
    }

    @Override
    public CampusAlert handleAlert(Long alertId, String alertStatus, String handleOpinion, Long operatorUserId) {
        if (StringUtils.isBlank(alertStatus)) {
            throw new IllegalArgumentException("预警处理状态不能为空");
        }
        if (!STATUS_HANDLED.equals(alertStatus) && !STATUS_IGNORED.equals(alertStatus)) {
            throw new IllegalArgumentException("预警处理状态不合法");
        }
        requireAlert(alertId);
        campusAlertDao.handle(alertId, alertStatus, handleOpinion, operatorUserId, operatorUserId);
        return campusAlertDao.selectByAlertId(alertId);
    }

    @Override
    public PageInfo<CampusAlert> listAlerts(Integer pageNum,
                                            Integer pageSize,
                                            String keyword,
                                            String alertSource,
                                            String riskLevel,
                                            String alertStatus) {
        PageHelper.startPage(defaultPageNum(pageNum), defaultPageSize(pageSize));
        return new PageInfo<>(campusAlertDao.list(keyword, alertSource,
                CampusRiskLevel.normalizeForQuery(riskLevel), alertStatus));
    }

    @Override
    public List<CampusAlert> evaluateClue(Long clueId, Long operatorUserId) {
        if (clueId == null) {
            throw new IllegalArgumentException("线索ID不能为空");
        }
        CampusClue clue = campusClueDao.selectByClueId(clueId);
        if (clue == null) {
            throw new IllegalArgumentException("线索不存在");
        }
        List<CampusAlert> alerts = evaluateText("clue", clueId, clue.getClueTitle(),
                joinText(clue.getClueContent(), clue.getKeywords(), clue.getInvolvedAccount()), operatorUserId);
        appendRuleAlerts(alerts, "clue", clueId, clue.getClueTitle(), clue.getClueContent(),
                clue.getRiskLevel(), operatorUserId);
        return alerts;
    }

    @Override
    public List<CampusAlert> evaluateAccountContent(Long contentId, Long operatorUserId) {
        if (contentId == null) {
            throw new IllegalArgumentException("账号内容ID不能为空");
        }
        CampusAccountContent content = campusAccountContentDao.selectByContentId(contentId);
        if (content == null) {
            throw new IllegalArgumentException("账号内容不存在");
        }
        List<CampusAlert> alerts = evaluateText("account_content", contentId, content.getContentTitle(),
                joinText(content.getContentText(), content.getKeywords()), operatorUserId);
        appendRuleAlerts(alerts, "account_content", contentId, content.getContentTitle(),
                content.getContentText(), content.getRiskLevel(), operatorUserId);
        return alerts;
    }

    @Override
    public List<CampusAlert> evaluateText(String alertSource,
                                          Long sourceObjectId,
                                          String title,
                                          String content,
                                          Long operatorUserId) {
        if (StringUtils.isBlank(alertSource)) {
            throw new IllegalArgumentException("预警来源不能为空");
        }
        List<CampusAlert> alerts = new ArrayList<>();
        List<CampusSensitiveWord> words = campusSensitiveWordDao.listEnabled();
        Set<String> matchedWords = new LinkedHashSet<>();
        String riskLevel = RISK_NORMAL;
        String text = joinText(title, content);
        for (CampusSensitiveWord word : words) {
            if (word == null || StringUtils.isBlank(word.getWordText())) {
                continue;
            }
            if (matches(text, word.getWordText(), word.getMatchType())) {
                matchedWords.add(word.getWordText());
                riskLevel = higherRisk(riskLevel, word.getRiskLevel());
            }
        }
        if (!matchedWords.isEmpty()) {
            CampusAlert alert = buildAlert("敏感词预警：" + defaultTitle(title),
                    summary(text, 1000), alertSource, sourceObjectId, null,
                    riskLevel, StringUtils.join(matchedWords, ","),
                    operatorUserId);
            CampusAlert saved = createIfAbsent(alert);
            if (saved != null) {
                alerts.add(saved);
            }
        }
        appendRuleAlerts(alerts, alertSource, sourceObjectId, title, content, null, operatorUserId);
        return alerts;
    }

    private void appendRuleAlerts(List<CampusAlert> alerts,
                                  String alertSource,
                                  Long sourceObjectId,
                                  String title,
                                  String content,
                                  String objectRiskLevel,
                                  Long operatorUserId) {
        List<CampusAlertRule> enabledRules = campusAlertRuleDao.list(null, null, 1);
        String text = joinText(title, content);
        for (CampusAlertRule rule : enabledRules) {
            if (rule == null || StringUtils.isBlank(rule.getRuleType())) {
                continue;
            }
            RuleMatch match = matchRule(rule, alertSource, text, objectRiskLevel);
            if (!match.matched) {
                continue;
            }
            CampusAlert alert = buildAlert("规则预警：" + rule.getRuleName(),
                    summary(text, 1000), alertSource, sourceObjectId, rule.getRuleId(),
                    StringUtils.defaultIfBlank(rule.getRiskLevel(), match.riskLevel),
                    match.matchedKeywords, operatorUserId);
            CampusAlert saved = createIfAbsent(alert);
            if (saved != null) {
                alerts.add(saved);
            }
        }
    }

    private RuleMatch matchRule(CampusAlertRule rule, String alertSource, String text, String objectRiskLevel) {
        String ruleType = rule.getRuleType();
        if ("clue_risk".equals(ruleType)) {
            if (!"clue".equals(alertSource)) {
                return RuleMatch.none();
            }
            Set<String> configuredRiskLevels = splitRiskTokens(rule.getRuleCondition());
            String normalizedObjectRiskLevel = CampusRiskLevel.normalizeForQuery(objectRiskLevel);
            if (StringUtils.isBlank(normalizedObjectRiskLevel)) {
                return RuleMatch.none();
            }
            if (configuredRiskLevels.isEmpty()) {
                boolean matched = CampusRiskLevel.isNonNormal(normalizedObjectRiskLevel);
                return matched ? RuleMatch.of(normalizedObjectRiskLevel, normalizedObjectRiskLevel) : RuleMatch.none();
            }
            return configuredRiskLevels.contains(normalizedObjectRiskLevel)
                    ? RuleMatch.of(normalizedObjectRiskLevel, normalizedObjectRiskLevel)
                    : RuleMatch.none();
        }
        if ("account_content".equals(ruleType)) {
            if (!"account_content".equals(alertSource)) {
                return RuleMatch.none();
            }
            Set<String> tokens = splitTokens(rule.getRuleCondition());
            if (tokens.isEmpty()) {
                return RuleMatch.of("重点账号动态", StringUtils.defaultIfBlank(rule.getRiskLevel(), RISK_CONCERN));
            }
            Set<String> matched = matchTokens(text, tokens);
            return matched.isEmpty()
                    ? RuleMatch.none()
                    : RuleMatch.of(StringUtils.join(matched, ","), StringUtils.defaultIfBlank(rule.getRiskLevel(), RISK_CONCERN));
        }
        if ("keyword".equals(ruleType) || "sensitive_word".equals(ruleType)) {
            Set<String> tokens = splitTokens(rule.getRuleCondition());
            Set<String> matched = matchTokens(text, tokens);
            return matched.isEmpty()
                    ? RuleMatch.none()
                    : RuleMatch.of(StringUtils.join(matched, ","), StringUtils.defaultIfBlank(rule.getRiskLevel(), RISK_CONCERN));
        }
        return RuleMatch.none();
    }

    private CampusAlert createIfAbsent(CampusAlert alert) {
        int count = campusAlertDao.countExisting(alert.getAlertSource(), alert.getSourceObjectId(),
                alert.getRuleId(), alert.getMatchedKeywords());
        if (count > 0) {
            return null;
        }
        campusAlertDao.insert(alert);
        return campusAlertDao.selectByAlertId(alert.getAlertId());
    }

    private CampusAlert buildAlert(String title,
                                   String content,
                                   String alertSource,
                                   Long sourceObjectId,
                                   Long ruleId,
                                   String riskLevel,
                                   String matchedKeywords,
                                   Long operatorUserId) {
        CampusAlert alert = new CampusAlert();
        alert.setAlertId(SnowflakeUtil.getId());
        alert.setAlertTitle(summary(title, 255));
        alert.setAlertContent(content);
        alert.setAlertSource(alertSource);
        alert.setSourceObjectId(sourceObjectId);
        alert.setRuleId(ruleId);
        alert.setRiskLevel(CampusRiskLevel.normalizeOrDefault(riskLevel));
        alert.setMatchedKeywords(summary(matchedKeywords, 512));
        alert.setEvidenceJson(summary(buildEvidenceJson(alertSource, sourceObjectId, ruleId,
                riskLevel, matchedKeywords, content), 4000));
        alert.setAlertStatus(STATUS_PENDING);
        alert.setDeleted(0);
        alert.setCreateUserId(operatorUserId);
        alert.setUpdateUserId(operatorUserId);
        return alert;
    }

    private boolean matches(String text, String pattern, String matchType) {
        if (StringUtils.isBlank(text) || StringUtils.isBlank(pattern)) {
            return false;
        }
        String resolvedMatchType = StringUtils.defaultIfBlank(matchType, MATCH_CONTAINS);
        if (MATCH_EXACT.equals(resolvedMatchType)) {
            return text.equals(pattern);
        }
        if (MATCH_REGEX.equals(resolvedMatchType)) {
            try {
                return Pattern.compile(pattern).matcher(text).find();
            } catch (PatternSyntaxException e) {
                return false;
            }
        }
        return text.contains(pattern);
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

    private Set<String> matchTokens(String text, Set<String> tokens) {
        Set<String> matched = new LinkedHashSet<>();
        for (String token : tokens) {
            if (StringUtils.isNotBlank(token) && StringUtils.defaultString(text).contains(token)) {
                matched.add(token);
            }
        }
        return matched;
    }

    private String higherRisk(String left, String right) {
        return CampusRiskLevel.higher(left, right);
    }

    private void validateSensitiveWord(CampusSensitiveWord word) {
        if (word == null) {
            throw new IllegalArgumentException("敏感词不能为空");
        }
        if (StringUtils.isBlank(word.getWordText())) {
            throw new IllegalArgumentException("敏感词内容不能为空");
        }
    }

    private void validateRule(CampusAlertRule rule) {
        if (rule == null) {
            throw new IllegalArgumentException("预警规则不能为空");
        }
        if (StringUtils.isBlank(rule.getRuleName())) {
            throw new IllegalArgumentException("规则名称不能为空");
        }
        if (StringUtils.isBlank(rule.getRuleType())) {
            throw new IllegalArgumentException("规则类型不能为空");
        }
    }

    private void validateAlert(CampusAlert alert) {
        if (alert == null) {
            throw new IllegalArgumentException("预警信息不能为空");
        }
        if (StringUtils.isBlank(alert.getAlertTitle())) {
            throw new IllegalArgumentException("预警标题不能为空");
        }
        if (StringUtils.isBlank(alert.getAlertSource())) {
            throw new IllegalArgumentException("预警来源不能为空");
        }
    }

    private CampusSensitiveWord requireSensitiveWord(Long wordId) {
        if (wordId == null) {
            throw new IllegalArgumentException("敏感词ID不能为空");
        }
        CampusSensitiveWord word = campusSensitiveWordDao.selectByWordId(wordId);
        if (word == null) {
            throw new IllegalArgumentException("敏感词不存在");
        }
        return word;
    }

    private CampusAlertRule requireRule(Long ruleId) {
        if (ruleId == null) {
            throw new IllegalArgumentException("规则ID不能为空");
        }
        CampusAlertRule rule = campusAlertRuleDao.selectByRuleId(ruleId);
        if (rule == null) {
            throw new IllegalArgumentException("预警规则不存在");
        }
        return rule;
    }

    private CampusAlert requireAlert(Long alertId) {
        if (alertId == null) {
            throw new IllegalArgumentException("预警ID不能为空");
        }
        CampusAlert alert = campusAlertDao.selectByAlertId(alertId);
        if (alert == null) {
            throw new IllegalArgumentException("预警不存在");
        }
        return alert;
    }

    private void setSensitiveWordDefaults(CampusSensitiveWord word) {
        normalizeSensitiveWordRisk(word);
        if (StringUtils.isBlank(word.getMatchType())) {
            word.setMatchType(MATCH_CONTAINS);
        }
        if (word.getStatus() == null) {
            word.setStatus(1);
        }
        word.setDeleted(0);
    }

    private void setRuleDefaults(CampusAlertRule rule) {
        normalizeRuleRisk(rule);
        if (rule.getEnabled() == null) {
            rule.setEnabled(1);
        }
        rule.setDeleted(0);
    }

    private void setAlertDefaults(CampusAlert alert) {
        if (StringUtils.isBlank(alert.getRiskLevel())) {
            alert.setRiskLevel(RISK_NORMAL);
        } else {
            alert.setRiskLevel(CampusRiskLevel.requireValid(alert.getRiskLevel()));
        }
        if (StringUtils.isBlank(alert.getAlertStatus())) {
            alert.setAlertStatus(STATUS_PENDING);
        }
        if (StringUtils.isBlank(alert.getEvidenceJson())) {
            alert.setEvidenceJson(summary(buildEvidenceJson(alert.getAlertSource(), alert.getSourceObjectId(),
                    alert.getRuleId(), alert.getRiskLevel(), alert.getMatchedKeywords(), alert.getAlertContent()), 4000));
        }
        alert.setDeleted(0);
    }

    private String buildEvidenceJson(String alertSource,
                                     Long sourceObjectId,
                                     Long ruleId,
                                     String riskLevel,
                                     String matchedKeywords,
                                     String content) {
        JSONObject evidence = new JSONObject();
        evidence.put("source", alertSource);
        evidence.put("sourceObjectId", sourceObjectId);
        evidence.put("ruleId", ruleId);
        evidence.put("riskLevel", CampusRiskLevel.normalizeOrDefault(riskLevel));
        evidence.put("matchedKeywords", matchedKeywords);
        evidence.put("contentSummary", summary(content, 500));
        return evidence.toJSONString();
    }

    private void normalizeSensitiveWordRisk(CampusSensitiveWord word) {
        if (StringUtils.isBlank(word.getRiskLevel())) {
            word.setRiskLevel(RISK_NORMAL);
        } else {
            word.setRiskLevel(CampusRiskLevel.requireValid(word.getRiskLevel()));
        }
    }

    private void normalizeRuleRisk(CampusAlertRule rule) {
        if (StringUtils.isBlank(rule.getRiskLevel())) {
            rule.setRiskLevel(RISK_NORMAL);
        } else {
            rule.setRiskLevel(CampusRiskLevel.requireValid(rule.getRiskLevel()));
        }
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

    private static class RuleMatch {

        private final boolean matched;
        private final String matchedKeywords;
        private final String riskLevel;

        private RuleMatch(boolean matched, String matchedKeywords, String riskLevel) {
            this.matched = matched;
            this.matchedKeywords = matchedKeywords;
            this.riskLevel = riskLevel;
        }

        private static RuleMatch of(String matchedKeywords, String riskLevel) {
            return new RuleMatch(true, matchedKeywords, riskLevel);
        }

        private static RuleMatch none() {
            return new RuleMatch(false, null, RISK_NORMAL);
        }
    }
}
