package com.stonedt.intelligence.service.campus.support;

import com.stonedt.intelligence.entity.campus.CampusIngestRecord;
import com.stonedt.intelligence.entity.campus.CampusMonitorTask;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashSet;
import java.util.Set;

public class CampusSchoolRelevanceService {

    private static final String[] SCHOOL_CONTEXT_TERMS = new String[]{
            "学校", "校园", "学院", "大学", "中学", "小学", "幼儿园",
            "老师", "教师", "学生", "家长", "班级", "宿舍", "食堂", "校区"
    };

    public CampusSchoolRelevance evaluate(CampusMonitorTask task,
                                          CampusIngestRecord record,
                                          Set<String> matchedSubjects,
                                          Set<String> matchedKeywords) {
        String text = join(record == null ? null : record.getTitle(),
                record == null ? null : record.getContent(),
                record == null ? null : record.getKeywords(),
                record == null ? null : record.getAuthorName());
        Set<String> taskTerms = split(join(task == null ? null : task.getMonitorSubject(),
                task == null ? null : task.getSubjectAliases()));
        return evaluate(text, taskTerms, matchedSubjects, matchedKeywords);
    }

    public CampusSchoolRelevance evaluateText(String title,
                                              String content,
                                              String keywords,
                                              String involvedAccount) {
        String text = join(title, content, keywords, involvedAccount);
        return evaluate(text, new LinkedHashSet<String>(), new LinkedHashSet<String>(), split(keywords));
    }

    private CampusSchoolRelevance evaluate(String text,
                                           Set<String> taskTerms,
                                           Set<String> matchedSubjects,
                                           Set<String> matchedKeywords) {
        Set<String> matchedTerms = new LinkedHashSet<>();
        int score = 0;
        if (matchedSubjects != null && !matchedSubjects.isEmpty()) {
            matchedTerms.addAll(matchedSubjects);
            score += Math.min(55, matchedSubjects.size() * 35);
        }
        Set<String> taskMatches = match(text, taskTerms);
        if (!taskMatches.isEmpty()) {
            matchedTerms.addAll(taskMatches);
            score += Math.min(35, taskMatches.size() * 20);
        }
        Set<String> contextMatches = match(text, split(StringUtils.join(SCHOOL_CONTEXT_TERMS, ",")));
        if (!contextMatches.isEmpty()) {
            matchedTerms.addAll(contextMatches);
            score += Math.min(20, contextMatches.size() * 5);
        }
        if (matchedKeywords != null && !matchedKeywords.isEmpty()) {
            score += Math.min(10, matchedKeywords.size() * 3);
        }
        score = Math.min(100, score);
        String reason = score >= 70 ? "命中学校主体和校园语境"
                : score >= 40 ? "命中学校主体或校园语境"
                : score > 0 ? "仅命中弱校园语境"
                : "未命中学校相关语境";
        return new CampusSchoolRelevance(score, reason, StringUtils.join(matchedTerms, ","), null);
    }

    private Set<String> match(String text, Set<String> tokens) {
        Set<String> matched = new LinkedHashSet<>();
        if (StringUtils.isBlank(text) || tokens == null || tokens.isEmpty()) {
            return matched;
        }
        for (String token : tokens) {
            if (StringUtils.isNotBlank(token) && StringUtils.containsIgnoreCase(text, token)) {
                matched.add(token);
            }
        }
        return matched;
    }

    private Set<String> split(String raw) {
        Set<String> tokens = new LinkedHashSet<>();
        if (StringUtils.isBlank(raw)) {
            return tokens;
        }
        String[] parts = raw.split("[,;，；、\\n\\r\\t ]+");
        for (String part : parts) {
            if (StringUtils.isNotBlank(part)) {
                tokens.add(part.trim());
            }
        }
        return tokens;
    }

    private String join(String... values) {
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
}
