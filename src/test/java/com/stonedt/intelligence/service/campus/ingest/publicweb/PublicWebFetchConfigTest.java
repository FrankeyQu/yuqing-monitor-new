package com.stonedt.intelligence.service.campus.ingest.publicweb;

import com.stonedt.intelligence.entity.campus.CampusIngestTask;
import com.stonedt.intelligence.service.campus.ingest.CampusIngestFetchRequest;
import org.junit.Assert;
import org.junit.Test;

public class PublicWebFetchConfigTest {

    @Test
    public void parseMetadataOnlyConfig() {
        PublicWebFetchConfig config = PublicWebFetchConfig.fromRequest(request(
                "{\"whitelistId\":1001,\"url\":\"https://news.example.edu.cn/public/\",\"mode\":\"metadata_only\"}"
        ));

        Assert.assertEquals(1001L, config.getWhitelistId().longValue());
        Assert.assertEquals("https://news.example.edu.cn/public/", config.getUrl());
        Assert.assertEquals("metadata_only", config.getMode());
    }

    @Test
    public void parseJinaReaderConfig() {
        PublicWebFetchConfig config = PublicWebFetchConfig.fromRequest(request(
                "{\"whitelistId\":1001,\"url\":\"https://news.example.edu.cn/public/1.html\","
                        + "\"mode\":\"jina_reader\",\"readerProvider\":\"jina\",\"maxDepth\":0,\"timeoutMs\":12000}"
        ));

        Assert.assertEquals("jina_reader", config.getMode());
        Assert.assertEquals("jina", config.getReaderProvider());
        Assert.assertEquals(0, config.getMaxDepth());
        Assert.assertEquals(12000, config.getTimeoutMs());
    }

    @Test(expected = PublicWebIngestException.class)
    public void rejectNonReservedMode() {
        PublicWebFetchConfig.fromRequest(request(
                "{\"whitelistId\":1001,\"url\":\"https://news.example.edu.cn/public/\",\"mode\":\"crawl\"}"
        ));
    }

    @Test(expected = PublicWebIngestException.class)
    public void rejectReaderDepthAboveZero() {
        PublicWebFetchConfig.fromRequest(request(
                "{\"whitelistId\":1001,\"url\":\"https://news.example.edu.cn/public/\","
                        + "\"mode\":\"jina_reader\",\"maxDepth\":1}"
        ));
    }

    @Test(expected = PublicWebIngestException.class)
    public void rejectUnsupportedFetchOptions() {
        PublicWebFetchConfig.fromRequest(request(
                "{\"whitelistId\":1001,\"url\":\"https://news.example.edu.cn/public/\",\"headers\":{\"Cookie\":\"x\"}}"
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
