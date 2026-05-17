package com.stonedt.intelligence.service.campus.ingest.publicweb;

import com.github.pagehelper.PageInfo;
import com.stonedt.intelligence.entity.campus.CampusIngestTask;
import com.stonedt.intelligence.entity.campus.CampusPublicWebWhitelist;
import com.stonedt.intelligence.service.campus.CampusPublicWebWhitelistService;
import com.stonedt.intelligence.service.campus.ingest.CampusIngestFetchRequest;
import com.stonedt.intelligence.service.campus.ingest.CampusIngestFetchResponse;
import com.stonedt.intelligence.service.campus.ingest.PublicWebIngestAdapter;
import com.stonedt.intelligence.service.campus.ingest.extract.ContentExtractionClient;
import com.stonedt.intelligence.service.campus.ingest.extract.ContentExtractionRequest;
import com.stonedt.intelligence.service.campus.ingest.extract.ContentExtractionResult;
import org.junit.Assert;
import org.junit.Test;

public class PublicWebIngestAdapterTest {

    @Test
    public void jinaReaderModeProducesOneRecord() {
        PublicWebIngestAdapter adapter = new PublicWebIngestAdapter(
                new PublicWebWhitelistValidator(new StubWhitelistService()), new StubExtractionClient());

        CampusIngestFetchResponse response = adapter.fetch(request(
                "{\"whitelistId\":1001,\"url\":\"https://www.example.edu.cn/news/1.html\","
                        + "\"mode\":\"jina_reader\",\"timeoutMs\":10000}"
        ));

        Assert.assertEquals(1, response.getRecords().size());
        Assert.assertEquals("public_web", response.getRecords().get(0).getPlatform());
        Assert.assertEquals("网页标题", response.getRecords().get(0).getTitle());
        Assert.assertEquals("网页正文", response.getRecords().get(0).getContent());
    }

    @Test
    public void metadataOnlyKeepsReservedEmptyResponse() {
        PublicWebIngestAdapter adapter = new PublicWebIngestAdapter(
                new PublicWebWhitelistValidator(new StubWhitelistService()), new StubExtractionClient());

        CampusIngestFetchResponse response = adapter.fetch(request(
                "{\"whitelistId\":1001,\"url\":\"https://www.example.edu.cn/news/1.html\","
                        + "\"mode\":\"metadata_only\"}"
        ));

        Assert.assertTrue(response.getRecords().isEmpty());
    }

    private CampusIngestFetchRequest request(String fetchConfig) {
        CampusIngestTask task = new CampusIngestTask();
        task.setFetchConfig(fetchConfig);
        CampusIngestFetchRequest request = new CampusIngestFetchRequest();
        request.setTask(task);
        return request;
    }

    private static class StubExtractionClient implements ContentExtractionClient {
        @Override
        public String provider() {
            return "jina_reader";
        }

        @Override
        public boolean isEnabled() {
            return true;
        }

        @Override
        public ContentExtractionResult extract(ContentExtractionRequest request) {
            ContentExtractionResult result = new ContentExtractionResult();
            result.setProvider(provider());
            result.setSourceUrl(request.getUrl());
            result.setTitle("网页标题");
            result.setContent("网页正文");
            return result;
        }
    }

    private static class StubWhitelistService implements CampusPublicWebWhitelistService {
        @Override
        public CampusPublicWebWhitelist save(CampusPublicWebWhitelist whitelist, Long operatorUserId) {
            return null;
        }

        @Override
        public CampusPublicWebWhitelist updateStatus(Long whitelistId, Integer enabled, Long operatorUserId) {
            return null;
        }

        @Override
        public void delete(Long whitelistId, Long operatorUserId) {
        }

        @Override
        public CampusPublicWebWhitelist requireEnabled(Long whitelistId) {
            CampusPublicWebWhitelist whitelist = new CampusPublicWebWhitelist();
            whitelist.setWhitelistId(1001L);
            whitelist.setSiteName("示例官网");
            whitelist.setSiteDomain("example.edu.cn");
            whitelist.setAllowedPathPrefix("/news/");
            whitelist.setEnabled(1);
            return whitelist;
        }

        @Override
        public PageInfo<CampusPublicWebWhitelist> list(Integer pageNum,
                                                       Integer pageSize,
                                                       String keyword,
                                                       String siteDomain,
                                                       Integer enabled) {
            return null;
        }
    }
}
