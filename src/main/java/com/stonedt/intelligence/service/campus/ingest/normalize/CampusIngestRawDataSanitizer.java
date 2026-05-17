package com.stonedt.intelligence.service.campus.ingest.normalize;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CampusIngestRawDataSanitizer {

    private static final String REDACTED = "[REDACTED]";

    public String sanitizeToString(Object value) {
        if (value == null) {
            return null;
        }
        Object sanitized = sanitizeValue(value);
        if (sanitized instanceof String) {
            String text = (String) sanitized;
            Object parsed = parseJson(text);
            if (parsed != null) {
                return JSON.toJSONString(sanitizeValue(parsed));
            }
            return text;
        }
        return JSON.toJSONString(sanitized);
    }

    public String sanitizeText(String text) {
        if (text == null) {
            return null;
        }
        String sanitized = text;
        sanitized = sanitized.replaceAll("(?i)(authorization\\s*[:=]\\s*bearer\\s+)[^\\s,;}]+", "$1" + REDACTED);
        sanitized = sanitized.replaceAll("(?i)(bearer\\s+)[A-Za-z0-9._\\-]+", "$1" + REDACTED);
        sanitized = sanitized.replaceAll("(?i)(api[_-]?key\\s*[:=]\\s*)[^\\s,;}]+", "$1" + REDACTED);
        sanitized = sanitized.replaceAll("(?i)(access[_-]?token\\s*[:=]\\s*)[^\\s,;}]+", "$1" + REDACTED);
        sanitized = sanitized.replaceAll("(?i)(refresh[_-]?token\\s*[:=]\\s*)[^\\s,;}]+", "$1" + REDACTED);
        sanitized = sanitized.replaceAll("(?i)(session[_-]?id\\s*[:=]\\s*)[^\\s,;}]+", "$1" + REDACTED);
        sanitized = sanitized.replaceAll("(?i)(password\\s*[:=]\\s*)[^\\s,;}]+", "$1" + REDACTED);
        sanitized = sanitized.replaceAll("(?i)(cookie\\s*[:=]\\s*)[^\\r\\n;}]+", "$1" + REDACTED);
        return sanitized;
    }

    private Object sanitizeValue(Object value) {
        if (value instanceof JSONObject) {
            JSONObject source = (JSONObject) value;
            JSONObject target = new JSONObject();
            for (Map.Entry<String, Object> entry : source.entrySet()) {
                if (isSensitiveKey(entry.getKey())) {
                    target.put(entry.getKey(), REDACTED);
                } else {
                    target.put(entry.getKey(), sanitizeValue(entry.getValue()));
                }
            }
            return target;
        }
        if (value instanceof JSONArray) {
            JSONArray source = (JSONArray) value;
            JSONArray target = new JSONArray();
            for (Object item : source) {
                target.add(sanitizeValue(item));
            }
            return target;
        }
        if (value instanceof String) {
            return sanitizeText((String) value);
        }
        return value;
    }

    private Object parseJson(String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        }
        String trimmed = text.trim();
        try {
            if (trimmed.startsWith("{")) {
                return JSON.parseObject(trimmed);
            }
            if (trimmed.startsWith("[")) {
                return JSON.parseArray(trimmed);
            }
            return null;
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private boolean isSensitiveKey(String key) {
        if (key == null) {
            return false;
        }
        String normalized = key.toLowerCase().replace("_", "").replace("-", "").replace(".", "");
        return "authorization".equals(normalized)
                || "apikey".equals(normalized)
                || "cookie".equals(normalized)
                || "cookies".equals(normalized)
                || "password".equals(normalized)
                || "session".equals(normalized)
                || "sessionid".equals(normalized)
                || "deviceid".equals(normalized)
                || "fingerprint".equals(normalized)
                || "mstoken".equals(normalized)
                || "ttwid".equals(normalized)
                || "xbogus".equals(normalized)
                || "abogus".equals(normalized)
                || "accesstoken".equals(normalized)
                || "refreshtoken".equals(normalized)
                || normalized.endsWith("token")
                || normalized.contains("secret")
                || normalized.contains("credential");
    }
}
