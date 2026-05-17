package com.stonedt.intelligence.service.campus.ingest;

import com.alibaba.fastjson.JSONObject;
import com.stonedt.intelligence.entity.campus.CampusPublicWebWhitelist;
import com.stonedt.intelligence.service.campus.ingest.extract.ContentExtractionClient;
import com.stonedt.intelligence.service.campus.ingest.extract.ContentExtractionException;
import com.stonedt.intelligence.service.campus.ingest.extract.ContentExtractionRequest;
import com.stonedt.intelligence.service.campus.ingest.extract.ContentExtractionResult;
import com.stonedt.intelligence.service.campus.ingest.publicweb.PublicWebFetchConfig;
import com.stonedt.intelligence.service.campus.ingest.publicweb.PublicWebIngestException;
import com.stonedt.intelligence.service.campus.ingest.publicweb.PublicWebWhitelistValidator;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PublicWebIngestAdapter implements CampusIngestAdapter {

    private final PublicWebWhitelistValidator publicWebWhitelistValidator;
    private final ContentExtractionClient contentExtractionClient;

    public PublicWebIngestAdapter(PublicWebWhitelistValidator publicWebWhitelistValidator,
                                  ContentExtractionClient contentExtractionClient) {
        this.publicWebWhitelistValidator = publicWebWhitelistValidator;
        this.contentExtractionClient = contentExtractionClient;
    }

    @Override
    public String adapterType() {
        return "public_web_pull";
    }

    @Override
    public CampusIngestFetchResponse fetch(CampusIngestFetchRequest request) {
        PublicWebFetchConfig fetchConfig = PublicWebFetchConfig.fromRequest(request);
        CampusPublicWebWhitelist whitelist = publicWebWhitelistValidator.validate(fetchConfig);
        if (PublicWebFetchConfig.MODE_METADATA_ONLY.equals(fetchConfig.getMode())) {
            return CampusIngestFetchResponse.empty("public web fetcher is reserved, no network request executed");
        }
        if (!PublicWebFetchConfig.MODE_JINA_READER.equals(fetchConfig.getMode())) {
            throw new PublicWebIngestException("公开网页 mode 不支持: " + fetchConfig.getMode());
        }
        if (!contentExtractionClient.isEnabled()) {
            throw new PublicWebIngestException("公开网页正文读取需要先启用 content.extraction.enabled");
        }
        try {
            ContentExtractionResult extractionResult = contentExtractionClient.extract(extractionRequest(fetchConfig, request));
            CampusIngestFetchResponse response = new CampusIngestFetchResponse();
            List<CampusIngestItem> records = new ArrayList<>();
            records.add(toItem(fetchConfig, whitelist, extractionResult));
            response.setRecords(records);
            response.setMessage("public web jina reader fetch completed, records=1");
            return response;
        } catch (ContentExtractionException ex) {
            throw new PublicWebIngestException("公开网页正文读取失败: " + ex.getMessage(), ex);
        }
    }

    private ContentExtractionRequest extractionRequest(PublicWebFetchConfig fetchConfig,
                                                       CampusIngestFetchRequest request) {
        ContentExtractionRequest extractionRequest = new ContentExtractionRequest();
        extractionRequest.setUrl(fetchConfig.getUrl());
        extractionRequest.setTimeoutMs(fetchConfig.getTimeoutMs());
        extractionRequest.setEndpointKey("public_web_reader");
        extractionRequest.setIngestRequest(request);
        return extractionRequest;
    }

    private CampusIngestItem toItem(PublicWebFetchConfig fetchConfig,
                                    CampusPublicWebWhitelist whitelist,
                                    ContentExtractionResult extractionResult) {
        CampusIngestItem item = new CampusIngestItem();
        item.setExternalId(DigestUtils.md5Hex(fetchConfig.getUrl()));
        item.setPlatform("public_web");
        item.setContentType("article");
        item.setTitle(StringUtils.defaultIfBlank(extractionResult.getTitle(),
                whitelist == null ? "公开网页" : whitelist.getSiteName()));
        item.setContent(extractionResult.getContent());
        item.setOriginalUrl(fetchConfig.getUrl());
        item.setAuthorName(whitelist == null ? null : whitelist.getSiteName());
        item.setRiskLevel("normal");
        item.setRawData(rawData(fetchConfig, whitelist, extractionResult));
        return item;
    }

    private String rawData(PublicWebFetchConfig fetchConfig,
                           CampusPublicWebWhitelist whitelist,
                           ContentExtractionResult extractionResult) {
        JSONObject raw = new JSONObject();
        raw.put("provider", "jina");
        raw.put("mode", fetchConfig.getMode());
        raw.put("url", fetchConfig.getUrl());
        raw.put("whitelistId", fetchConfig.getWhitelistId());
        raw.put("siteName", whitelist == null ? null : whitelist.getSiteName());
        raw.put("siteDomain", whitelist == null ? null : whitelist.getSiteDomain());
        raw.put("readerTitle", extractionResult == null ? null : extractionResult.getTitle());
        raw.put("sourceUrl", extractionResult == null ? null : extractionResult.getSourceUrl());
        return raw.toJSONString();
    }
}
