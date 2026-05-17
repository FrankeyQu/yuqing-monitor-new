package com.stonedt.intelligence.service.campus.support;

import org.apache.commons.lang3.StringUtils;

public enum CampusRiskLevel {

    NORMAL("normal", "普通关注", 1),
    CONCERN("concern", "一般预警", 2),
    MAJOR("major", "重大预警", 3),
    URGENT("urgent", "特别重大", 4);

    private final String code;
    private final String label;
    private final int rank;

    CampusRiskLevel(String code, String label, int rank) {
        this.code = code;
        this.label = label;
        this.rank = rank;
    }

    public String code() {
        return code;
    }

    public String label() {
        return label;
    }

    public int rank() {
        return rank;
    }

    public static String normalCode() {
        return NORMAL.code;
    }

    public static String concernCode() {
        return CONCERN.code;
    }

    public static String normalizeOrDefault(String value) {
        String normalized = normalize(value);
        return isCode(normalized) ? normalized : NORMAL.code;
    }

    public static String normalizeForQuery(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        String normalized = normalize(value);
        return isCode(normalized) ? normalized : value.trim();
    }

    public static String requireValid(String value) {
        if (StringUtils.isBlank(value)) {
            throw new IllegalArgumentException("风险等级不能为空");
        }
        String normalized = normalize(value);
        if (!isCode(normalized)) {
            throw new IllegalArgumentException("风险等级不合法");
        }
        return normalized;
    }

    public static boolean isNonNormal(String value) {
        return !NORMAL.code.equals(normalizeOrDefault(value));
    }

    public static String higher(String left, String right) {
        return rank(right) > rank(left) ? normalizeOrDefault(right) : normalizeOrDefault(left);
    }

    public static int rank(String value) {
        String normalized = normalize(value);
        for (CampusRiskLevel level : values()) {
            if (level.code.equals(normalized)) {
                return level.rank;
            }
        }
        return NORMAL.rank;
    }

    public static String label(String value) {
        String normalized = normalize(value);
        for (CampusRiskLevel level : values()) {
            if (level.code.equals(normalized)) {
                return level.label;
            }
        }
        return StringUtils.defaultIfBlank(value, NORMAL.label);
    }

    private static String normalize(String value) {
        if (StringUtils.isBlank(value)) {
            return NORMAL.code;
        }
        String trimmed = value.trim();
        for (CampusRiskLevel level : values()) {
            if (level.code.equals(trimmed)) {
                return level.code;
            }
        }
        if ("一般".equals(trimmed) || "普通".equals(trimmed) || "一般关注".equals(trimmed)
                || "普通关注".equals(trimmed)) {
            return NORMAL.code;
        }
        if ("关注".equals(trimmed) || "一般预警".equals(trimmed)) {
            return CONCERN.code;
        }
        if ("higher".equals(trimmed) || "较大".equals(trimmed) || "较大风险".equals(trimmed)
                || "重大".equals(trimmed) || "重大风险".equals(trimmed) || "重大预警".equals(trimmed)) {
            return MAJOR.code;
        }
        if ("紧急".equals(trimmed) || "紧急事件".equals(trimmed) || "特别重大".equals(trimmed)) {
            return URGENT.code;
        }
        return trimmed;
    }

    private static boolean isCode(String value) {
        for (CampusRiskLevel level : values()) {
            if (level.code.equals(value)) {
                return true;
            }
        }
        return false;
    }
}
