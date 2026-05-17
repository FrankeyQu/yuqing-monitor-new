package com.stonedt.intelligence.service.minority.search.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.stonedt.intelligence.service.minority.model.MinoritySearchResult;
import com.stonedt.intelligence.service.minority.search.SearchEngineAdapter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 百度搜索引擎适配器
 * 基于百度智能云千帆 Web Search API 实现
 */
@Component
public class BaiduSearchAdapter implements SearchEngineAdapter {

    private static final Logger logger = LoggerFactory.getLogger(BaiduSearchAdapter.class);

    private static final String API_URL = "https://qianfan.baidubce.com/v2/ai_search/web_search";
    private static final int CONNECT_TIMEOUT_MS = 15_000;
    private static final int READ_TIMEOUT_MS = 30_000;

    @Value("${baidu.api.key:}")
    private String apiKey;

    @Override
    public String getEngineName() {
        return "baidu";
    }

    @Override
    public List<MinoritySearchResult> search(String keyword, int page) {
        if (StringUtils.isBlank(apiKey)) {
            logger.warn("Baidu API key not configured, skipping Baidu search");
            return new ArrayList<>();
        }

        String responseBody;
        try {
            responseBody = callQianfanApi(keyword);
        } catch (Exception e) {
            logger.error("Baidu Qianfan API call failed, keyword={}, page={}", keyword, page, e);
            return new ArrayList<>();
        }

        if (StringUtils.isBlank(responseBody)) {
            return new ArrayList<>();
        }

        try {
            return parseResponse(responseBody);
        } catch (Exception e) {
            logger.error("Failed to parse Baidu Qianfan API response, keyword={}", keyword, e);
            return new ArrayList<>();
        }
    }

    /**
     * 调用百度千帆 Web Search API
     */
    private String callQianfanApi(String keyword) throws Exception {
        URL url = new URL(API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
        conn.setReadTimeout(READ_TIMEOUT_MS);
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setRequestProperty("X-Appbuilder-Authorization", "Bearer " + apiKey);
        conn.setDoOutput(true);

        JSONObject body = new JSONObject();

        JSONArray messages = new JSONArray();
        JSONObject msg = new JSONObject();
        msg.put("content", keyword);
        msg.put("role", "user");
        messages.add(msg);
        body.put("messages", messages);

        body.put("search_source", "baidu_search_v2");

        JSONArray resourceFilter = new JSONArray();
        JSONObject webFilter = new JSONObject();
        webFilter.put("type", "web");
        webFilter.put("top_k", 20);
        resourceFilter.add(webFilter);
        body.put("resource_type_filter", resourceFilter);

        byte[] bodyBytes = body.toJSONString().getBytes(StandardCharsets.UTF_8);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(bodyBytes);
            os.flush();
        }

        int statusCode = conn.getResponseCode();
        if (statusCode != 200) {
            String errorBody = readStream(conn.getErrorStream());
            logger.error("Baidu Qianfan API returned status {}: {}", statusCode, errorBody);
            return null;
        }

        String responseBody = readStream(conn.getInputStream());
        conn.disconnect();
        return responseBody;
    }

    /**
     * 解析 API 响应，提取搜索结果
     */
    private List<MinoritySearchResult> parseResponse(String responseBody) {
        JSONObject response = JSON.parseObject(responseBody);

        // 检查业务错误码
        String code = response.getString("code");
        if (StringUtils.isNotBlank(code) && !"0".equals(code) && !"200".equals(code)) {
            logger.warn("Baidu Qianfan API business error: code={}, message={}",
                    code, response.getString("message"));
            return new ArrayList<>();
        }

        JSONArray references = response.getJSONArray("references");
        if (references == null || references.isEmpty()) {
            return new ArrayList<>();
        }

        List<MinoritySearchResult> results = new ArrayList<>();
        for (int i = 0; i < references.size(); i++) {
            try {
                JSONObject ref = references.getJSONObject(i);
                MinoritySearchResult result = new MinoritySearchResult();
                result.setTitle(StringUtils.defaultString(ref.getString("title")));
                result.setUrl(StringUtils.defaultString(ref.getString("url")));
                result.setSnippet(StringUtils.defaultString(ref.getString("content")));
                result.setSource(extractSource(ref.getString("url")));
                result.setEngine("baidu");
                result.setPublishDate(StringUtils.defaultString(ref.getString("date")));
                results.add(result);
            } catch (Exception e) {
                logger.warn("Failed to parse a single Baidu Qianfan reference item", e);
            }
        }

        return results;
    }

    /**
     * 读取输入流中的全部文本。
     */
    private String readStream(InputStream stream) throws java.io.IOException {
        if (stream == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    /**
     * 从 URL 中提取域名作为来源
     */
    private String extractSource(String urlStr) {
        if (StringUtils.isBlank(urlStr)) {
            return "";
        }
        try {
            URI uri = new URI(urlStr);
            String host = uri.getHost();
            return host != null ? host : "";
        } catch (Exception e) {
            return "";
        }
    }
}
