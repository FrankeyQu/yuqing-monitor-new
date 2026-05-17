package com.stonedt.intelligence.service.campus.ingest.baidu;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.Map;

public final class BaiduIngestSanitizer {

    private static final int MAX_ERROR_LENGTH = 500;
    private static final String REDACTED = "[REDACTED]";

    private BaiduIngestSanitizer() {
    }

    public static String sanitizeError(String message) {
        return sanitizeError(message, new String[0]);
    }

    public static String sanitizeError(String message, String... sensitiveValues) {
        String sanitized = sanitizeText(message);
        if (sanitized == null) {
            return null;
        }
        sanitized = sanitizeSensitiveValues(sanitized, sensitiveValues);
        if (sanitized.length() <= MAX_ERROR_LENGTH) {
            return sanitized;
        }
        return sanitized.substring(0, MAX_ERROR_LENGTH) + "...";
    }

    public static String sanitizeText(String text) {
        return sanitizeText(text, new String[0]);
    }

    public static String sanitizeText(String text, String... sensitiveValues) {
        if (text == null) {
            return null;
        }
        String sanitized = text;
        sanitized = sanitized.replaceAll("(?i)(authorization\\s*[:=]\\s*bearer\\s+)[^\\s,;}]+", "$1" + REDACTED);
        sanitized = sanitized.replaceAll("(?i)(bearer\\s+)[A-Za-z0-9._\\-]+", "$1" + REDACTED);
        sanitized = sanitized.replaceAll("(?i)(api[_-]?key\\s*[:=]\\s*)[^\\s,;}]+", "$1" + REDACTED);
        String configuredKey = System.getenv(BaiduIngestFetchConfig.DEFAULT_CREDENTIAL_REF);
        if (configuredKey != null && configuredKey.trim().length() > 0) {
            sanitized = sanitized.replace(configuredKey, REDACTED);
        }
        sanitized = sanitizeSensitiveValues(sanitized, sensitiveValues);
        return sanitized;
    }

    public static String sanitizeJsonToString(Object value) {
        return JSON.toJSONString(sanitizeJsonValue(value));
    }

    private static Object sanitizeJsonValue(Object value) {
        if (value instanceof JSONObject) {
            JSONObject source = (JSONObject) value;
            JSONObject target = new JSONObject();
            for (Map.Entry<String, Object> entry : source.entrySet()) {
                if (isSensitiveKey(entry.getKey())) {
                    target.put(entry.getKey(), REDACTED);
                } else {
                    target.put(entry.getKey(), sanitizeJsonValue(entry.getValue()));
                }
            }
            return target;
        }
        if (value instanceof JSONArray) {
            JSONArray source = (JSONArray) value;
            JSONArray target = new JSONArray();
            for (Object item : source) {
                target.add(sanitizeJsonValue(item));
            }
            return target;
        }
        if (value instanceof String) {
            return sanitizeText((String) value);
        }
        return value;
    }

    private static String sanitizeSensitiveValues(String value, String... sensitiveValues) {
        if (value == null || sensitiveValues == null) {
            return value;
        }
        String sanitized = value;
        for (String sensitiveValue : sensitiveValues) {
            if (sensitiveValue == null || sensitiveValue.trim().isEmpty()) {
                continue;
            }
            sanitized = sanitized.replace(sensitiveValue, REDACTED);
        }
        return sanitized;
    }

    private static boolean isSensitiveKey(String key) {
        if (key == null) {
            return false;
        }
        String normalized = key.toLowerCase().replace("_", "").replace("-", "").replace(".", "");
        return "authorization".equals(normalized)
                || "apikey".equals(normalized)
                || "accesstoken".equals(normalized)
                || "refreshtoken".equals(normalized)
                || normalized.endsWith("token")
                || normalized.contains("secret")
                || normalized.contains("credential")
                || "sign".equals(normalized)
                || "signature".equals(normalized);
    }
}
