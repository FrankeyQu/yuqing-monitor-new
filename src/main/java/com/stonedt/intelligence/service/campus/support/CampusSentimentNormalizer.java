package com.stonedt.intelligence.service.campus.support;

import org.apache.commons.lang3.StringUtils;

public final class CampusSentimentNormalizer {

    private CampusSentimentNormalizer() {
    }

    public static String normalize(String raw) {
        String value = StringUtils.trimToNull(raw);
        if (value == null) {
            return null;
        }
        String lower = value.toLowerCase();
        if ("none".equals(lower) || "unknown".equals(lower) || "未知".equals(value)) {
            return "none";
        }
        if (lower.contains("negative") || lower.contains("neg") || value.contains("负")) {
            return "negative";
        }
        if (lower.contains("positive") || lower.contains("pos") || value.contains("正")) {
            return "positive";
        }
        if (lower.contains("neutral") || value.contains("中")) {
            return "neutral";
        }
        return null;
    }

    public static String normalizeOrDefault(String raw, String defaultValue) {
        return StringUtils.defaultIfBlank(normalize(raw), defaultValue);
    }
}
