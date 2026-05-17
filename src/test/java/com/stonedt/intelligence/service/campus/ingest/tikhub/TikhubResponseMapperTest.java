package com.stonedt.intelligence.service.campus.ingest.tikhub;

import com.stonedt.intelligence.entity.campus.CampusIngestTask;
import com.stonedt.intelligence.service.campus.ingest.CampusIngestFetchRequest;
import com.stonedt.intelligence.service.campus.ingest.CampusIngestItem;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TikhubResponseMapperTest {

    @Test
    public void mapDouyinSearchVideoV2SampleBusinessData() {
        TikhubFetchConfig fetchConfig = fetchConfig();
        TikhubEndpointDefinition endpointDefinition = new TikhubEndpointDefinition(
                "douyin_search_video_v2",
                "POST",
                "/api/v1/douyin/search/fetch_video_search_v2",
                true
        );
        String responseBody = "{"
                + "\"data\":{"
                + "\"business_data\":[{"
                + "\"data\":{"
                + "\"aweme_info\":{"
                + "\"aweme_id\":\"7290000000000000001\","
                + "\"desc\":\"School canteen discussion sample\","
                + "\"share_url\":\"https://www.douyin.com/video/7290000000000000001\","
                + "\"create_time\":1700000000,"
                + "\"access_token\":\"sample-secret\","
                + "\"author\":{\"nickname\":\"Campus Observer\"}"
                + "}"
                + "}"
                + "}]"
                + "}"
                + "}";

        List<CampusIngestItem> items = new TikhubResponseMapper().map(endpointDefinition, fetchConfig, responseBody);

        Assert.assertEquals(1, items.size());
        CampusIngestItem item = items.get(0);
        Assert.assertEquals("7290000000000000001", item.getExternalId());
        Assert.assertEquals("douyin", item.getPlatform());
        Assert.assertEquals("video", item.getContentType());
        Assert.assertEquals("School canteen discussion sample", item.getContent());
        Assert.assertEquals("Campus Observer", item.getAuthorName());
        Assert.assertEquals("school canteen", item.getKeywords());
        Assert.assertEquals(1700000000000L, item.getPublishTime().getTime());
        Assert.assertTrue(item.getRawData().contains("[REDACTED]"));
        Assert.assertFalse(item.getRawData().contains("sample-secret"));
    }

    @Test
    public void mapWeiboSearchAllWithGenericMapper() {
        CampusIngestTask task = new CampusIngestTask();
        task.setFetchConfig("{\"provider\":\"tikhub\",\"endpointKey\":\"weibo_search_all\","
                + "\"query\":\"school canteen\",\"limit\":10,"
                + "\"credentialRef\":\"TIKHUB_API_KEY\"}");
        CampusIngestFetchRequest request = new CampusIngestFetchRequest();
        request.setTask(task);
        TikhubFetchConfig fetchConfig = TikhubFetchConfig.fromRequest(request);
        TikhubEndpointDefinition endpointDefinition = new TikhubEndpointDefinition(
                "weibo_search_all",
                "GET",
                "/api/v1/weibo/app/fetch_search_all",
                true
        );
        String responseBody = "{"
                + "\"data\":{\"cards\":[{\"mblog\":{"
                + "\"id\":\"5088000000000001\","
                + "\"text\":\"School canteen weibo sample\","
                + "\"created_at\":\"Wed May 13 16:27:37 +0800 2026\","
                + "\"attitudes_count\":12,"
                + "\"comments_count\":3,"
                + "\"reposts_count\":2,"
                + "\"user\":{\"id\":\"123456\",\"screen_name\":\"Weibo Observer\"},"
                + "\"token\":\"secret-value\""
                + "}}]}"
                + "}}";

        List<CampusIngestItem> items = new TikhubResponseMapper().map(endpointDefinition, fetchConfig, responseBody);

        Assert.assertEquals(1, items.size());
        CampusIngestItem item = items.get(0);
        Assert.assertEquals("5088000000000001", item.getExternalId());
        Assert.assertEquals("weibo", item.getPlatform());
        Assert.assertEquals("post", item.getContentType());
        Assert.assertEquals("School canteen weibo sample", item.getContent());
        Assert.assertEquals("https://weibo.com/123456/5088000000000001", item.getOriginalUrl());
        Assert.assertEquals("Weibo Observer", item.getAuthorName());
        Assert.assertNotNull(item.getPublishTime());
        Assert.assertEquals(Long.valueOf(12L), item.getLikeCount());
        Assert.assertEquals(Long.valueOf(3L), item.getCommentCount());
        Assert.assertEquals(Long.valueOf(2L), item.getShareCount());
        Assert.assertTrue(item.getRawData().contains("[REDACTED]"));
        Assert.assertFalse(item.getRawData().contains("secret-value"));
    }

    @Test
    public void ignoreProfileOnlyObjectsInGenericMapper() {
        CampusIngestTask task = new CampusIngestTask();
        task.setFetchConfig("{\"provider\":\"tikhub\",\"endpointKey\":\"weibo_search_all\","
                + "\"query\":\"school canteen\",\"limit\":10,"
                + "\"credentialRef\":\"TIKHUB_API_KEY\"}");
        CampusIngestFetchRequest request = new CampusIngestFetchRequest();
        request.setTask(task);
        TikhubFetchConfig fetchConfig = TikhubFetchConfig.fromRequest(request);
        TikhubEndpointDefinition endpointDefinition = new TikhubEndpointDefinition(
                "weibo_search_all",
                "GET",
                "/api/v1/weibo/app/fetch_search_all",
                true
        );
        String responseBody = "{"
                + "\"data\":{\"cards\":[{\"user\":{"
                + "\"id\":\"profile-1\","
                + "\"name\":\"Only Profile\","
                + "\"screen_name\":\"Only Profile\""
                + "}}]}"
                + "}}";

        List<CampusIngestItem> items = new TikhubResponseMapper().map(endpointDefinition, fetchConfig, responseBody);

        Assert.assertTrue(items.isEmpty());
    }

    @Test
    public void ignoreWeiboSearchAndTopicCards() {
        CampusIngestTask task = new CampusIngestTask();
        task.setFetchConfig("{\"provider\":\"tikhub\",\"endpointKey\":\"weibo_search_all\","
                + "\"query\":\"school canteen\",\"limit\":10,"
                + "\"credentialRef\":\"TIKHUB_API_KEY\"}");
        CampusIngestFetchRequest request = new CampusIngestFetchRequest();
        request.setTask(task);
        TikhubFetchConfig fetchConfig = TikhubFetchConfig.fromRequest(request);
        TikhubEndpointDefinition endpointDefinition = new TikhubEndpointDefinition(
                "weibo_search_all",
                "GET",
                "/api/v1/weibo/app/fetch_search_all",
                true
        );
        String responseBody = "{"
                + "\"data\":{\"cards\":["
                + "{\"title\":\"school canteen\",\"content\":\"实时资讯，海量讨论\",\"url\":\"http://s.weibo.com/weibo?q=school%20canteen\"},"
                + "{\"title\":\"3.9万帖子 13.3万新大人\",\"scheme\":\"sinaweibo://tabbar?containerid=100803_-_super&forwardscheme=mid_5297997741032379\"}"
                + "]}"
                + "}";

        List<CampusIngestItem> items = new TikhubResponseMapper().map(endpointDefinition, fetchConfig, responseBody);

        Assert.assertTrue(items.isEmpty());
    }

    @Test
    public void mapWeiboDetailSchemeToHttpOriginalUrl() {
        TikhubFetchConfig fetchConfig = fetchConfig("weibo_search_all", "weibo");
        TikhubEndpointDefinition endpointDefinition = new TikhubEndpointDefinition(
                "weibo_search_all",
                "GET",
                "/api/v1/weibo/app/fetch_search_all",
                true
        );
        String responseBody = "{"
                + "\"data\":{\"cards\":[{\"mblog\":{"
                + "\"text\":\"Real weibo post body\","
                + "\"scheme\":\"sinaweibo://detail/?mblogid=5298460245101522&id=5298460245101522\","
                + "\"user\":{\"id\":\"7861351792\",\"screen_name\":\"Weibo Author\"}"
                + "}}]}"
                + "}";

        List<CampusIngestItem> items = new TikhubResponseMapper().map(endpointDefinition, fetchConfig, responseBody);

        Assert.assertEquals(1, items.size());
        Assert.assertEquals("5298460245101522", items.get(0).getExternalId());
        Assert.assertEquals("https://weibo.com/7861351792/5298460245101522", items.get(0).getOriginalUrl());
    }

    @Test
    public void mapXiaohongshuSearchNotesWithGenericMapper() {
        CampusIngestTask task = new CampusIngestTask();
        task.setFetchConfig("{\"provider\":\"tikhub\",\"endpointKey\":\"xiaohongshu_search_notes\","
                + "\"platform\":\"xiaohongshu\",\"query\":\"school canteen\",\"limit\":10,"
                + "\"credentialRef\":\"TIKHUB_API_KEY\"}");
        CampusIngestFetchRequest request = new CampusIngestFetchRequest();
        request.setTask(task);
        TikhubFetchConfig fetchConfig = TikhubFetchConfig.fromRequest(request);
        TikhubEndpointDefinition endpointDefinition = new TikhubEndpointDefinition(
                "xiaohongshu_search_notes",
                "GET",
                "/api/v1/xiaohongshu/app_v2/search_notes",
                true
        );
        String responseBody = "{"
                + "\"data\":{\"items\":[{"
                + "\"note_id\":\"xhs-note-1\","
                + "\"title\":\"School canteen note\","
                + "\"desc\":\"XHS content sample\","
                + "\"liked_count\":7,"
                + "\"comments_count\":2,"
                + "\"shared_count\":3,"
                + "\"collected_count\":4,"
                + "\"user\":{\"nickname\":\"XHS Observer\"}"
                + "}]}"
                + "}";

        List<CampusIngestItem> items = new TikhubResponseMapper().map(endpointDefinition, fetchConfig, responseBody);

        Assert.assertEquals(1, items.size());
        CampusIngestItem item = items.get(0);
        Assert.assertEquals("xhs-note-1", item.getExternalId());
        Assert.assertEquals("xiaohongshu", item.getPlatform());
        Assert.assertEquals("note", item.getContentType());
        Assert.assertEquals("XHS content sample", item.getContent());
        Assert.assertEquals("XHS Observer", item.getAuthorName());
        Assert.assertEquals("https://www.xiaohongshu.com/explore/xhs-note-1", item.getOriginalUrl());
        Assert.assertEquals(Long.valueOf(7L), item.getLikeCount());
        Assert.assertEquals(Long.valueOf(2L), item.getCommentCount());
        Assert.assertEquals(Long.valueOf(3L), item.getShareCount());
        Assert.assertEquals(Long.valueOf(4L), item.getCollectCount());
    }

    @Test
    public void mapXiaohongshuDetailMergesLongerContentAndMetrics() {
        CampusIngestTask task = new CampusIngestTask();
        task.setFetchConfig("{\"provider\":\"tikhub\",\"endpointKey\":\"xiaohongshu_search_notes\","
                + "\"platform\":\"xiaohongshu\",\"query\":\"school canteen\",\"limit\":10,"
                + "\"detailEnabled\":true,\"maxDetailCalls\":5,"
                + "\"credentialRef\":\"TIKHUB_API_KEY\"}");
        CampusIngestFetchRequest request = new CampusIngestFetchRequest();
        request.setTask(task);
        TikhubFetchConfig fetchConfig = TikhubFetchConfig.fromRequest(request);
        TikhubEndpointDefinition endpointDefinition = new TikhubEndpointDefinition(
                "xiaohongshu_image_note_detail",
                "GET",
                "/api/v1/xiaohongshu/app_v2/get_image_note_detail",
                true
        );
        CampusIngestItem fallback = new CampusIngestItem();
        fallback.setExternalId("xhs-note-1");
        fallback.setPlatform("xiaohongshu");
        fallback.setContentType("note");
        fallback.setTitle("short");
        fallback.setContent("short desc");
        fallback.setKeywords("school canteen");
        String responseBody = "{"
                + "\"data\":{\"data\":{\"items\":[{"
                + "\"id\":\"xhs-note-1\","
                + "\"title\":\"School canteen detail\","
                + "\"desc\":\"This is a longer xiaohongshu note detail body\","
                + "\"time\":1700000000,"
                + "\"liked_count\":12,"
                + "\"comments_count\":5,"
                + "\"shared_count\":4,"
                + "\"collected_count\":6,"
                + "\"user\":{\"nickname\":\"XHS Detail Author\"}"
                + "}]}}"
                + "}";

        CampusIngestItem item = new TikhubResponseMapper()
                .mapXiaohongshuDetail(endpointDefinition, fetchConfig, responseBody, fallback);

        Assert.assertEquals("xhs-note-1", item.getExternalId());
        Assert.assertEquals("xiaohongshu", item.getPlatform());
        Assert.assertEquals("note", item.getContentType());
        Assert.assertEquals("This is a longer xiaohongshu note detail body", item.getContent());
        Assert.assertEquals("XHS Detail Author", item.getAuthorName());
        Assert.assertEquals("https://www.xiaohongshu.com/explore/xhs-note-1", item.getOriginalUrl());
        Assert.assertEquals(1700000000000L, item.getPublishTime().getTime());
        Assert.assertEquals(Long.valueOf(12L), item.getLikeCount());
        Assert.assertEquals(Long.valueOf(5L), item.getCommentCount());
        Assert.assertEquals(Long.valueOf(4L), item.getShareCount());
        Assert.assertEquals(Long.valueOf(6L), item.getCollectCount());
        Assert.assertTrue(fetchConfig.isDetailEnabled());
        Assert.assertEquals(5, fetchConfig.getMaxDetailCalls());
    }

    @Test
    public void mapBilibiliSearchBuildsVideoUrl() {
        TikhubFetchConfig fetchConfig = fetchConfig("bilibili_search_by_type", "bilibili");
        TikhubEndpointDefinition endpointDefinition = new TikhubEndpointDefinition(
                "bilibili_search_by_type", "GET", "/api/v1/bilibili/app/fetch_search_by_type", true);
        String responseBody = "{"
                + "\"data\":{\"result\":[{"
                + "\"aid\":\"114514\","
                + "\"bvid\":\"BV1xx411c7mD\","
                + "\"title\":\"School <em class=\\\"keyword\\\">canteen</em> bilibili sample\","
                + "\"description\":\"Bilibili <em class=\\\"keyword\\\">detail</em> summary\","
                + "\"owner\":{\"name\":\"Bili Observer\"},"
                + "\"play\":88"
                + "}]}"
                + "}";

        List<CampusIngestItem> items = new TikhubResponseMapper().map(endpointDefinition, fetchConfig, responseBody);

        Assert.assertEquals(1, items.size());
        CampusIngestItem item = items.get(0);
        Assert.assertEquals("BV1xx411c7mD", item.getExternalId());
        Assert.assertEquals("bilibili", item.getPlatform());
        Assert.assertEquals("video", item.getContentType());
        Assert.assertEquals("School canteen bilibili sample", item.getTitle());
        Assert.assertEquals("Bilibili detail summary", item.getContent());
        Assert.assertEquals("https://www.bilibili.com/video/BV1xx411c7mD", item.getOriginalUrl());
        Assert.assertEquals("Bili Observer", item.getAuthorName());
    }

    @Test
    public void mapWeiboDetailMarksFullCapture() {
        TikhubFetchConfig fetchConfig = fetchConfig("weibo_search_all", "weibo");
        TikhubEndpointDefinition detailEndpoint = new TikhubEndpointDefinition(
                "weibo_post_detail_v2", "GET", "/api/v1/weibo/web_v2/fetch_post_detail", true);
        CampusIngestItem fallback = new CampusIngestItem();
        fallback.setExternalId("5088000000000001");
        fallback.setPlatform("weibo");
        fallback.setContentType("post");
        fallback.setTitle("short");
        fallback.setContent("short");
        String responseBody = "{"
                + "\"data\":{\"id\":\"5088000000000001\","
                + "\"text_raw\":\"This is a longer weibo long text body for campus monitoring\","
                + "\"user\":{\"id\":\"123456\",\"screen_name\":\"Weibo Detail\"}}"
                + "}";

        CampusIngestItem item = new TikhubResponseMapper()
                .mapDetail(detailEndpoint, fetchConfig, responseBody, fallback);

        Assert.assertEquals("This is a longer weibo long text body for campus monitoring", item.getContent());
        Assert.assertEquals("https://weibo.com/123456/5088000000000001", item.getOriginalUrl());
        Assert.assertTrue(item.getRawData().contains("\"_content_capture_status\":\"full\""));
    }

    @Test
    public void mapZhihuWechatAndKuaishouGenericResponses() {
        TikhubResponseMapper mapper = new TikhubResponseMapper();

        List<CampusIngestItem> zhihu = mapper.map(
                new TikhubEndpointDefinition("zhihu_article_search_v3", "GET",
                        "/api/v1/zhihu/web/fetch_article_search_v3", true),
                fetchConfig("zhihu_article_search_v3", "zhihu"),
                "{\"data\":[{\"article_id\":\"9001\",\"title\":\"Campus zhihu article\","
                        + "\"excerpt\":\"Zhihu article excerpt\",\"author\":{\"name\":\"Zhihu Author\"}}]}");
        List<CampusIngestItem> wechat = mapper.map(
                new TikhubEndpointDefinition("wechat_mp_search_article", "GET",
                        "/api/v1/wechat_mp/web/fetch_search_article", true),
                fetchConfig("wechat_mp_search_article", "wechat_official"),
                "{\"data\":{\"items\":[{\"id\":\"wx-1\",\"title\":\"Campus wechat article\","
                        + "\"digest\":\"Wechat digest\",\"link\":\"https://mp.weixin.qq.com/s/demo\","
                        + "\"account_name\":\"Campus MP\"}]}}");
        List<CampusIngestItem> kuaishou = mapper.map(
                new TikhubEndpointDefinition("kuaishou_search_comprehensive", "GET",
                        "/api/v1/kuaishou/app/search_comprehensive", true),
                fetchConfig("kuaishou_search_comprehensive", "kuaishou"),
                "{\"data\":{\"feeds\":[{\"photo_id\":\"3xabc\",\"caption\":\"Campus kuaishou video\","
                        + "\"user\":{\"name\":\"KS Author\"}}]}}");

        Assert.assertEquals("zhihu", zhihu.get(0).getPlatform());
        Assert.assertEquals("https://zhuanlan.zhihu.com/p/9001", zhihu.get(0).getOriginalUrl());
        Assert.assertEquals("wechat_official", wechat.get(0).getPlatform());
        Assert.assertEquals("https://mp.weixin.qq.com/s/demo", wechat.get(0).getOriginalUrl());
        Assert.assertEquals("kuaishou", kuaishou.get(0).getPlatform());
        Assert.assertEquals("https://www.kuaishou.com/short-video/3xabc", kuaishou.get(0).getOriginalUrl());
    }

    @Test(expected = TikhubIngestException.class)
    public void rejectInlineSecretInFetchConfig() {
        CampusIngestTask task = new CampusIngestTask();
        task.setFetchConfig("{\"provider\":\"tikhub\",\"endpointKey\":\"douyin_search_video_v2\","
                + "\"query\":\"school\",\"apiKey\":\"not-allowed\"}");
        CampusIngestFetchRequest request = new CampusIngestFetchRequest();
        request.setTask(task);

        TikhubFetchConfig.fromRequest(request);
    }

    private TikhubFetchConfig fetchConfig() {
        CampusIngestTask task = new CampusIngestTask();
        task.setFetchConfig("{\"provider\":\"tikhub\",\"endpointKey\":\"douyin_search_video_v2\","
                + "\"platform\":\"douyin\",\"query\":\"school canteen\",\"limit\":10,"
                + "\"credentialRef\":\"TIKHUB_API_KEY\"}");
        CampusIngestFetchRequest request = new CampusIngestFetchRequest();
        request.setTask(task);
        return TikhubFetchConfig.fromRequest(request);
    }

    private TikhubFetchConfig fetchConfig(String endpointKey, String platform) {
        CampusIngestTask task = new CampusIngestTask();
        task.setFetchConfig("{\"provider\":\"tikhub\",\"endpointKey\":\"" + endpointKey + "\","
                + "\"platform\":\"" + platform + "\",\"query\":\"school canteen\",\"limit\":10,"
                + "\"credentialRef\":\"TIKHUB_API_KEY\"}");
        CampusIngestFetchRequest request = new CampusIngestFetchRequest();
        request.setTask(task);
        return TikhubFetchConfig.fromRequest(request);
    }
}
