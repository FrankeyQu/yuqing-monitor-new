package com.stonedt.intelligence.service.campus.support;

import com.stonedt.intelligence.entity.campus.CampusDictItem;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CampusTopicClassifier {

    public static final String DICT_TYPE = "campus_event_topic";
    private static final String TOPIC_OTHER = "other";

    private static final Map<String, List<String>> FALLBACK_TERMS = new LinkedHashMap<>();

    static {
        FALLBACK_TERMS.put("food_safety", Arrays.asList("食堂", "饭菜", "餐饮", "食品", "卫生", "腹泻", "异物", "饮水"));
        FALLBACK_TERMS.put("dormitory", Arrays.asList("宿舍", "寝室", "门禁", "热水", "空调", "水电", "住宿"));
        FALLBACK_TERMS.put("campus_safety", Arrays.asList("安全", "消防", "交通", "事故", "受伤", "校车", "设施"));
        FALLBACK_TERMS.put("bullying_conflict", Arrays.asList("欺凌", "霸凌", "冲突", "打架", "辱骂", "网暴"));
        FALLBACK_TERMS.put("teacher_ethics", Arrays.asList("老师", "教师", "师德", "体罚", "补课", "师生"));
        FALLBACK_TERMS.put("fee_dispute", Arrays.asList("收费", "退费", "学费", "住宿费", "乱收费"));
        FALLBACK_TERMS.put("admission_employment", Arrays.asList("招生", "录取", "就业", "实习", "升学"));
        FALLBACK_TERMS.put("exam_teaching", Arrays.asList("考试", "成绩", "作业", "课程", "教学", "课堂"));
        FALLBACK_TERMS.put("logistics_service", Arrays.asList("后勤", "维修", "物业", "网络", "医疗", "服务"));
        FALLBACK_TERMS.put("public_incident", Arrays.asList("公共卫生", "传染病", "暴雨", "地震", "群体", "集会"));
        FALLBACK_TERMS.put("rumor", Arrays.asList("谣言", "不实", "网传", "造谣", "误传", "未经证实"));
    }

    public CampusTopicClassification classify(String title, String content, String keywords) {
        return classify(title, content, keywords, null);
    }

    public CampusTopicClassification classify(String title,
                                              String content,
                                              String keywords,
                                              List<CampusDictItem> topicItems) {
        String text = join(title, content, keywords);
        if (StringUtils.isBlank(text)) {
            return new CampusTopicClassification(TOPIC_OTHER, "其他", "无可分类文本");
        }
        List<TopicCandidate> candidates = buildCandidates(topicItems);
        TopicCandidate best = null;
        Set<String> bestTerms = new LinkedHashSet<>();
        int bestScore = 0;
        for (TopicCandidate candidate : candidates) {
            Set<String> matched = match(text, candidate.terms);
            int score = matched.size();
            if (StringUtils.isNotBlank(candidate.label) && StringUtils.containsIgnoreCase(text, candidate.label)) {
                matched.add(candidate.label);
                score += 2;
            }
            if (score > bestScore) {
                best = candidate;
                bestTerms = matched;
                bestScore = score;
            }
        }
        if (best == null || bestScore == 0) {
            return new CampusTopicClassification(TOPIC_OTHER, "其他", "未命中明确主题词");
        }
        return new CampusTopicClassification(best.code, best.label,
                "命中主题词：" + StringUtils.join(bestTerms, ","));
    }

    private List<TopicCandidate> buildCandidates(List<CampusDictItem> topicItems) {
        List<TopicCandidate> candidates = new ArrayList<>();
        if (topicItems != null) {
            for (CampusDictItem item : topicItems) {
                if (item == null) {
                    continue;
                }
                String code = StringUtils.defaultIfBlank(item.getItemValue(), item.getItemCode());
                if (StringUtils.isBlank(code) || TOPIC_OTHER.equals(code)) {
                    continue;
                }
                Set<String> terms = new LinkedHashSet<>();
                terms.add(item.getItemName());
                terms.addAll(split(item.getDescription()));
                List<String> fallback = FALLBACK_TERMS.get(code);
                if (fallback != null) {
                    terms.addAll(fallback);
                }
                candidates.add(new TopicCandidate(code, item.getItemName(), terms));
            }
        }
        if (!candidates.isEmpty()) {
            return candidates;
        }
        for (Map.Entry<String, List<String>> entry : FALLBACK_TERMS.entrySet()) {
            candidates.add(new TopicCandidate(entry.getKey(), entry.getKey(), new LinkedHashSet<>(entry.getValue())));
        }
        return candidates;
    }

    private Set<String> match(String text, Set<String> tokens) {
        Set<String> matched = new LinkedHashSet<>();
        if (tokens == null) {
            return matched;
        }
        for (String token : tokens) {
            if (StringUtils.isNotBlank(token) && token.length() <= 16
                    && StringUtils.containsIgnoreCase(text, token)) {
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
        String[] parts = raw.split("[,;，；、\\n\\r\\t /|]+");
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

    private static class TopicCandidate {

        private final String code;
        private final String label;
        private final Set<String> terms;

        private TopicCandidate(String code, String label, Set<String> terms) {
            this.code = code;
            this.label = label;
            this.terms = terms;
        }
    }
}
