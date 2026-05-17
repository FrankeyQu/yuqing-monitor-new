package com.stonedt.intelligence.service.campus.ingest.baidu;

import com.stonedt.intelligence.entity.campus.CampusIngestTask;
import com.stonedt.intelligence.service.campus.ingest.CampusIngestFetchRequest;
import org.junit.Assert;
import org.junit.Test;

public class BaiduIngestFetchConfigTest {

    @Test
    public void parseReaderOptions() {
        BaiduIngestFetchConfig config = BaiduIngestFetchConfig.fromRequest(request(
                "{\"provider\":\"baidu\",\"query\":\"新疆 教育\",\"resourceTypes\":[\"web\"],"
                        + "\"readerEnabled\":true,\"readerProvider\":\"jina\",\"maxReaderCalls\":3,"
                        + "\"fallbackToSnippet\":false,\"readerTimeoutMs\":12000}"
        ));

        Assert.assertTrue(config.isReaderEnabled());
        Assert.assertEquals("jina", config.getReaderProvider());
        Assert.assertEquals(3, config.getMaxReaderCalls());
        Assert.assertFalse(config.isFallbackToSnippet());
        Assert.assertEquals(12000, config.getReaderTimeoutMs());
    }

    @Test(expected = BaiduIngestException.class)
    public void rejectUnsupportedReaderProvider() {
        BaiduIngestFetchConfig.fromRequest(request(
                "{\"provider\":\"baidu\",\"query\":\"新疆 教育\",\"readerEnabled\":true,\"readerProvider\":\"other\"}"
        ));
    }

    private CampusIngestFetchRequest request(String fetchConfig) {
        CampusIngestTask task = new CampusIngestTask();
        task.setFetchConfig(fetchConfig);
        CampusIngestFetchRequest request = new CampusIngestFetchRequest();
        request.setTask(task);
        return request;
    }
}
