package com.stonedt.intelligence.service.campus.ingest;

import com.stonedt.intelligence.entity.campus.CampusIngestTask;
import com.stonedt.intelligence.service.campus.ingest.tikhub.TikhubClient;
import com.stonedt.intelligence.service.campus.ingest.tikhub.TikhubEndpointDefinition;
import com.stonedt.intelligence.service.campus.ingest.tikhub.TikhubEndpointRegistry;
import com.stonedt.intelligence.service.campus.ingest.tikhub.TikhubFetchConfig;
import com.stonedt.intelligence.service.campus.ingest.tikhub.TikhubIngestException;
import com.stonedt.intelligence.service.campus.ingest.tikhub.TikhubResponseMapper;
import org.junit.Assert;
import org.junit.Test;

public class ThirdPartyApiIngestAdapterTest {

    @Test
    public void retriesWechatSearchTransientFailures() {
        RetryingTikhubClient client = new RetryingTikhubClient(2,
                "{\"data\":{\"items\":[{\"id\":\"wx-1\",\"title\":\"新疆大学公众号文章\","
                        + "\"desc\":\"新疆大学相关信息\","
                        + "\"link\":\"https://mp.weixin.qq.com/s/demo\"}]}}");
        ThirdPartyApiIngestAdapter adapter = new ThirdPartyApiIngestAdapter(
                new TikhubEndpointRegistry(), client, new TikhubResponseMapper());

        CampusIngestFetchResponse response = adapter.fetch(request(
                "{\"provider\":\"tikhub\",\"endpointKey\":\"wechat_mp_search_article\","
                        + "\"platform\":\"wechat_official\",\"query\":\"新疆大学\","
                        + "\"sortType\":\"_0\",\"credentialRef\":\"TIKHUB_API_KEY\"}"));

        Assert.assertEquals(3, client.getAttempts());
        Assert.assertEquals(1, response.getRecords().size());
        Assert.assertEquals("wechat_official", response.getRecords().get(0).getPlatform());
        Assert.assertEquals("https://mp.weixin.qq.com/s/demo", response.getRecords().get(0).getOriginalUrl());
    }

    @Test
    public void marksWechatEmptySuccessAsNoRecognizableArticles() {
        RetryingTikhubClient client = new RetryingTikhubClient(0, "{\"data\":{\"items\":[]}}");
        ThirdPartyApiIngestAdapter adapter = new ThirdPartyApiIngestAdapter(
                new TikhubEndpointRegistry(), client, new TikhubResponseMapper());

        CampusIngestFetchResponse response = adapter.fetch(request(
                "{\"provider\":\"tikhub\",\"endpointKey\":\"wechat_mp_search_article\","
                        + "\"platform\":\"wechat_official\",\"query\":\"新疆大学\","
                        + "\"sortType\":\"_0\",\"credentialRef\":\"TIKHUB_API_KEY\"}"));

        Assert.assertTrue(response.getRecords().isEmpty());
        Assert.assertEquals("TikHub WeChat request succeeded but returned no recognizable articles",
                response.getMessage());
    }

    private CampusIngestFetchRequest request(String fetchConfig) {
        CampusIngestTask task = new CampusIngestTask();
        task.setFetchConfig(fetchConfig);
        CampusIngestFetchRequest request = new CampusIngestFetchRequest();
        request.setTask(task);
        return request;
    }

    private static class RetryingTikhubClient extends TikhubClient {

        private final int failuresBeforeSuccess;
        private final String responseBody;
        private int attempts;

        RetryingTikhubClient(int failuresBeforeSuccess, String responseBody) {
            super(null, null, null);
            this.failuresBeforeSuccess = failuresBeforeSuccess;
            this.responseBody = responseBody;
        }

        @Override
        public String fetch(TikhubEndpointDefinition endpointDefinition,
                            TikhubFetchConfig fetchConfig,
                            CampusIngestFetchRequest fetchRequest) {
            attempts++;
            if (attempts <= failuresBeforeSuccess) {
                throw new TikhubIngestException("TikHub request failed: status=400, body={\"message\":\"Request failed. Please retry.\"}");
            }
            return responseBody;
        }

        int getAttempts() {
            return attempts;
        }
    }
}
