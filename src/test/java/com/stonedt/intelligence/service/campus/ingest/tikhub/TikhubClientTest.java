package com.stonedt.intelligence.service.campus.ingest.tikhub;

import com.stonedt.intelligence.entity.campus.CampusIngestTask;
import com.stonedt.intelligence.service.campus.ingest.CampusIngestFetchRequest;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class TikhubClientTest {

    @Test
    public void buildBilibiliSearchUrlNormalizesNonNumericOrder() throws Exception {
        CampusIngestTask task = new CampusIngestTask();
        task.setFetchConfig("{\"provider\":\"tikhub\",\"endpointKey\":\"bilibili_search_by_type\","
                + "\"platform\":\"bilibili\",\"query\":\"school\",\"limit\":20,\"page\":1,"
                + "\"sortType\":\"totalrank\",\"credentialRef\":\"TIKHUB_API_KEY\"}");
        CampusIngestFetchRequest request = new CampusIngestFetchRequest();
        request.setTask(task);
        TikhubFetchConfig fetchConfig = TikhubFetchConfig.fromRequest(request);
        TikhubEndpointDefinition endpointDefinition = new TikhubEndpointDefinition(
                "bilibili_search_by_type",
                "GET",
                "/api/v1/bilibili/app/fetch_search_by_type",
                true
        );

        TikhubClient client = new TikhubClient(null, null, null);
        Field baseUrl = TikhubClient.class.getDeclaredField("baseUrl");
        baseUrl.setAccessible(true);
        baseUrl.set(client, "https://api.example.test");
        Method buildGetUrl = TikhubClient.class.getDeclaredMethod(
                "buildGetUrl", TikhubEndpointDefinition.class, TikhubFetchConfig.class);
        buildGetUrl.setAccessible(true);

        String url = (String) buildGetUrl.invoke(client, endpointDefinition, fetchConfig);

        Assert.assertTrue(url.contains("order=0"));
        Assert.assertFalse(url.contains("totalrank"));
    }

    @Test
    public void buildXiaohongshuDetailUrlUsesNoteId() throws Exception {
        TikhubEndpointDefinition endpointDefinition = new TikhubEndpointDefinition(
                "xiaohongshu_image_note_detail",
                "GET",
                "/api/v1/xiaohongshu/app_v2/get_image_note_detail",
                true
        );
        TikhubClient client = new TikhubClient(null, null, null);
        Field baseUrl = TikhubClient.class.getDeclaredField("baseUrl");
        baseUrl.setAccessible(true);
        baseUrl.set(client, "https://api.example.test");
        Method buildDetailGetUrl = TikhubClient.class.getDeclaredMethod(
                "buildDetailGetUrl", TikhubEndpointDefinition.class, String.class);
        buildDetailGetUrl.setAccessible(true);

        String url = (String) buildDetailGetUrl.invoke(client, endpointDefinition, "xhs-note-1");

        Assert.assertEquals("https://api.example.test/api/v1/xiaohongshu/app_v2/get_image_note_detail?note_id=xhs-note-1", url);
    }

    @Test
    public void buildNewPlatformSearchUrls() throws Exception {
        TikhubClient client = new TikhubClient(null, null, null);
        Field baseUrl = TikhubClient.class.getDeclaredField("baseUrl");
        baseUrl.setAccessible(true);
        baseUrl.set(client, "https://api.example.test");
        Method buildGetUrl = TikhubClient.class.getDeclaredMethod(
                "buildGetUrl", TikhubEndpointDefinition.class, TikhubFetchConfig.class);
        buildGetUrl.setAccessible(true);

        String zhihuUrl = (String) buildGetUrl.invoke(client,
                new TikhubEndpointDefinition("zhihu_article_search_v3", "GET",
                        "/api/v1/zhihu/web/fetch_article_search_v3", true),
                fetchConfig("zhihu_article_search_v3", "zhihu"));
        String wechatUrl = (String) buildGetUrl.invoke(client,
                new TikhubEndpointDefinition("wechat_mp_search_article", "GET",
                        "/api/v1/wechat_mp/web/fetch_search_article", true),
                fetchConfig("wechat_mp_search_article", "wechat_official"));
        String kuaishouUrl = (String) buildGetUrl.invoke(client,
                new TikhubEndpointDefinition("kuaishou_search_comprehensive", "GET",
                        "/api/v1/kuaishou/app/search_comprehensive", true),
                fetchConfig("kuaishou_search_comprehensive", "kuaishou"));

        Assert.assertTrue(zhihuUrl.contains("/api/v1/zhihu/web/fetch_article_search_v3"));
        Assert.assertTrue(zhihuUrl.contains("keyword=school"));
        Assert.assertTrue(wechatUrl.contains("/api/v1/wechat_mp/web/fetch_search_article"));
        Assert.assertTrue(wechatUrl.contains("sort_type=_0"));
        Assert.assertTrue(kuaishouUrl.contains("/api/v1/kuaishou/app/search_comprehensive"));
        Assert.assertTrue(kuaishouUrl.contains("search_scope=all"));
    }

    @Test
    public void buildWeiboAndBilibiliDetailUrls() throws Exception {
        TikhubClient client = new TikhubClient(null, null, null);
        Field baseUrl = TikhubClient.class.getDeclaredField("baseUrl");
        baseUrl.setAccessible(true);
        baseUrl.set(client, "https://api.example.test");
        Method buildDetailGetUrl = TikhubClient.class.getDeclaredMethod(
                "buildDetailGetUrl", TikhubEndpointDefinition.class, String.class);
        buildDetailGetUrl.setAccessible(true);

        String weiboUrl = (String) buildDetailGetUrl.invoke(client,
                new TikhubEndpointDefinition("weibo_post_detail_v2", "GET",
                        "/api/v1/weibo/web_v2/fetch_post_detail", true),
                "5088000000000001");
        String bilibiliUrl = (String) buildDetailGetUrl.invoke(client,
                new TikhubEndpointDefinition("bilibili_video_detail", "GET",
                        "/api/v1/bilibili/web/fetch_video_detail", true),
                "114514");

        Assert.assertEquals("https://api.example.test/api/v1/weibo/web_v2/fetch_post_detail?id=5088000000000001&is_get_long_text=true", weiboUrl);
        Assert.assertEquals("https://api.example.test/api/v1/bilibili/web/fetch_video_detail?aid=114514", bilibiliUrl);
    }

    private TikhubFetchConfig fetchConfig(String endpointKey, String platform) {
        CampusIngestTask task = new CampusIngestTask();
        task.setFetchConfig("{\"provider\":\"tikhub\",\"endpointKey\":\"" + endpointKey + "\","
                + "\"platform\":\"" + platform + "\",\"query\":\"school\",\"limit\":20,\"page\":1,"
                + "\"credentialRef\":\"TIKHUB_API_KEY\"}");
        CampusIngestFetchRequest request = new CampusIngestFetchRequest();
        request.setTask(task);
        return TikhubFetchConfig.fromRequest(request);
    }
}
