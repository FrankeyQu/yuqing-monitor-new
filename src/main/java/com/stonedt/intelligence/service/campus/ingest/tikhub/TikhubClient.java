package com.stonedt.intelligence.service.campus.ingest.tikhub;

import com.alibaba.fastjson.JSONObject;
import com.stonedt.intelligence.service.campus.ingest.CampusIngestFetchRequest;
import com.stonedt.intelligence.service.campus.ingest.governance.CampusIngestApiCallLogger;
import com.stonedt.intelligence.service.campus.ai.CampusAiRuntimeService;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
public class TikhubClient {

    private static final String DEFAULT_BASE_URL = "https://api.tikhub.io";
    private static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient okHttpClient;
    private final TikhubCredentialResolver credentialResolver;
    private final CampusIngestApiCallLogger apiCallLogger;
    private final CampusAiRuntimeService campusAiRuntimeService;

    @Value("${tikhub.base-url:${TIKHUB_BASE_URL:https://api.tikhub.io}}")
    private String baseUrl;

    @Autowired
    public TikhubClient(OkHttpClient okHttpClient,
                        TikhubCredentialResolver credentialResolver,
                        CampusIngestApiCallLogger apiCallLogger,
                        CampusAiRuntimeService campusAiRuntimeService) {
        this.okHttpClient = okHttpClient;
        this.credentialResolver = credentialResolver;
        this.apiCallLogger = apiCallLogger;
        this.campusAiRuntimeService = campusAiRuntimeService;
    }

    public TikhubClient(OkHttpClient okHttpClient,
                        TikhubCredentialResolver credentialResolver,
                        CampusIngestApiCallLogger apiCallLogger) {
        this.okHttpClient = okHttpClient;
        this.credentialResolver = credentialResolver;
        this.apiCallLogger = apiCallLogger;
        this.campusAiRuntimeService = null;
    }

    public String fetch(TikhubEndpointDefinition endpointDefinition,
                        TikhubFetchConfig fetchConfig,
                        CampusIngestFetchRequest fetchRequest) {
        return execute(endpointDefinition, fetchConfig, fetchRequest, null);
    }

    public String fetchDetail(TikhubEndpointDefinition endpointDefinition,
                              TikhubFetchConfig fetchConfig,
                              CampusIngestFetchRequest fetchRequest,
                              String noteId) {
        return execute(endpointDefinition, fetchConfig, fetchRequest, noteId);
    }

    private String execute(TikhubEndpointDefinition endpointDefinition,
                           TikhubFetchConfig fetchConfig,
                           CampusIngestFetchRequest fetchRequest,
                           String detailNoteId) {
        Date requestTime = new Date();
        long startMillis = System.currentTimeMillis();
        String credential = null;
        String callStatus = CampusIngestApiCallLogger.STATUS_FAILED;
        Integer httpStatus = null;
        String errorType = null;
        String errorMessage = null;
        int costUnits = 0;
        boolean consumeQuota = false;

        try {
            credential = credentialResolver.resolve(fetchConfig);
            Request request = StringUtils.isBlank(detailNoteId)
                    ? buildRequest(endpointDefinition, fetchConfig, credential)
                    : buildDetailRequest(endpointDefinition, credential, detailNoteId);

            OkHttpClient requestClient = okHttpClient.newBuilder()
                    .callTimeout(fetchConfig.getTimeoutMs(), TimeUnit.MILLISECONDS)
                    .build();

            consumeQuota = true;
            costUnits = 1;
            Response response = requestClient.newCall(request).execute();
            try {
                httpStatus = response.code();
                String responseBody = response.body() == null ? "" : response.body().string();
                if (!response.isSuccessful()) {
                    errorType = "http_error";
                    errorMessage = "TikHub request failed: status="
                            + response.code()
                            + ", body="
                            + TikhubSanitizer.sanitizeError(responseBody, credential);
                    throw new TikhubIngestException(errorMessage);
                }
                callStatus = CampusIngestApiCallLogger.STATUS_SUCCESS;
                return responseBody;
            } finally {
                response.close();
            }
        } catch (TikhubIngestException ex) {
            errorType = errorType == null ? classifyErrorType(ex.getMessage()) : errorType;
            errorMessage = TikhubSanitizer.sanitizeError(ex.getMessage(), credential);
            throw ex;
        } catch (IOException ex) {
            errorType = "request_failed";
            errorMessage = "TikHub request failed: " + TikhubSanitizer.sanitizeError(ex.getMessage(), credential);
            throw new TikhubIngestException(errorMessage);
        } finally {
            if (apiCallLogger != null) {
                apiCallLogger.recordTikhubCall(fetchRequest, endpointDefinition, fetchConfig,
                        requestTime, elapsedMillis(startMillis), callStatus, httpStatus,
                        errorType, errorMessage, costUnits, consumeQuota);
            }
        }
    }

    private Request buildRequest(TikhubEndpointDefinition endpointDefinition,
                                 TikhubFetchConfig fetchConfig,
                                 String credential) {
        Request.Builder builder = new Request.Builder()
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", "Bearer " + credential);
        String method = endpointDefinition.getMethod();
        if ("POST".equals(method)) {
            builder.url(safeBaseUrl() + endpointDefinition.getPath())
                    .post(RequestBody.create(buildPostBody(endpointDefinition, fetchConfig).toJSONString(), JSON_MEDIA_TYPE))
                    .addHeader("Content-Type", "application/json");
            return builder.build();
        }
        if ("GET".equals(method)) {
            builder.url(buildGetUrl(endpointDefinition, fetchConfig)).get();
            return builder.build();
        }
        throw new TikhubIngestException("TikHub endpoint method is not supported: " + method);
    }

    private Request buildDetailRequest(TikhubEndpointDefinition endpointDefinition,
                                       String credential,
                                       String noteId) {
        if (!"GET".equals(endpointDefinition.getMethod())) {
            throw new TikhubIngestException("TikHub detail endpoint method is not supported: "
                    + endpointDefinition.getMethod());
        }
        return new Request.Builder()
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", "Bearer " + credential)
                .url(buildDetailGetUrl(endpointDefinition, noteId))
                .get()
                .build();
    }

    private String buildGetUrl(TikhubEndpointDefinition endpointDefinition, TikhubFetchConfig fetchConfig) {
        HttpUrl base = HttpUrl.parse(safeBaseUrl() + endpointDefinition.getPath());
        if (base == null) {
            throw new TikhubIngestException("TikHub base URL is invalid");
        }
        HttpUrl.Builder builder = base.newBuilder();
        String endpointKey = endpointDefinition.getEndpointKey();
        if ("weibo_search_all".equals(endpointKey)) {
            builder.addQueryParameter("query", fetchConfig.getQuery())
                    .addQueryParameter("page", String.valueOf(fetchConfig.getPage()))
                    .addQueryParameter("search_type", StringUtils.defaultIfBlank(fetchConfig.getSearchType(), "1"));
            return builder.build().toString();
        }
        if ("xiaohongshu_search_notes".equals(endpointKey)) {
            builder.addQueryParameter("keyword", fetchConfig.getQuery())
                    .addQueryParameter("page", String.valueOf(fetchConfig.getPage()))
                    .addQueryParameter("sort_type", StringUtils.defaultIfBlank(fetchConfig.getSortType(), "general"))
                    .addQueryParameter("note_type", StringUtils.defaultIfBlank(fetchConfig.getContentType(), "不限"));
            return builder.build().toString();
        }
        if ("bilibili_search_by_type".equals(endpointKey)) {
            builder.addQueryParameter("keyword", fetchConfig.getQuery())
                    .addQueryParameter("search_type", StringUtils.defaultIfBlank(fetchConfig.getSearchType(), "video"))
                    .addQueryParameter("page", String.valueOf(fetchConfig.getPage()))
                    .addQueryParameter("page_size", String.valueOf(fetchConfig.getLimit()))
                    .addQueryParameter("order", normalizeBilibiliOrder(fetchConfig.getSortType()));
            return builder.build().toString();
        }
        if ("zhihu_article_search_v3".equals(endpointKey)) {
            builder.addQueryParameter("keyword", fetchConfig.getQuery())
                    .addQueryParameter("offset", String.valueOf((fetchConfig.getPage() - 1) * fetchConfig.getLimit()))
                    .addQueryParameter("limit", String.valueOf(fetchConfig.getLimit()))
                    .addQueryParameter("show_all_topics", "0")
                    .addQueryParameter("search_source", StringUtils.defaultIfBlank(fetchConfig.getSearchType(), "Normal"))
                    .addQueryParameter("search_hash_id", "")
                    .addQueryParameter("vertical", normalizeOptionalParam(fetchConfig.getContentType()))
                    .addQueryParameter("sort", normalizeOptionalParam(fetchConfig.getSortType()))
                    .addQueryParameter("time_interval", normalizeOptionalParam(fetchConfig.getPublishTime()))
                    .addQueryParameter("vertical_info", "");
            return builder.build().toString();
        }
        if ("wechat_mp_search_article".equals(endpointKey)) {
            builder.addQueryParameter("keyword", fetchConfig.getQuery())
                    .addQueryParameter("offset", String.valueOf((fetchConfig.getPage() - 1) * 20))
                    .addQueryParameter("sort_type", normalizeWechatSortType(fetchConfig.getSortType()));
            return builder.build().toString();
        }
        if ("kuaishou_search_comprehensive".equals(endpointKey)) {
            builder.addQueryParameter("keyword", fetchConfig.getQuery())
                    .addQueryParameter("sort_type", StringUtils.defaultIfBlank(fetchConfig.getSortType(), "all"))
                    .addQueryParameter("publish_time", StringUtils.defaultIfBlank(fetchConfig.getPublishTime(), "all"))
                    .addQueryParameter("duration", StringUtils.defaultIfBlank(fetchConfig.getFilterDuration(), "all"))
                    .addQueryParameter("search_scope", StringUtils.defaultIfBlank(fetchConfig.getSearchType(), "all"));
            if (fetchConfig.getCursor() > 0) {
                builder.addQueryParameter("pcursor", String.valueOf(fetchConfig.getCursor()));
            }
            return builder.build().toString();
        }
        if ("kuaishou_search_video_v2".equals(endpointKey)) {
            builder.addQueryParameter("keyword", fetchConfig.getQuery())
                    .addQueryParameter("page", String.valueOf(fetchConfig.getPage()));
            return builder.build().toString();
        }
        throw new TikhubIngestException("TikHub GET endpoint is not implemented: " + endpointKey);
    }

    private String buildDetailGetUrl(TikhubEndpointDefinition endpointDefinition, String detailId) {
        HttpUrl base = HttpUrl.parse(safeBaseUrl() + endpointDefinition.getPath());
        if (base == null) {
            throw new TikhubIngestException("TikHub base URL is invalid");
        }
        String endpointKey = endpointDefinition.getEndpointKey();
        if ("xiaohongshu_image_note_detail".equals(endpointKey)
                || "xiaohongshu_video_note_detail".equals(endpointKey)) {
            return base.newBuilder()
                    .addQueryParameter("note_id", StringUtils.trimToEmpty(detailId))
                    .build()
                    .toString();
        }
        if ("weibo_post_detail_v2".equals(endpointKey)) {
            return base.newBuilder()
                    .addQueryParameter("id", StringUtils.trimToEmpty(detailId))
                    .addQueryParameter("is_get_long_text", "true")
                    .build()
                    .toString();
        }
        if ("bilibili_video_detail".equals(endpointKey)) {
            return base.newBuilder()
                    .addQueryParameter("aid", StringUtils.trimToEmpty(detailId))
                    .build()
                    .toString();
        }
        throw new TikhubIngestException("TikHub detail endpoint is not implemented: " + endpointKey);
    }

    private String normalizeBilibiliOrder(String sortType) {
        String value = StringUtils.defaultIfBlank(sortType, "0").trim();
        return value.matches("\\d+") ? value : "0";
    }

    private String normalizeWechatSortType(String sortType) {
        String value = StringUtils.defaultString(sortType).trim();
        if (value.length() == 0 || "0".equals(value)) {
            return "_0";
        }
        return value;
    }

    private String normalizeOptionalParam(String value) {
        String normalized = StringUtils.defaultString(value).trim();
        if (normalized.length() == 0 || "0".equals(normalized) || "不限".equals(normalized) || "all".equalsIgnoreCase(normalized)) {
            return "";
        }
        return normalized;
    }

    private JSONObject buildPostBody(TikhubEndpointDefinition endpointDefinition, TikhubFetchConfig fetchConfig) {
        String endpointKey = endpointDefinition.getEndpointKey();
        if ("douyin_search_video_v2".equals(endpointKey)
                || "douyin_search_general_v5".equals(endpointKey)) {
            return buildDouyinSearchBody(fetchConfig);
        }
        throw new TikhubIngestException("TikHub POST endpoint is not implemented: " + endpointKey);
    }

    private String classifyErrorType(String message) {
        String lower = message == null ? "" : message.toLowerCase();
        if (lower.contains("credential") || lower.contains("key") || lower.contains("密钥")) {
            return "credential_missing";
        }
        if (lower.contains("timeout") || lower.contains("request failed")) {
            return "request_failed";
        }
        return "unknown";
    }

    private long elapsedMillis(long startMillis) {
        return Math.max(System.currentTimeMillis() - startMillis, 0L);
    }

    private JSONObject buildDouyinSearchBody(TikhubFetchConfig fetchConfig) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("keyword", fetchConfig.getQuery());
        requestBody.put("cursor", fetchConfig.getCursor());
        requestBody.put("sort_type", fetchConfig.getSortType());
        requestBody.put("publish_time", fetchConfig.getPublishTime());
        requestBody.put("filter_duration", fetchConfig.getFilterDuration());
        requestBody.put("content_type", fetchConfig.getContentType());
        requestBody.put("search_id", fetchConfig.getSearchId());
        requestBody.put("backtrace", fetchConfig.getBacktrace());
        return requestBody;
    }

    private String safeBaseUrl() {
        String runtimeBaseUrl = campusAiRuntimeService == null
                ? baseUrl
                : campusAiRuntimeService.resolveProviderBaseUrl("tikhub", baseUrl);
        String resolved = StringUtils.defaultIfBlank(runtimeBaseUrl, DEFAULT_BASE_URL).trim();
        if (resolved.endsWith("/")) {
            return resolved.substring(0, resolved.length() - 1);
        }
        return resolved;
    }
}
