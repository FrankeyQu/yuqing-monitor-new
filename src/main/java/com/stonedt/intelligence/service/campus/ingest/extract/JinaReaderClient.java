package com.stonedt.intelligence.service.campus.ingest.extract;

import com.stonedt.intelligence.service.campus.ingest.governance.CampusIngestApiCallLogger;
import com.stonedt.intelligence.service.campus.ingest.publicweb.PublicWebWhitelistValidator;
import com.stonedt.intelligence.service.campus.ai.CampusAiRuntimeService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JinaReaderClient implements ContentExtractionClient {

    public static final String PROVIDER = "jina_reader";
    private static final String DEFAULT_API_URL = "https://r.jina.ai";
    private static final int DEFAULT_TIMEOUT_MS = 15000;
    private static final int MAX_TIMEOUT_MS = 30000;
    private static final int DEFAULT_MAX_CONTENT_LENGTH = 20000;
    private static final int MAX_CONTENT_LENGTH = 120000;
    private static final String MARKDOWN_CONTENT_MARKER = "Markdown Content:";
    private static final String READER_USER_AGENT = "Mozilla/5.0";

    @Value("${content.extraction.enabled:false}")
    private String extractionEnabled;

    @Value("${content.extraction.provider:jina}")
    private String extractionProvider;

    @Value("${jina.reader.api.url:" + DEFAULT_API_URL + "}")
    private String apiUrl;

    @Value("${jina.reader.api.key:}")
    private String apiKey;

    @Value("${jina.reader.credential-ref:JINA_READER_API_KEY}")
    private String credentialRef;

    @Value("${jina.reader.timeout-ms:" + DEFAULT_TIMEOUT_MS + "}")
    private Integer defaultTimeoutMs;

    @Value("${jina.reader.max-content-length:" + DEFAULT_MAX_CONTENT_LENGTH + "}")
    private Integer maxContentLength;

    private final CampusIngestApiCallLogger apiCallLogger;
    private final CampusAiRuntimeService campusAiRuntimeService;

    @Autowired
    public JinaReaderClient(CampusIngestApiCallLogger apiCallLogger,
                            CampusAiRuntimeService campusAiRuntimeService) {
        this.apiCallLogger = apiCallLogger;
        this.campusAiRuntimeService = campusAiRuntimeService;
    }

    public JinaReaderClient(CampusIngestApiCallLogger apiCallLogger) {
        this.apiCallLogger = apiCallLogger;
        this.campusAiRuntimeService = null;
    }

    @Override
    public String provider() {
        return PROVIDER;
    }

    @Override
    public boolean isEnabled() {
        return "true".equalsIgnoreCase(StringUtils.defaultString(extractionEnabled))
                || "1".equals(StringUtils.defaultString(extractionEnabled));
    }

    public boolean isJinaProvider() {
        return "jina".equalsIgnoreCase(StringUtils.defaultIfBlank(extractionProvider, "jina"))
                || PROVIDER.equalsIgnoreCase(StringUtils.defaultIfBlank(extractionProvider, "jina"));
    }

    @Override
    public ContentExtractionResult extract(ContentExtractionRequest request) {
        if (!isEnabled() || !isJinaProvider()) {
            throw new ContentExtractionException("Jina Reader content extraction is disabled");
        }
        if (request == null || StringUtils.isBlank(request.getUrl())) {
            throw new ContentExtractionException("Jina Reader target URL is required");
        }
        String targetUrl = request.getUrl().trim();
        try {
            PublicWebWhitelistValidator.validateHttpUrl(targetUrl);
        } catch (RuntimeException ex) {
            throw new ContentExtractionException("Jina Reader target URL is not allowed: " + ex.getMessage(), ex);
        }

        Date requestTime = new Date();
        long startMillis = System.currentTimeMillis();
        String callStatus = CampusIngestApiCallLogger.STATUS_FAILED;
        Integer httpStatus = null;
        String errorType = null;
        String errorMessage = null;
        boolean consumeQuota = false;
        String resolvedApiKey = resolveApiKey();
        String resolvedCredentialRef = resolveCredentialRef();
        try {
            URL url = new URL(buildReaderUrl(targetUrl));
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(resolveTimeout(request.getTimeoutMs()));
            conn.setReadTimeout(resolveTimeout(request.getTimeoutMs()));
            conn.setRequestProperty("Accept", "text/plain; charset=UTF-8");
            conn.setRequestProperty("User-Agent", READER_USER_AGENT);
            conn.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");
            if (StringUtils.isNotBlank(resolvedApiKey)) {
                conn.setRequestProperty("Authorization", "Bearer " + resolvedApiKey.trim());
            }
            consumeQuota = true;
            httpStatus = conn.getResponseCode();
            String body = readStream(httpStatus >= 200 && httpStatus < 300 ? conn.getInputStream() : conn.getErrorStream());
            if (httpStatus < 200 || httpStatus >= 300) {
                errorType = "http_error";
                errorMessage = "Jina Reader HTTP error: status=" + httpStatus
                        + ", body=" + sanitize(body, resolvedApiKey);
                throw new ContentExtractionException(errorMessage);
            }
            ContentExtractionResult result = parse(targetUrl, body);
            if (StringUtils.isBlank(result.getContent())) {
                errorType = "empty_content";
                errorMessage = "Jina Reader returned empty content";
                throw new ContentExtractionException(errorMessage);
            }
            callStatus = CampusIngestApiCallLogger.STATUS_SUCCESS;
            return result;
        } catch (ContentExtractionException ex) {
            errorType = StringUtils.defaultIfBlank(errorType, "request_failed");
            errorMessage = sanitize(ex.getMessage(), resolvedApiKey);
            throw ex;
        } catch (Exception ex) {
            errorType = "request_failed";
            errorMessage = "Jina Reader request failed: " + sanitize(ex.getMessage(), resolvedApiKey);
            throw new ContentExtractionException(errorMessage, ex);
        } finally {
            apiCallLogger.recordExternalCall(request == null ? null : request.getIngestRequest(),
                    PROVIDER,
                    StringUtils.defaultIfBlank(request == null ? null : request.getEndpointKey(), "reader"),
                    StringUtils.defaultIfBlank(resolvedCredentialRef, "JINA_READER_API_KEY"),
                    requestTime,
                    elapsedMillis(startMillis),
                    callStatus,
                    httpStatus,
                    errorType,
                    errorMessage,
                    1,
                    consumeQuota);
        }
    }

    ContentExtractionResult parse(String sourceUrl, String responseBody) {
        String raw = StringUtils.trimToEmpty(responseBody);
        ContentExtractionResult result = new ContentExtractionResult();
        result.setProvider(PROVIDER);
        result.setSourceUrl(sourceUrl);
        result.setTitle(extractTitle(raw));
        result.setContent(limitContent(cleanMarkdownContent(raw)));
        return result;
    }

    private String buildReaderUrl(String targetUrl) {
        String resolvedBase = campusAiRuntimeService == null
                ? apiUrl
                : campusAiRuntimeService.resolveProviderBaseUrl(PROVIDER, apiUrl);
        String base = StringUtils.defaultIfBlank(resolvedBase, DEFAULT_API_URL).trim();
        while (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base + "/" + targetUrl;
    }

    private String extractTitle(String raw) {
        if (StringUtils.isBlank(raw)) {
            return null;
        }
        String[] lines = raw.split("\\r?\\n");
        for (String line : lines) {
            String trimmed = StringUtils.trimToEmpty(line);
            if (trimmed.startsWith("Title:")) {
                return StringUtils.trimToNull(trimmed.substring("Title:".length()));
            }
            if (trimmed.length() > 0 && !trimmed.startsWith("URL Source:")) {
                break;
            }
        }
        return null;
    }

    private String cleanMarkdownContent(String raw) {
        String text = StringUtils.trimToEmpty(raw);
        int markerIndex = text.indexOf(MARKDOWN_CONTENT_MARKER);
        if (markerIndex >= 0) {
            return StringUtils.trimToEmpty(text.substring(markerIndex + MARKDOWN_CONTENT_MARKER.length()));
        }
        return text;
    }

    private String limitContent(String content) {
        String normalized = StringUtils.trimToNull(content);
        if (normalized == null) {
            return null;
        }
        int limit = resolveMaxContentLength();
        return normalized.length() <= limit ? normalized : normalized.substring(0, limit);
    }

    private int resolveTimeout(int requestTimeoutMs) {
        int configuredDefault = defaultTimeoutMs == null ? DEFAULT_TIMEOUT_MS : defaultTimeoutMs;
        int providerDefault = campusAiRuntimeService == null
                ? configuredDefault
                : campusAiRuntimeService.resolveProviderTimeoutMs(PROVIDER, configuredDefault);
        int timeout = requestTimeoutMs > 0 ? requestTimeoutMs : providerDefault;
        return Math.max(1000, Math.min(timeout, MAX_TIMEOUT_MS));
    }

    private String resolveCredentialRef() {
        if (campusAiRuntimeService == null) {
            return credentialRef;
        }
        return campusAiRuntimeService.resolveProviderCredentialRef(PROVIDER, credentialRef);
    }

    private String resolveApiKey() {
        String resolvedCredentialRef = resolveCredentialRef();
        if (campusAiRuntimeService == null) {
            return StringUtils.trimToNull(apiKey);
        }
        return campusAiRuntimeService.resolveCredential(resolvedCredentialRef, apiKey);
    }

    private int resolveMaxContentLength() {
        int configured = maxContentLength == null ? DEFAULT_MAX_CONTENT_LENGTH : maxContentLength;
        return Math.max(1000, Math.min(configured, MAX_CONTENT_LENGTH));
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
                sb.append(line).append('\n');
            }
        }
        return sb.toString();
    }

    private Long elapsedMillis(long startMillis) {
        return Math.max(System.currentTimeMillis() - startMillis, 0L);
    }

    private String sanitize(String value, String resolvedApiKey) {
        String sanitized = StringUtils.defaultString(value);
        if (StringUtils.isNotBlank(apiKey)) {
            sanitized = sanitized.replace(apiKey.trim(), "[REDACTED]");
        }
        if (StringUtils.isNotBlank(resolvedApiKey)) {
            sanitized = sanitized.replace(resolvedApiKey.trim(), "[REDACTED]");
        }
        sanitized = sanitized.replaceAll("(?i)(authorization\\s*[:=]\\s*bearer\\s+)[^\\s,;}]+", "$1[REDACTED]");
        sanitized = sanitized.replaceAll("(?i)(bearer\\s+)[A-Za-z0-9._\\-]+", "$1[REDACTED]");
        return StringUtils.left(sanitized, 500);
    }
}
