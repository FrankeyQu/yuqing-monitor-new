package com.stonedt.intelligence.service.campus.ingest.baidu;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.stonedt.intelligence.service.campus.ingest.CampusIngestAdapter;
import com.stonedt.intelligence.service.campus.ingest.CampusIngestFetchRequest;
import com.stonedt.intelligence.service.campus.ingest.CampusIngestFetchResponse;
import com.stonedt.intelligence.service.campus.ingest.CampusIngestItem;
import com.stonedt.intelligence.service.campus.ingest.extract.ContentExtractionClient;
import com.stonedt.intelligence.service.campus.ingest.extract.ContentExtractionException;
import com.stonedt.intelligence.service.campus.ingest.extract.ContentExtractionRequest;
import com.stonedt.intelligence.service.campus.ingest.extract.ContentExtractionResult;
import com.stonedt.intelligence.service.campus.ingest.governance.CampusIngestApiCallLogger;
import com.stonedt.intelligence.service.campus.ai.CampusAiRuntimeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Component
public class BaiduIngestAdapter implements CampusIngestAdapter {

    private static final Logger logger = LoggerFactory.getLogger(BaiduIngestAdapter.class);

    private static final String DEFAULT_API_URL = "https://qianfan.baidubce.com/v2/ai_search/web_search";
    private static final String AI_PROVIDER_CODE = "baidu_qianfan";

    @Value("${baidu.api.url:" + DEFAULT_API_URL + "}")
    private String apiUrl;

    @Value("${baidu.api.key:}")
    private String springApiKey;

    private final BaiduIngestResponseMapper responseMapper;
    private final ContentExtractionClient contentExtractionClient;
    private final CampusIngestApiCallLogger apiCallLogger;
    private final CampusAiRuntimeService campusAiRuntimeService;

    @Autowired
    public BaiduIngestAdapter(BaiduIngestResponseMapper responseMapper,
                              ContentExtractionClient contentExtractionClient,
                              CampusIngestApiCallLogger apiCallLogger,
                              CampusAiRuntimeService campusAiRuntimeService) {
        this.responseMapper = responseMapper;
        this.contentExtractionClient = contentExtractionClient;
        this.apiCallLogger = apiCallLogger;
        this.campusAiRuntimeService = campusAiRuntimeService;
    }

    public BaiduIngestAdapter(BaiduIngestResponseMapper responseMapper,
                              ContentExtractionClient contentExtractionClient,
                              CampusIngestApiCallLogger apiCallLogger) {
        this.responseMapper = responseMapper;
        this.contentExtractionClient = contentExtractionClient;
        this.apiCallLogger = apiCallLogger;
        this.campusAiRuntimeService = null;
    }

    @Override
    public String adapterType() {
        return "baidu_search";
    }

    @Override
    public CampusIngestFetchResponse fetch(CampusIngestFetchRequest request) {
        try {
            BaiduIngestFetchConfig fetchConfig = BaiduIngestFetchConfig.fromRequest(request);

            String apiKey = resolveApiKey(fetchConfig);
            if (apiKey == null || apiKey.trim().isEmpty()) {
                return CampusIngestFetchResponse.empty("Baidu API key not configured");
            }

            String responseBody = callQianfanApi(fetchConfig, apiKey, request);
            List<CampusIngestItem> records = responseMapper.map(fetchConfig, responseBody);
            int readerSuccessCount = enhanceWithReader(fetchConfig, request, records);

            CampusIngestFetchResponse response = new CampusIngestFetchResponse();
            response.setRecords(records);
            response.setMessage("Baidu Qianfan fetch completed, records=" + records.size()
                    + ", readerSuccess=" + readerSuccessCount);
            return response;
        } catch (BaiduIngestException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw new BaiduIngestException("Baidu ingest failed: "
                    + BaiduIngestSanitizer.sanitizeError(ex.getMessage()));
        } catch (Exception ex) {
            throw new BaiduIngestException("Baidu ingest failed: "
                    + BaiduIngestSanitizer.sanitizeError(ex.getMessage()));
        }
    }

    private String resolveApiKey(BaiduIngestFetchConfig config) {
        String credentialRef = config.getCredentialRef();
        if (credentialRef != null && !credentialRef.trim().isEmpty()) {
            String envValue = System.getenv(credentialRef.trim());
            if (envValue != null && !envValue.trim().isEmpty()) {
                return envValue.trim();
            }
        }
        if (campusAiRuntimeService != null) {
            String providerCredentialRef = campusAiRuntimeService.resolveProviderCredentialRef(AI_PROVIDER_CODE,
                    BaiduIngestFetchConfig.DEFAULT_CREDENTIAL_REF);
            String credential = campusAiRuntimeService.resolveCredential(providerCredentialRef, springApiKey);
            if (credential != null && credential.trim().length() > 0) {
                return credential.trim();
            }
        }
        if (springApiKey != null && !springApiKey.trim().isEmpty()) {
            return springApiKey.trim();
        }
        return null;
    }

    private String callQianfanApi(BaiduIngestFetchConfig config,
                                  String apiKey,
                                  CampusIngestFetchRequest request) throws Exception {
        Date requestTime = new Date();
        long startMillis = System.currentTimeMillis();
        String callStatus = CampusIngestApiCallLogger.STATUS_FAILED;
        Integer httpStatus = null;
        String errorType = null;
        String errorMessage = null;
        String resolvedApiUrl = campusAiRuntimeService == null
                ? apiUrl
                : campusAiRuntimeService.resolveProviderBaseUrl(AI_PROVIDER_CODE, apiUrl);
        int timeoutMs = campusAiRuntimeService == null
                ? config.getTimeoutMs()
                : campusAiRuntimeService.resolveProviderTimeoutMs(AI_PROVIDER_CODE, config.getTimeoutMs());
        URL url = new URL(resolvedApiUrl);
        try {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(timeoutMs);
            conn.setReadTimeout(timeoutMs);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("X-Appbuilder-Authorization", "Bearer " + apiKey);
            conn.setDoOutput(true);

            JSONObject body = new JSONObject();
            JSONArray messages = new JSONArray();
            JSONObject msg = new JSONObject();
            msg.put("role", "user");
            msg.put("content", config.getQuery());
            messages.add(msg);
            body.put("messages", messages);

            body.put("search_source", "baidu_search_v2");

            JSONArray resourceFilter = new JSONArray();
            for (String type : config.getResourceTypes()) {
                JSONObject typeFilter = new JSONObject();
                typeFilter.put("type", type);
                typeFilter.put("top_k", config.getTopK());
                resourceFilter.add(typeFilter);
            }
            body.put("resource_type_filter", resourceFilter);

            byte[] bodyBytes = body.toJSONString().getBytes(StandardCharsets.UTF_8);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(bodyBytes);
                os.flush();
            }

            httpStatus = conn.getResponseCode();
            if (httpStatus != 200) {
                String errorBody = readStream(conn.getErrorStream());
                logger.error("Baidu Qianfan API returned status {}: {}", httpStatus,
                        BaiduIngestSanitizer.sanitizeText(errorBody));
                errorType = "http_error";
                errorMessage = "Baidu Qianfan API HTTP error: status=" + httpStatus;
                throw new BaiduIngestException(errorMessage);
            }

            String responseBody = readStream(conn.getInputStream());
            conn.disconnect();
            callStatus = CampusIngestApiCallLogger.STATUS_SUCCESS;
            return responseBody;
        } catch (BaiduIngestException ex) {
            errorType = errorType == null ? "request_failed" : errorType;
            errorMessage = BaiduIngestSanitizer.sanitizeError(ex.getMessage(), apiKey);
            throw ex;
        } catch (Exception ex) {
            errorType = "request_failed";
            errorMessage = "Baidu Qianfan API request failed: "
                    + BaiduIngestSanitizer.sanitizeError(ex.getMessage(), apiKey);
            throw ex;
        } finally {
            apiCallLogger.recordExternalCall(request,
                    BaiduIngestFetchConfig.PROVIDER,
                    "baidu_search_v2",
                    config.getCredentialRef(),
                    requestTime,
                    elapsedMillis(startMillis),
                    callStatus,
                    httpStatus,
                    errorType,
                    errorMessage,
                    1,
                    true);
        }
    }

    private int enhanceWithReader(BaiduIngestFetchConfig fetchConfig,
                                  CampusIngestFetchRequest request,
                                  List<CampusIngestItem> records) {
        if (!fetchConfig.isReaderEnabled() || records == null || records.isEmpty()) {
            return 0;
        }
        if (!contentExtractionClient.isEnabled()) {
            if (fetchConfig.isFallbackToSnippet()) {
                return 0;
            }
            throw new BaiduIngestException("Baidu readerEnabled=true but content extraction is disabled");
        }
        int maxCalls = Math.min(fetchConfig.getMaxReaderCalls(), records.size());
        int successCount = 0;
        for (int i = 0; i < maxCalls; i++) {
            CampusIngestItem item = records.get(i);
            if (item == null || item.getOriginalUrl() == null || item.getOriginalUrl().trim().length() == 0) {
                continue;
            }
            try {
                ContentExtractionResult result = contentExtractionClient.extract(readerRequest(item, fetchConfig, request));
                if (result != null && result.getContent() != null && result.getContent().trim().length() > 0) {
                    item.setContent(result.getContent());
                    if ((item.getTitle() == null || item.getTitle().trim().length() == 0)
                            && result.getTitle() != null && result.getTitle().trim().length() > 0) {
                        item.setTitle(result.getTitle());
                    }
                    item.setRawData(mergeReaderRawData(item.getRawData(), "success", null, result));
                    successCount++;
                }
            } catch (ContentExtractionException ex) {
                item.setRawData(mergeReaderRawData(item.getRawData(), "failed",
                        BaiduIngestSanitizer.sanitizeError(ex.getMessage()), null));
                if (!fetchConfig.isFallbackToSnippet()) {
                    throw new BaiduIngestException("Baidu Reader extraction failed: "
                            + BaiduIngestSanitizer.sanitizeError(ex.getMessage()), ex);
                }
            }
        }
        return successCount;
    }

    private ContentExtractionRequest readerRequest(CampusIngestItem item,
                                                   BaiduIngestFetchConfig fetchConfig,
                                                   CampusIngestFetchRequest request) {
        ContentExtractionRequest extractionRequest = new ContentExtractionRequest();
        extractionRequest.setUrl(item.getOriginalUrl());
        extractionRequest.setTimeoutMs(fetchConfig.getReaderTimeoutMs());
        extractionRequest.setEndpointKey("baidu_result_reader");
        extractionRequest.setIngestRequest(request);
        return extractionRequest;
    }

    private String mergeReaderRawData(String rawData,
                                      String status,
                                      String errorMessage,
                                      ContentExtractionResult result) {
        JSONObject raw;
        try {
            raw = JSON.parseObject(rawData);
            if (raw == null) {
                raw = new JSONObject();
            }
        } catch (RuntimeException ex) {
            raw = new JSONObject();
            raw.put("origin", rawData);
        }
        JSONObject reader = new JSONObject();
        reader.put("provider", "jina");
        reader.put("status", status);
        reader.put("errorMessage", errorMessage);
        reader.put("title", result == null ? null : result.getTitle());
        reader.put("sourceUrl", result == null ? null : result.getSourceUrl());
        raw.put("reader", reader);
        return raw.toJSONString();
    }

    private Long elapsedMillis(long startMillis) {
        return Math.max(System.currentTimeMillis() - startMillis, 0L);
    }

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
}
