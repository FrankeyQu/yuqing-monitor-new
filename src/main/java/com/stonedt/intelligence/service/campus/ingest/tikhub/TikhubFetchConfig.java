package com.stonedt.intelligence.service.campus.ingest.tikhub;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.stonedt.intelligence.service.campus.ingest.CampusIngestFetchRequest;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.regex.Pattern;

public class TikhubFetchConfig {

    public static final String PROVIDER = "tikhub";
    public static final String DEFAULT_CREDENTIAL_REF = "TIKHUB_API_KEY";
    private static final Pattern CREDENTIAL_REF_PATTERN = Pattern.compile("^[A-Za-z_][A-Za-z0-9_]*$");

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 50;
    private static final int DEFAULT_PAGE = 1;
    private static final int MAX_PAGE = 100;
    private static final int MAX_QUERY_LENGTH = 120;
    private static final int DEFAULT_TIMEOUT_MS = 10000;
    private static final int MAX_TIMEOUT_MS = 30000;
    private static final int DEFAULT_MAX_DETAIL_CALLS = 20;
    private static final int MAX_DETAIL_CALLS = 50;
    private static final int MAX_CREDENTIAL_REF_LENGTH = 128;

    private String provider;
    private String endpointKey;
    private String platform;
    private String query;
    private int limit;
    private int page;
    private long cursor;
    private String searchType;
    private String sortType;
    private String publishTime;
    private String filterDuration;
    private String contentType;
    private String searchId;
    private String backtrace;
    private String credentialRef;
    private int timeoutMs;
    private boolean detailEnabled;
    private int maxDetailCalls;

    public static boolean isTikhubProvider(CampusIngestFetchRequest request) {
        String fetchConfig = readFetchConfig(request);
        if (isBlank(fetchConfig)) {
            return false;
        }
        try {
            JSONObject jsonObject = JSON.parseObject(fetchConfig);
            return PROVIDER.equalsIgnoreCase(trimToEmpty(jsonObject.getString("provider")));
        } catch (RuntimeException ex) {
            return false;
        }
    }

    public static TikhubFetchConfig fromRequest(CampusIngestFetchRequest request) {
        String fetchConfig = readFetchConfig(request);
        if (isBlank(fetchConfig)) {
            throw new TikhubIngestException("TikHub fetch_config is required");
        }

        JSONObject jsonObject;
        try {
            jsonObject = JSON.parseObject(fetchConfig);
        } catch (RuntimeException ex) {
            throw new TikhubIngestException("TikHub fetch_config JSON parse failed: " + ex.getMessage());
        }

        rejectInlineSecrets(jsonObject);

        TikhubFetchConfig config = new TikhubFetchConfig();
        config.provider = trimToEmpty(jsonObject.getString("provider"));
        if (!PROVIDER.equalsIgnoreCase(config.provider)) {
            throw new TikhubIngestException("TikHub provider is required");
        }

        config.endpointKey = trimToEmpty(jsonObject.getString("endpointKey"));
        if (isBlank(config.endpointKey)) {
            throw new TikhubIngestException("TikHub endpointKey is required");
        }

        config.platform = defaultString(jsonObject.getString("platform"), "");
        config.query = trimToEmpty(jsonObject.getString("query"));
        if (isBlank(config.query)) {
            throw new TikhubIngestException("TikHub query is required");
        }
        if (config.query.length() > MAX_QUERY_LENGTH) {
            throw new TikhubIngestException("TikHub query is too long");
        }

        config.limit = boundedInt(jsonObject.get("limit"), DEFAULT_LIMIT, MAX_LIMIT);
        config.page = boundedInt(jsonObject.get("page"), DEFAULT_PAGE, MAX_PAGE);
        config.cursor = longValue(jsonObject.get("cursor"), 0L);
        config.searchType = defaultString(jsonObject.getString("searchType"), "");
        config.sortType = defaultString(jsonObject.getString("sortType"), "0");
        config.publishTime = defaultString(jsonObject.getString("publishTime"), "0");
        config.filterDuration = defaultString(jsonObject.getString("filterDuration"), "0");
        config.contentType = defaultString(jsonObject.getString("contentType"), "0");
        config.searchId = defaultString(jsonObject.getString("searchId"), "");
        config.backtrace = defaultString(jsonObject.getString("backtrace"), "");
        config.credentialRef = defaultString(jsonObject.getString("credentialRef"), DEFAULT_CREDENTIAL_REF);
        validateCredentialRef(config.credentialRef);
        config.timeoutMs = boundedInt(jsonObject.get("timeoutMs"), DEFAULT_TIMEOUT_MS, MAX_TIMEOUT_MS);
        config.detailEnabled = booleanValue(jsonObject.get("detailEnabled"), false);
        config.maxDetailCalls = boundedInt(jsonObject.get("maxDetailCalls"), DEFAULT_MAX_DETAIL_CALLS, MAX_DETAIL_CALLS);
        return config;
    }

    private static void validateCredentialRef(String credentialRef) {
        if (isBlank(credentialRef)) {
            throw new TikhubIngestException("TikHub credentialRef is required");
        }
        if (credentialRef.length() > MAX_CREDENTIAL_REF_LENGTH) {
            throw new TikhubIngestException("TikHub credentialRef is too long");
        }
        if (!CREDENTIAL_REF_PATTERN.matcher(credentialRef).matches()) {
            throw new TikhubIngestException("TikHub credentialRef must be a valid environment variable name");
        }
    }

    private static void rejectInlineSecrets(JSONObject jsonObject) {
        String[] blockedKeys = new String[]{
                "apiKey",
                "api_key",
                "accessToken",
                "access_token",
                "refreshToken",
                "refresh_token",
                "authorization",
                "cookie",
                "cookies",
                "password",
                "session",
                "sessionId",
                "session_id",
                "deviceId",
                "device_id",
                "fingerprint",
                "msToken",
                "ttwid",
                "xBogus",
                "x_bogus",
                "aBogus",
                "a_bogus",
                "sign",
                "signature",
                "secret",
                "token"
        };
        for (String blockedKey : blockedKeys) {
            if (jsonObject.containsKey(blockedKey) && !isBlank(String.valueOf(jsonObject.get(blockedKey)))) {
                throw new TikhubIngestException("TikHub fetch_config must use credentialRef instead of inline secret values");
            }
        }
    }

    private static String readFetchConfig(CampusIngestFetchRequest request) {
        if (request == null || request.getTask() == null) {
            return null;
        }
        Object task = request.getTask();
        String value = invokeStringGetter(task, "getFetchConfig");
        if (value != null) {
            return value;
        }
        value = invokeStringGetter(task, "getFetch_config");
        if (value != null) {
            return value;
        }
        value = readStringField(task, "fetchConfig");
        if (value != null) {
            return value;
        }
        return readStringField(task, "fetch_config");
    }

    private static String invokeStringGetter(Object target, String methodName) {
        try {
            Method method = target.getClass().getMethod(methodName);
            Object value = method.invoke(target);
            return value == null ? null : String.valueOf(value);
        } catch (NoSuchMethodException ex) {
            return null;
        } catch (Exception ex) {
            throw new TikhubIngestException("TikHub fetch_config cannot be read");
        }
    }

    private static String readStringField(Object target, String fieldName) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            Object value = field.get(target);
            return value == null ? null : String.valueOf(value);
        } catch (NoSuchFieldException ex) {
            return null;
        } catch (Exception ex) {
            throw new TikhubIngestException("TikHub fetch_config cannot be read");
        }
    }

    private static int boundedInt(Object value, int defaultValue, int maxValue) {
        int parsed = intValue(value, defaultValue);
        if (parsed <= 0) {
            return defaultValue;
        }
        return Math.min(parsed, maxValue);
    }

    private static int intValue(Object value, int defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private static long longValue(Object value, long defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private static boolean booleanValue(Object value, boolean defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        String text = String.valueOf(value).trim();
        if ("1".equals(text) || "true".equalsIgnoreCase(text) || "yes".equalsIgnoreCase(text)) {
            return true;
        }
        if ("0".equals(text) || "false".equalsIgnoreCase(text) || "no".equalsIgnoreCase(text)) {
            return false;
        }
        return defaultValue;
    }

    private static String defaultString(String value, String defaultValue) {
        String trimmed = trimToEmpty(value);
        return trimmed.length() == 0 ? defaultValue : trimmed;
    }

    private static String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().length() == 0;
    }

    public String getProvider() {
        return provider;
    }

    public String getEndpointKey() {
        return endpointKey;
    }

    public String getPlatform() {
        return platform;
    }

    public String getQuery() {
        return query;
    }

    public int getLimit() {
        return limit;
    }

    public int getPage() {
        return page;
    }

    public long getCursor() {
        return cursor;
    }

    public String getSearchType() {
        return searchType;
    }

    public String getSortType() {
        return sortType;
    }

    public String getPublishTime() {
        return publishTime;
    }

    public String getFilterDuration() {
        return filterDuration;
    }

    public String getContentType() {
        return contentType;
    }

    public String getSearchId() {
        return searchId;
    }

    public String getBacktrace() {
        return backtrace;
    }

    public String getCredentialRef() {
        return credentialRef;
    }

    public int getTimeoutMs() {
        return timeoutMs;
    }

    public boolean isDetailEnabled() {
        return detailEnabled;
    }

    public int getMaxDetailCalls() {
        return maxDetailCalls;
    }
}
