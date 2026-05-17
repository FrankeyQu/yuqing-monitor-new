package com.stonedt.intelligence.service.campus.ingest.baidu;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.stonedt.intelligence.service.campus.ingest.CampusIngestFetchRequest;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class BaiduIngestFetchConfig {

    public static final String PROVIDER = "baidu";
    public static final String DEFAULT_CREDENTIAL_REF = "BAIDU_API_KEY";
    private static final Pattern CREDENTIAL_REF_PATTERN = Pattern.compile("^[A-Za-z_][A-Za-z0-9_]*$");

    private static final int DEFAULT_TOP_K = 20;
    private static final int MAX_TOP_K = 50;
    private static final int MAX_QUERY_LENGTH = 500;
    private static final int DEFAULT_TIMEOUT_MS = 15000;
    private static final int MAX_TIMEOUT_MS = 30000;
    private static final int DEFAULT_READER_TIMEOUT_MS = 15000;
    private static final int DEFAULT_MAX_READER_CALLS = 5;
    private static final int MAX_CREDENTIAL_REF_LENGTH = 128;
    private static final List<String> DEFAULT_RESOURCE_TYPES;

    static {
        List<String> types = new ArrayList<>();
        types.add("web");
        DEFAULT_RESOURCE_TYPES = Collections.unmodifiableList(types);
    }

    private String provider;
    private String query;
    private int topK;
    private List<String> resourceTypes;
    private String credentialRef;
    private int timeoutMs;
    private boolean readerEnabled;
    private String readerProvider;
    private int maxReaderCalls;
    private boolean fallbackToSnippet;
    private int readerTimeoutMs;

    public static BaiduIngestFetchConfig fromRequest(CampusIngestFetchRequest request) {
        String fetchConfig = readFetchConfig(request);
        if (isBlank(fetchConfig)) {
            throw new BaiduIngestException("Baidu fetch_config is required");
        }

        JSONObject jsonObject;
        try {
            jsonObject = JSON.parseObject(fetchConfig);
        } catch (RuntimeException ex) {
            throw new BaiduIngestException("Baidu fetch_config JSON parse failed: " + ex.getMessage());
        }

        rejectInlineSecrets(jsonObject);

        BaiduIngestFetchConfig config = new BaiduIngestFetchConfig();
        config.provider = trimToEmpty(jsonObject.getString("provider"));

        config.query = trimToEmpty(jsonObject.getString("query"));
        if (isBlank(config.query)) {
            throw new BaiduIngestException("Baidu fetch_config query is required");
        }
        if (config.query.length() > MAX_QUERY_LENGTH) {
            throw new BaiduIngestException("Baidu fetch_config query is too long, max " + MAX_QUERY_LENGTH + " chars");
        }

        config.topK = boundedInt(jsonObject.get("topK"), DEFAULT_TOP_K, MAX_TOP_K);

        config.resourceTypes = parseResourceTypes(jsonObject.get("resourceTypes"));

        config.credentialRef = defaultString(jsonObject.getString("credentialRef"), DEFAULT_CREDENTIAL_REF);
        validateCredentialRef(config.credentialRef);

        config.timeoutMs = boundedInt(jsonObject.get("timeoutMs"), DEFAULT_TIMEOUT_MS, MAX_TIMEOUT_MS);
        config.readerEnabled = booleanValue(jsonObject.get("readerEnabled"), false);
        config.readerProvider = defaultString(jsonObject.getString("readerProvider"), "jina");
        config.maxReaderCalls = boundedInt(jsonObject.get("maxReaderCalls"), DEFAULT_MAX_READER_CALLS, MAX_TOP_K);
        config.fallbackToSnippet = booleanValue(jsonObject.get("fallbackToSnippet"), true);
        config.readerTimeoutMs = boundedInt(jsonObject.get("readerTimeoutMs"), DEFAULT_READER_TIMEOUT_MS, MAX_TIMEOUT_MS);
        if (config.readerEnabled && !"jina".equalsIgnoreCase(config.readerProvider)) {
            throw new BaiduIngestException("Baidu readerProvider currently only supports jina");
        }
        return config;
    }

    private static List<String> parseResourceTypes(Object value) {
        if (value instanceof JSONArray) {
            JSONArray array = (JSONArray) value;
            List<String> types = new ArrayList<>();
            for (int i = 0; i < array.size(); i++) {
                String type = trimToEmpty(array.getString(i));
                if (type.length() > 0) {
                    types.add(type);
                }
            }
            if (!types.isEmpty()) {
                return types;
            }
        }
        if (value instanceof String) {
            String str = ((String) value).trim();
            if (str.length() > 0) {
                List<String> types = new ArrayList<>();
                types.add(str);
                return types;
            }
        }
        return new ArrayList<>(DEFAULT_RESOURCE_TYPES);
    }

    private static void validateCredentialRef(String credentialRef) {
        if (isBlank(credentialRef)) {
            throw new BaiduIngestException("Baidu credentialRef is required");
        }
        if (credentialRef.length() > MAX_CREDENTIAL_REF_LENGTH) {
            throw new BaiduIngestException("Baidu credentialRef is too long");
        }
        if (!CREDENTIAL_REF_PATTERN.matcher(credentialRef).matches()) {
            throw new BaiduIngestException("Baidu credentialRef must be a valid environment variable name");
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
                "sign",
                "signature",
                "secret",
                "token"
        };
        for (String blockedKey : blockedKeys) {
            if (jsonObject.containsKey(blockedKey) && !isBlank(String.valueOf(jsonObject.get(blockedKey)))) {
                throw new BaiduIngestException("Baidu fetch_config must use credentialRef instead of inline secret values");
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
            throw new BaiduIngestException("Baidu fetch_config cannot be read");
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
            throw new BaiduIngestException("Baidu fetch_config cannot be read");
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

    private static boolean booleanValue(Object value, boolean defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        String str = String.valueOf(value).trim();
        if (str.length() == 0) {
            return defaultValue;
        }
        return "true".equalsIgnoreCase(str) || "1".equals(str) || "yes".equalsIgnoreCase(str);
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

    // getters

    public String getProvider() {
        return provider;
    }

    public String getQuery() {
        return query;
    }

    public int getTopK() {
        return topK;
    }

    public List<String> getResourceTypes() {
        return resourceTypes;
    }

    public String getCredentialRef() {
        return credentialRef;
    }

    public int getTimeoutMs() {
        return timeoutMs;
    }

    public boolean isReaderEnabled() {
        return readerEnabled;
    }

    public String getReaderProvider() {
        return readerProvider;
    }

    public int getMaxReaderCalls() {
        return maxReaderCalls;
    }

    public boolean isFallbackToSnippet() {
        return fallbackToSnippet;
    }

    public int getReaderTimeoutMs() {
        return readerTimeoutMs;
    }
}
