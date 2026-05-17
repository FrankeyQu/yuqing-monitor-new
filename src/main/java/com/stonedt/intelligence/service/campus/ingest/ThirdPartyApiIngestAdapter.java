package com.stonedt.intelligence.service.campus.ingest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.stonedt.intelligence.service.campus.ingest.tikhub.TikhubClient;
import com.stonedt.intelligence.service.campus.ingest.tikhub.TikhubEndpointDefinition;
import com.stonedt.intelligence.service.campus.ingest.tikhub.TikhubEndpointRegistry;
import com.stonedt.intelligence.service.campus.ingest.tikhub.TikhubFetchConfig;
import com.stonedt.intelligence.service.campus.ingest.tikhub.TikhubIngestException;
import com.stonedt.intelligence.service.campus.ingest.tikhub.TikhubResponseMapper;
import com.stonedt.intelligence.service.campus.ingest.tikhub.TikhubSanitizer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ThirdPartyApiIngestAdapter implements CampusIngestAdapter {

    private static final String XIAOHONGSHU_SEARCH_NOTES = "xiaohongshu_search_notes";
    private static final String XIAOHONGSHU_IMAGE_DETAIL = "xiaohongshu_image_note_detail";
    private static final String XIAOHONGSHU_VIDEO_DETAIL = "xiaohongshu_video_note_detail";
    private static final String WEIBO_SEARCH_ALL = "weibo_search_all";
    private static final String WEIBO_POST_DETAIL = "weibo_post_detail_v2";
    private static final String BILIBILI_SEARCH_BY_TYPE = "bilibili_search_by_type";
    private static final String BILIBILI_VIDEO_DETAIL = "bilibili_video_detail";
    private static final String WECHAT_MP_SEARCH_ARTICLE = "wechat_mp_search_article";
    private static final int WECHAT_SEARCH_MAX_ATTEMPTS = 3;

    private final TikhubEndpointRegistry tikhubEndpointRegistry;
    private final TikhubClient tikhubClient;
    private final TikhubResponseMapper tikhubResponseMapper;

    public ThirdPartyApiIngestAdapter(TikhubEndpointRegistry tikhubEndpointRegistry,
                                      TikhubClient tikhubClient,
                                      TikhubResponseMapper tikhubResponseMapper) {
        this.tikhubEndpointRegistry = tikhubEndpointRegistry;
        this.tikhubClient = tikhubClient;
        this.tikhubResponseMapper = tikhubResponseMapper;
    }

    @Override
    public String adapterType() {
        return "third_party_api";
    }

    @Override
    public CampusIngestFetchResponse fetch(CampusIngestFetchRequest request) {
        if (!TikhubFetchConfig.isTikhubProvider(request)) {
            return CampusIngestFetchResponse.unsupported("third_party_api adapter only supports provider=tikhub in Batch22");
        }

        try {
            TikhubFetchConfig fetchConfig = TikhubFetchConfig.fromRequest(request);
            TikhubEndpointDefinition endpointDefinition = tikhubEndpointRegistry.require(fetchConfig.getEndpointKey());
            if (!endpointDefinition.isImplemented()) {
                return CampusIngestFetchResponse.unsupported("TikHub endpoint is not supported in Batch22: "
                        + TikhubSanitizer.sanitizeText(fetchConfig.getEndpointKey()));
            }

            String responseBody = fetchSearchResponse(endpointDefinition, fetchConfig, request);
            CampusIngestFetchResponse response = new CampusIngestFetchResponse();
            List<CampusIngestItem> records = tikhubResponseMapper.map(endpointDefinition, fetchConfig, responseBody);
            records = enhanceTikhubDetails(endpointDefinition, fetchConfig, request, records);
            response.setRecords(records);
            response.setMessage(tikhubFetchMessage(endpointDefinition, response.getRecords().size()));
            return response;
        } catch (TikhubIngestException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw new TikhubIngestException("TikHub ingest failed: " + TikhubSanitizer.sanitizeError(ex.getMessage()));
        }
    }

    private String fetchSearchResponse(TikhubEndpointDefinition endpointDefinition,
                                       TikhubFetchConfig fetchConfig,
                                       CampusIngestFetchRequest request) {
        if (!WECHAT_MP_SEARCH_ARTICLE.equals(endpointDefinition.getEndpointKey())) {
            return tikhubClient.fetch(endpointDefinition, fetchConfig, request);
        }
        TikhubIngestException lastError = null;
        for (int attempt = 1; attempt <= WECHAT_SEARCH_MAX_ATTEMPTS; attempt++) {
            try {
                return tikhubClient.fetch(endpointDefinition, fetchConfig, request);
            } catch (TikhubIngestException ex) {
                lastError = ex;
                if (attempt >= WECHAT_SEARCH_MAX_ATTEMPTS || !isRetryableWechatSearchError(ex)) {
                    throw ex;
                }
                sleepBeforeWechatRetry(attempt);
            }
        }
        throw lastError == null ? new TikhubIngestException("TikHub WeChat search failed") : lastError;
    }

    private boolean isRetryableWechatSearchError(TikhubIngestException ex) {
        String message = StringUtils.defaultString(ex.getMessage());
        return message.contains("Please retry")
                || message.contains("请求失败，请重试")
                || message.contains("status=400")
                || message.contains("request_failed");
    }

    private void sleepBeforeWechatRetry(int attempt) {
        try {
            Thread.sleep(250L * attempt);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new TikhubIngestException("TikHub WeChat retry interrupted");
        }
    }

    private List<CampusIngestItem> enhanceXiaohongshuDetails(TikhubEndpointDefinition endpointDefinition,
                                                             TikhubFetchConfig fetchConfig,
                                                             CampusIngestFetchRequest request,
                                                             List<CampusIngestItem> records) {
        if (!XIAOHONGSHU_SEARCH_NOTES.equals(endpointDefinition.getEndpointKey())
                || !fetchConfig.isDetailEnabled()
                || records == null
                || records.isEmpty()) {
            return records;
        }
        int remaining = Math.min(fetchConfig.getMaxDetailCalls(), records.size());
        if (remaining <= 0) {
            return records;
        }
        List<CampusIngestItem> enhanced = new ArrayList<>(records.size());
        for (CampusIngestItem record : records) {
            CampusIngestItem next = record;
            if (remaining > 0 && record != null && StringUtils.isNotBlank(record.getExternalId())) {
                try {
                    remaining--;
                    TikhubEndpointDefinition detailEndpoint = tikhubEndpointRegistry.require(resolveXiaohongshuDetailEndpoint(record));
                    String detailBody = tikhubClient.fetchDetail(detailEndpoint, fetchConfig, request, record.getExternalId());
                    next = tikhubResponseMapper.mapXiaohongshuDetail(detailEndpoint, fetchConfig, detailBody, record);
                } catch (RuntimeException ignored) {
                    next = markDetailFailure(record, resolveXiaohongshuDetailEndpoint(record), ignored);
                }
            }
            enhanced.add(next);
        }
        return enhanced;
    }

    private List<CampusIngestItem> enhanceTikhubDetails(TikhubEndpointDefinition endpointDefinition,
                                                        TikhubFetchConfig fetchConfig,
                                                        CampusIngestFetchRequest request,
                                                        List<CampusIngestItem> records) {
        records = enhanceXiaohongshuDetails(endpointDefinition, fetchConfig, request, records);
        records = enhanceGenericDetails(endpointDefinition, fetchConfig, request, records,
                WEIBO_SEARCH_ALL, WEIBO_POST_DETAIL, "id");
        records = enhanceGenericDetails(endpointDefinition, fetchConfig, request, records,
                BILIBILI_SEARCH_BY_TYPE, BILIBILI_VIDEO_DETAIL, "aid");
        return records;
    }

    private List<CampusIngestItem> enhanceGenericDetails(TikhubEndpointDefinition endpointDefinition,
                                                         TikhubFetchConfig fetchConfig,
                                                         CampusIngestFetchRequest request,
                                                         List<CampusIngestItem> records,
                                                         String searchEndpointKey,
                                                         String detailEndpointKey,
                                                         String detailIdKey) {
        if (!searchEndpointKey.equals(endpointDefinition.getEndpointKey())
                || !fetchConfig.isDetailEnabled()
                || records == null
                || records.isEmpty()) {
            return records;
        }
        int remaining = Math.min(fetchConfig.getMaxDetailCalls(), records.size());
        if (remaining <= 0) {
            return records;
        }
        List<CampusIngestItem> enhanced = new ArrayList<>(records.size());
        TikhubEndpointDefinition detailEndpoint = tikhubEndpointRegistry.require(detailEndpointKey);
        for (CampusIngestItem record : records) {
            CampusIngestItem next = record;
            String detailId = resolveDetailId(record, detailIdKey);
            if (remaining > 0 && StringUtils.isNotBlank(detailId)) {
                try {
                    remaining--;
                    String detailBody = tikhubClient.fetchDetail(detailEndpoint, fetchConfig, request, detailId);
                    next = tikhubResponseMapper.mapDetail(detailEndpoint, fetchConfig, detailBody, record);
                } catch (RuntimeException ignored) {
                    next = markDetailFailure(record, detailEndpointKey, ignored);
                }
            }
            enhanced.add(next);
        }
        return enhanced;
    }

    private String tikhubFetchMessage(TikhubEndpointDefinition endpointDefinition, int recordCount) {
        String endpointKey = endpointDefinition == null ? null : endpointDefinition.getEndpointKey();
        if (recordCount == 0 && WECHAT_MP_SEARCH_ARTICLE.equals(endpointKey)) {
            return "TikHub WeChat request succeeded but returned no recognizable articles";
        }
        if (recordCount == 0) {
            return "TikHub request succeeded but returned no recognizable records";
        }
        return "TikHub fetch completed, records=" + recordCount;
    }

    private CampusIngestItem markDetailFailure(CampusIngestItem record, String detailEndpointKey, RuntimeException ex) {
        if (record == null) {
            return null;
        }
        JSONObject raw;
        try {
            raw = JSON.parseObject(record.getRawData());
            if (raw == null) {
                raw = new JSONObject();
            }
        } catch (RuntimeException parseError) {
            raw = new JSONObject();
            raw.put("origin", record.getRawData());
        }
        raw.put("_detail_capture_status", "failed");
        raw.put("_detail_endpoint", detailEndpointKey);
        raw.put("_detail_error", TikhubSanitizer.sanitizeError(ex == null ? null : ex.getMessage()));
        record.setRawData(TikhubSanitizer.sanitizeJsonToString(raw));
        return record;
    }

    private String resolveDetailId(CampusIngestItem record, String detailIdKey) {
        if (record == null) {
            return null;
        }
        try {
            JSONObject raw = JSON.parseObject(record.getRawData());
            String detailId = raw == null ? null : raw.getString(detailIdKey);
            if (StringUtils.isNotBlank(detailId)) {
                return detailId;
            }
        } catch (RuntimeException ignored) {
            // Fall back to external_id below.
        }
        String externalId = StringUtils.trimToNull(record.getExternalId());
        if ("aid".equals(detailIdKey) && externalId != null && !externalId.matches("\\d+")) {
            return null;
        }
        return externalId;
    }

    private String resolveXiaohongshuDetailEndpoint(CampusIngestItem record) {
        String type = StringUtils.defaultString(record.getContentType()).toLowerCase();
        if (type.contains("video")) {
            return XIAOHONGSHU_VIDEO_DETAIL;
        }
        try {
            JSONObject raw = JSON.parseObject(record.getRawData());
            String rawType = raw == null ? null : raw.getString("type");
            if (StringUtils.defaultString(rawType).toLowerCase().contains("video")) {
                return XIAOHONGSHU_VIDEO_DETAIL;
            }
        } catch (RuntimeException ignored) {
            // Search-card raw data is best-effort only; image detail is the safer default.
        }
        return XIAOHONGSHU_IMAGE_DETAIL;
    }
}
