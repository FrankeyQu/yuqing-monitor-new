package com.stonedt.intelligence.service.campus.ingest.publicweb;

import com.github.pagehelper.PageInfo;
import com.stonedt.intelligence.entity.campus.CampusIngestTask;
import com.stonedt.intelligence.entity.campus.CampusPublicWebWhitelist;
import com.stonedt.intelligence.service.campus.CampusPublicWebWhitelistService;
import com.stonedt.intelligence.service.campus.ingest.CampusIngestFetchRequest;
import org.junit.Assert;
import org.junit.Test;

public class PublicWebWhitelistValidatorTest {

    @Test
    public void allowExactDomainAndPathPrefix() {
        PublicWebWhitelistValidator validator = new PublicWebWhitelistValidator(new StubWhitelistService(whitelist()));

        CampusPublicWebWhitelist result = validator.validate(config("https://www.example.edu.cn/news/today.html"));

        Assert.assertEquals(1001L, result.getWhitelistId().longValue());
    }

    @Test
    public void allowExactPathWhenPrefixHasNoTrailingSlash() {
        CampusPublicWebWhitelist whitelist = whitelist();
        whitelist.setAllowedPathPrefix("/news");
        PublicWebWhitelistValidator validator = new PublicWebWhitelistValidator(new StubWhitelistService(whitelist));

        validator.validate(config("https://www.example.edu.cn/news"));
    }

    @Test
    public void allowSubDomainButRejectLookalikeDomain() {
        PublicWebWhitelistValidator validator = new PublicWebWhitelistValidator(new StubWhitelistService(whitelist()));

        validator.validate(config("https://news.example.edu.cn/news/today.html"));
        try {
            validator.validate(config("https://badexample.edu.cn/news/today.html"));
            Assert.fail("lookalike domain should be rejected");
        } catch (PublicWebIngestException expected) {
            Assert.assertTrue(expected.getMessage().contains("域名"));
        }
    }

    @Test(expected = PublicWebIngestException.class)
    public void rejectPathOutsidePrefix() {
        PublicWebWhitelistValidator validator = new PublicWebWhitelistValidator(new StubWhitelistService(whitelist()));

        validator.validate(config("https://www.example.edu.cn/private/today.html"));
    }

    @Test(expected = PublicWebIngestException.class)
    public void rejectSimilarPathPrefix() {
        CampusPublicWebWhitelist whitelist = whitelist();
        whitelist.setAllowedPathPrefix("/news");
        PublicWebWhitelistValidator validator = new PublicWebWhitelistValidator(new StubWhitelistService(whitelist));

        validator.validate(config("https://www.example.edu.cn/newsroom/today.html"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectUrlWithUserInfo() {
        PublicWebWhitelistValidator.validateHttpUrl("https://user:pass@www.example.edu.cn/news/");
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectLocalhostUrl() {
        PublicWebWhitelistValidator.validateHttpUrl("https://localhost/news/");
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectIpLiteralUrl() {
        PublicWebWhitelistValidator.validateHttpUrl("https://127.0.0.1/news/");
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectUrlWithSensitiveQuery() {
        PublicWebWhitelistValidator.validateHttpUrl("https://www.example.edu.cn/news/?token=abc");
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectDomainContainingScheme() {
        PublicWebWhitelistValidator.validateDomainFormat("https://www.example.edu.cn");
    }

    private PublicWebFetchConfig config(String url) {
        CampusIngestTask task = new CampusIngestTask();
        task.setFetchConfig("{\"whitelistId\":1001,\"url\":\"" + url + "\"}");
        CampusIngestFetchRequest request = new CampusIngestFetchRequest();
        request.setTask(task);
        return PublicWebFetchConfig.fromRequest(request);
    }

    private CampusPublicWebWhitelist whitelist() {
        CampusPublicWebWhitelist whitelist = new CampusPublicWebWhitelist();
        whitelist.setWhitelistId(1001L);
        whitelist.setSiteDomain("example.edu.cn");
        whitelist.setAllowedPathPrefix("/news/");
        whitelist.setEnabled(1);
        return whitelist;
    }

    private static class StubWhitelistService implements CampusPublicWebWhitelistService {

        private final CampusPublicWebWhitelist whitelist;

        private StubWhitelistService(CampusPublicWebWhitelist whitelist) {
            this.whitelist = whitelist;
        }

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
