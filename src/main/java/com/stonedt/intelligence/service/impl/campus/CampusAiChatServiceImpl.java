package com.stonedt.intelligence.service.impl.campus;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.stonedt.intelligence.entity.campus.CampusAiCallLog;
import com.stonedt.intelligence.service.campus.ai.CampusAiChatRequest;
import com.stonedt.intelligence.service.campus.ai.CampusAiChatResponse;
import com.stonedt.intelligence.service.campus.ai.CampusAiChatService;
import com.stonedt.intelligence.service.campus.ai.CampusAiRuntimeConfig;
import com.stonedt.intelligence.service.campus.ai.CampusAiRuntimeService;
import com.stonedt.intelligence.util.SnowflakeUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class CampusAiChatServiceImpl implements CampusAiChatService {

    private static final String DEFAULT_FEATURE_CODE = "report_generate";
    private static final String DEFAULT_PROVIDER = "deepseek";
    private static final String DEFAULT_MODEL = "deepseek-chat";
    private static final int DEFAULT_TIMEOUT_MS = 180000;

    @Value("${deepseek.api.url:https://api.deepseek.com/v1/chat/completions}")
    private String defaultDeepSeekUrl;

    @Value("${deepseek.api.key:}")
    private String defaultDeepSeekKey;

    private final CampusAiRuntimeService campusAiRuntimeService;

    public CampusAiChatServiceImpl(CampusAiRuntimeService campusAiRuntimeService) {
        this.campusAiRuntimeService = campusAiRuntimeService;
    }

    @Override
    public CampusAiChatResponse chat(CampusAiChatRequest request) {
        return execute(request, false, null);
    }

    @Override
    public CampusAiChatResponse chatStreaming(CampusAiChatRequest request, StringBuilder streamOutput) {
        return execute(request, true, streamOutput);
    }

    private CampusAiChatResponse execute(CampusAiChatRequest request,
                                         boolean stream,
                                         StringBuilder streamOutput) {
        CampusAiChatRequest safeRequest = request == null ? new CampusAiChatRequest() : request;
        String featureCode = StringUtils.defaultIfBlank(safeRequest.getFeatureCode(), DEFAULT_FEATURE_CODE);
        CampusAiRuntimeConfig config = campusAiRuntimeService.resolveFeature(featureCode,
                DEFAULT_PROVIDER, DEFAULT_MODEL, defaultDeepSeekUrl, "DEEPSEEK_API_KEY", DEFAULT_TIMEOUT_MS);
        String credential = campusAiRuntimeService.resolveCredential(config.getCredentialRef(), defaultDeepSeekKey);
        config.setCredentialValue(credential);

        Date requestTime = new Date();
        long startMillis = System.currentTimeMillis();
        Integer httpStatus = null;
        String responseSnapshot = null;
        try {
            validateConfig(config);
            JSONObject body = buildRequestBody(safeRequest, config, stream);
            HttpURLConnection conn = createConnection(config, credential);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            if (stream) {
                conn.setRequestProperty("Accept", "text/event-stream");
            }
            conn.setDoOutput(true);

            byte[] bodyBytes = body.toJSONString().getBytes(StandardCharsets.UTF_8);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(bodyBytes);
                os.flush();
            }

            httpStatus = conn.getResponseCode();
            if (httpStatus < 200 || httpStatus >= 300) {
                String errorBody = readStream(conn.getErrorStream(), false);
                responseSnapshot = sanitize(errorBody, credential);
                throw new IllegalStateException("AI API HTTP error: status=" + httpStatus + ", body=" + responseSnapshot);
            }

            CampusAiChatResponse response = stream
                    ? readStreamingResponse(conn.getInputStream(), streamOutput)
                    : readNonStreamingResponse(conn.getInputStream());
            response.setHttpStatus(httpStatus);
            responseSnapshot = response.getContent();
            recordLog(config, requestTime, elapsedMillis(startMillis), "success", httpStatus,
                    null, null, safeRequest, response, responseSnapshot);
            conn.disconnect();
            return response;
        } catch (Exception ex) {
            String message = sanitize(ex.getMessage(), credential);
            recordLog(config, requestTime, elapsedMillis(startMillis), "failed", httpStatus,
                    classifyErrorType(message), message, safeRequest, null, responseSnapshot);
            throw new IllegalStateException(message, ex);
        }
    }

    private void validateConfig(CampusAiRuntimeConfig config) {
        if (config == null || !config.isFeatureEnabled()) {
            throw new IllegalStateException("AI功能已停用");
        }
        if (!config.isProviderEnabled()) {
            throw new IllegalStateException("AI供应商已停用");
        }
        if (!config.isModelEnabled()) {
            throw new IllegalStateException("AI模型已停用");
        }
        if (StringUtils.isBlank(config.getEndpoint())) {
            throw new IllegalStateException("AI接入点未配置");
        }
        if (StringUtils.isBlank(config.getCredentialValue())) {
            throw new IllegalStateException("AI密钥未配置：" + StringUtils.defaultString(config.getCredentialRef()));
        }
    }

    private JSONObject buildRequestBody(CampusAiChatRequest request,
                                        CampusAiRuntimeConfig config,
                                        boolean stream) {
        JSONObject body = new JSONObject();
        body.put("model", StringUtils.defaultIfBlank(config.getModelCode(), DEFAULT_MODEL));

        JSONArray messages = new JSONArray();
        if (StringUtils.isNotBlank(request.getSystemPrompt())) {
            JSONObject system = new JSONObject();
            system.put("role", "system");
            system.put("content", request.getSystemPrompt());
            messages.add(system);
        }
        JSONObject user = new JSONObject();
        user.put("role", "user");
        user.put("content", StringUtils.defaultString(request.getUserPrompt()));
        messages.add(user);

        BigDecimal temperature = request.getTemperature() == null ? config.getTemperature() : request.getTemperature();
        Integer maxTokens = request.getMaxTokens() == null ? config.getMaxTokens() : request.getMaxTokens();
        body.put("messages", messages);
        body.put("stream", stream);
        body.put("max_tokens", maxTokens == null ? 4096 : maxTokens);
        body.put("temperature", temperature == null ? new BigDecimal("0.20") : temperature);
        return body;
    }

    private HttpURLConnection createConnection(CampusAiRuntimeConfig config,
                                               String credential) throws Exception {
        URL url = new URL(config.getEndpoint());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        int timeout = config.getTimeoutMs() == null ? DEFAULT_TIMEOUT_MS : Math.max(config.getTimeoutMs(), 1000);
        conn.setConnectTimeout(timeout);
        conn.setReadTimeout(timeout);
        conn.setRequestProperty("Authorization", "Bearer " + credential);
        conn.setRequestProperty("User-Agent", "Stonedt-Campus-AI/1.0");
        return conn;
    }

    private CampusAiChatResponse readNonStreamingResponse(InputStream stream) throws Exception {
        String responseBody = readStream(stream, false);
        JSONObject responseJson = JSON.parseObject(responseBody);
        JSONArray choices = responseJson.getJSONArray("choices");
        if (choices == null || choices.isEmpty()) {
            throw new IllegalStateException("AI响应未返回 choices");
        }
        JSONObject firstChoice = choices.getJSONObject(0);
        JSONObject message = firstChoice.getJSONObject("message");
        if (message == null) {
            throw new IllegalStateException("AI响应缺少 message 字段");
        }
        CampusAiChatResponse response = new CampusAiChatResponse();
        response.setContent(message.getString("content"));
        JSONObject usage = responseJson.getJSONObject("usage");
        if (usage != null) {
            response.setPromptTokens(usage.getInteger("prompt_tokens"));
            response.setCompletionTokens(usage.getInteger("completion_tokens"));
            response.setTotalTokens(usage.getInteger("total_tokens"));
        }
        return response;
    }

    private CampusAiChatResponse readStreamingResponse(InputStream stream,
                                                       StringBuilder streamOutput) throws Exception {
        StringBuilder fullContent = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.length() == 0 || !line.startsWith("data: ")) {
                    continue;
                }
                String data = line.substring(6);
                if ("[DONE]".equals(data)) {
                    break;
                }
                JSONObject chunk = JSON.parseObject(data);
                JSONArray choices = chunk.getJSONArray("choices");
                if (choices == null || choices.isEmpty()) {
                    continue;
                }
                JSONObject firstChoice = choices.getJSONObject(0);
                JSONObject delta = firstChoice.getJSONObject("delta");
                if (delta == null) {
                    continue;
                }
                String content = delta.getString("content");
                if (content != null) {
                    fullContent.append(content);
                    if (streamOutput != null) {
                        streamOutput.append(content);
                    }
                }
            }
        }
        CampusAiChatResponse response = new CampusAiChatResponse();
        response.setContent(fullContent.toString());
        return response;
    }

    private void recordLog(CampusAiRuntimeConfig config,
                           Date requestTime,
                           Long durationMs,
                           String status,
                           Integer httpStatus,
                           String errorType,
                           String errorMessage,
                           CampusAiChatRequest request,
                           CampusAiChatResponse response,
                           String responseSnapshot) {
        CampusAiCallLog log = new CampusAiCallLog();
        log.setCallId(SnowflakeUtil.getId());
        log.setFeatureCode(config == null ? null : config.getFeatureCode());
        log.setProviderCode(config == null ? null : config.getProviderCode());
        log.setModelCode(config == null ? null : config.getModelCode());
        log.setEndpoint(config == null ? null : StringUtils.left(config.getEndpoint(), 512));
        log.setRequestTime(requestTime);
        log.setDurationMs(durationMs);
        log.setCallStatus(status);
        log.setHttpStatus(httpStatus);
        log.setErrorType(errorType);
        log.setErrorMessage(StringUtils.left(errorMessage, 2048));
        log.setPromptTokens(response == null ? null : response.getPromptTokens());
        log.setCompletionTokens(response == null ? null : response.getCompletionTokens());
        log.setTotalTokens(response == null ? null : response.getTotalTokens());
        log.setQuotaUnits(1);
        log.setRequestSnapshot(buildRequestSnapshot(config, request));
        log.setResponseSnapshot(StringUtils.left(sanitize(responseSnapshot, config == null ? null : config.getCredentialValue()), 2000));
        log.setDeleted(0);
        campusAiRuntimeService.recordCall(log);
    }

    private String buildRequestSnapshot(CampusAiRuntimeConfig config,
                                        CampusAiChatRequest request) {
        JSONObject snapshot = new JSONObject();
        snapshot.put("featureCode", config == null ? null : config.getFeatureCode());
        snapshot.put("providerCode", config == null ? null : config.getProviderCode());
        snapshot.put("modelCode", config == null ? null : config.getModelCode());
        snapshot.put("promptChars", request == null ? 0 : StringUtils.length(request.getUserPrompt()));
        if (config != null && config.isLogPrompt() && request != null) {
            snapshot.put("systemPrompt", StringUtils.left(request.getSystemPrompt(), 1000));
            snapshot.put("userPrompt", StringUtils.left(request.getUserPrompt(), 2000));
        }
        return snapshot.toJSONString();
    }

    private String readStream(InputStream stream, boolean keepNewLine) throws Exception {
        if (stream == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                if (keepNewLine) {
                    sb.append('\n');
                }
            }
        }
        return sb.toString();
    }

    private String classifyErrorType(String message) {
        String lower = StringUtils.defaultString(message).toLowerCase();
        if (lower.contains("密钥") || lower.contains("credential") || lower.contains("key")) {
            return "credential_missing";
        }
        if (lower.contains("timeout") || lower.contains("timed out")) {
            return "timeout";
        }
        if (lower.contains("http error") || lower.contains("status=")) {
            return "http_error";
        }
        return "request_failed";
    }

    private String sanitize(String value, String credential) {
        String sanitized = StringUtils.defaultString(value);
        if (StringUtils.isNotBlank(credential)) {
            sanitized = sanitized.replace(credential.trim(), "[REDACTED]");
        }
        sanitized = sanitized.replaceAll("(?i)(authorization\\s*[:=]\\s*bearer\\s+)[^\\s,;}]+", "$1[REDACTED]");
        sanitized = sanitized.replaceAll("(?i)(bearer\\s+)[A-Za-z0-9._\\-]+", "$1[REDACTED]");
        return sanitized;
    }

    private Long elapsedMillis(long startMillis) {
        return Math.max(System.currentTimeMillis() - startMillis, 0L);
    }
}
