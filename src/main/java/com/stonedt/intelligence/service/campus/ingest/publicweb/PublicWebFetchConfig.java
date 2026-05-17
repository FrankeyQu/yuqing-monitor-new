package com.stonedt.intelligence.service.campus.ingest.publicweb;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.stonedt.intelligence.service.campus.ingest.CampusIngestFetchRequest;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class PublicWebFetchConfig {

    private static final Set<String> ALLOWED_CONFIG_KEYS = new HashSet<>(
            Arrays.asList("whitelistId", "url", "mode", "readerProvider", "maxDepth", "timeoutMs"));
    public static final String MODE_METADATA_ONLY = "metadata_only";
    public static final String MODE_JINA_READER = "jina_reader";

    private Long whitelistId;
    private String url;
    private String mode;
    private String readerProvider;
    private int maxDepth;
    private int timeoutMs;

    public static PublicWebFetchConfig fromRequest(CampusIngestFetchRequest request) {
        if (request == null || request.getTask() == null || StringUtils.isBlank(request.getTask().getFetchConfig())) {
            throw new PublicWebIngestException("公开网页 fetchConfig 不能为空");
        }
        return fromJson(request.getTask().getFetchConfig());
    }

    public static PublicWebFetchConfig fromJson(String fetchConfig) {
        if (StringUtils.isBlank(fetchConfig)) {
            throw new PublicWebIngestException("公开网页 fetchConfig 不能为空");
        }
        JSONObject jsonObject;
        try {
            jsonObject = JSON.parseObject(fetchConfig);
        } catch (RuntimeException ex) {
            throw new PublicWebIngestException("公开网页 fetchConfig JSON 解析失败: " + ex.getMessage());
        }
        rejectUnsupportedKeys(jsonObject);
        PublicWebFetchConfig config = new PublicWebFetchConfig();
        config.whitelistId = longValue(jsonObject.get("whitelistId"));
        config.url = StringUtils.trimToEmpty(jsonObject.getString("url"));
        config.mode = StringUtils.defaultIfBlank(jsonObject.getString("mode"), MODE_METADATA_ONLY);
        config.readerProvider = StringUtils.defaultIfBlank(jsonObject.getString("readerProvider"), "jina");
        config.maxDepth = intValue(jsonObject.get("maxDepth"), 0);
        config.timeoutMs = intValue(jsonObject.get("timeoutMs"), 15000);
        if (config.whitelistId == null) {
            throw new PublicWebIngestException("公开网页白名单ID不能为空");
        }
        if (StringUtils.isBlank(config.url)) {
            throw new PublicWebIngestException("公开网页URL不能为空");
        }
        if (!MODE_METADATA_ONLY.equals(config.mode) && !MODE_JINA_READER.equals(config.mode)) {
            throw new PublicWebIngestException("公开网页 mode 仅允许 metadata_only 或 jina_reader");
        }
        if (MODE_JINA_READER.equals(config.mode) && !"jina".equalsIgnoreCase(config.readerProvider)) {
            throw new PublicWebIngestException("公开网页正文读取当前仅支持 Jina Reader");
        }
        if (config.maxDepth != 0) {
            throw new PublicWebIngestException("公开网页正文读取第一版仅支持单URL，maxDepth 必须为 0");
        }
        return config;
    }

    private static void rejectUnsupportedKeys(JSONObject jsonObject) {
        if (jsonObject == null) {
            return;
        }
        for (String key : jsonObject.keySet()) {
            if (!ALLOWED_CONFIG_KEYS.contains(key)) {
                throw new PublicWebIngestException("Batch27 公开网页 fetchConfig 仅允许 whitelistId、url、mode");
            }
        }
    }

    private static Long longValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.valueOf(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return null;
        }
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

    public Long getWhitelistId() {
        return whitelistId;
    }

    public String getUrl() {
        return url;
    }

    public String getMode() {
        return mode;
    }

    public String getReaderProvider() {
        return readerProvider;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public int getTimeoutMs() {
        return timeoutMs;
    }
}
