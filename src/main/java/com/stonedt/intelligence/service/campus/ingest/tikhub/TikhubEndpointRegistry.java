package com.stonedt.intelligence.service.campus.ingest.tikhub;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class TikhubEndpointRegistry {

    private final Map<String, TikhubEndpointDefinition> endpoints;

    public TikhubEndpointRegistry() {
        Map<String, TikhubEndpointDefinition> definitions = new LinkedHashMap<>();
        definitions.put("douyin_search_video_v2", new TikhubEndpointDefinition(
                "douyin_search_video_v2",
                "POST",
                "/api/v1/douyin/search/fetch_video_search_v2",
                true
        ));
        definitions.put("douyin_search_general_v5", new TikhubEndpointDefinition(
                "douyin_search_general_v5",
                "POST",
                "/api/v1/douyin/search/fetch_general_search_v5",
                true
        ));
        definitions.put("weibo_search_all", new TikhubEndpointDefinition(
                "weibo_search_all",
                "GET",
                "/api/v1/weibo/app/fetch_search_all",
                true
        ));
        definitions.put("weibo_post_detail_v2", new TikhubEndpointDefinition(
                "weibo_post_detail_v2",
                "GET",
                "/api/v1/weibo/web_v2/fetch_post_detail",
                true
        ));
        definitions.put("xiaohongshu_search_notes", new TikhubEndpointDefinition(
                "xiaohongshu_search_notes",
                "GET",
                "/api/v1/xiaohongshu/app_v2/search_notes",
                true
        ));
        definitions.put("xiaohongshu_image_note_detail", new TikhubEndpointDefinition(
                "xiaohongshu_image_note_detail",
                "GET",
                "/api/v1/xiaohongshu/app_v2/get_image_note_detail",
                true
        ));
        definitions.put("xiaohongshu_video_note_detail", new TikhubEndpointDefinition(
                "xiaohongshu_video_note_detail",
                "GET",
                "/api/v1/xiaohongshu/app_v2/get_video_note_detail",
                true
        ));
        definitions.put("bilibili_search_by_type", new TikhubEndpointDefinition(
                "bilibili_search_by_type",
                "GET",
                "/api/v1/bilibili/app/fetch_search_by_type",
                true
        ));
        definitions.put("bilibili_video_detail", new TikhubEndpointDefinition(
                "bilibili_video_detail",
                "GET",
                "/api/v1/bilibili/web/fetch_video_detail",
                true
        ));
        definitions.put("zhihu_article_search_v3", new TikhubEndpointDefinition(
                "zhihu_article_search_v3",
                "GET",
                "/api/v1/zhihu/web/fetch_article_search_v3",
                true
        ));
        definitions.put("wechat_mp_search_article", new TikhubEndpointDefinition(
                "wechat_mp_search_article",
                "GET",
                "/api/v1/wechat_mp/web/fetch_search_article",
                true
        ));
        definitions.put("kuaishou_search_comprehensive", new TikhubEndpointDefinition(
                "kuaishou_search_comprehensive",
                "GET",
                "/api/v1/kuaishou/app/search_comprehensive",
                true
        ));
        definitions.put("kuaishou_search_video_v2", new TikhubEndpointDefinition(
                "kuaishou_search_video_v2",
                "GET",
                "/api/v1/kuaishou/app/search_video_v2",
                true
        ));
        this.endpoints = Collections.unmodifiableMap(definitions);
    }

    public TikhubEndpointDefinition require(String endpointKey) {
        if (endpointKey == null || endpointKey.trim().length() == 0) {
            throw new TikhubIngestException("TikHub endpointKey is required");
        }
        TikhubEndpointDefinition endpointDefinition = endpoints.get(endpointKey);
        if (endpointDefinition == null) {
            throw new TikhubIngestException("TikHub endpoint is not allowlisted: "
                    + TikhubSanitizer.sanitizeText(endpointKey));
        }
        return endpointDefinition;
    }
}
