package com.stonedt.intelligence.service.campus.ingest.security;

import org.apache.commons.lang3.StringUtils;

public final class CampusIngestAuditSanitizer {

    private static final String REDACTED = "[REDACTED]";
    private static final int MAX_SECRET_LENGTH = 16;

    private CampusIngestAuditSanitizer() {
    }

    public static String sanitize(String value) {
        if (value == null) {
            return null;
        }
        String sanitized = value;
        sanitized = sanitized.replaceAll("(?i)(authorization\\s*[:=]\\s*bearer\\s+)[^\\s,;}]+", "$1" + REDACTED);
        sanitized = sanitized.replaceAll("(?i)(bearer\\s+)[A-Za-z0-9._\\-]+", "$1" + REDACTED);
        sanitized = sanitized.replaceAll("(?i)(\\\\\"?(api[_-]?key|access[_-]?token|refresh[_-]?token|authorization|cookie|cookies|password|session|session[_-]?id|secret|token|device[_-]?id|fingerprint|msToken|ttwid|xBogus|x_bogus|aBogus|a_bogus|sign|signature)\\\\\"?\\s*[:=]\\s*\\\\\")([^\\\\\"]+)(\\\\\")", "$1" + REDACTED + "$4");
        sanitized = sanitized.replaceAll("(?i)(\"?(api[_-]?key|access[_-]?token|refresh[_-]?token|authorization|cookie|cookies|password|session|session[_-]?id|secret|token|device[_-]?id|fingerprint|msToken|ttwid|xBogus|x_bogus|aBogus|a_bogus|sign|signature)\"?\\s*[:=]\\s*\")([^\"]+)(\")", "$1" + REDACTED + "$4");
        sanitized = sanitized.replaceAll("(?i)((api[_-]?key|access[_-]?token|refresh[_-]?token|authorization|cookie|cookies|password|session|session[_-]?id|secret|token|device[_-]?id|fingerprint|msToken|ttwid|xBogus|x_bogus|aBogus|a_bogus|sign|signature)\\s*[:=]\\s*)([^\\s,;}]+)", "$1" + REDACTED);
        sanitized = sanitizeConfiguredEnvSecret(sanitized, "TIKHUB_API_KEY");
        sanitized = sanitized.replaceAll("(?i)(credentialRef\"?\\s*[:=]\\s*\")([^\"]{"
                + (MAX_SECRET_LENGTH + 1) + ",})(\")", "$1" + REDACTED + "$3");
        return sanitized;
    }

    private static String sanitizeConfiguredEnvSecret(String value, String envName) {
        String envValue = System.getenv(envName);
        if (StringUtils.isBlank(envValue)) {
            return value;
        }
        return value.replace(envValue, REDACTED);
    }
}
