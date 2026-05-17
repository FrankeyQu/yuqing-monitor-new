package com.stonedt.intelligence.service.campus.ingest.extract;

import com.stonedt.intelligence.service.campus.ingest.CampusIngestFetchRequest;

public class ContentExtractionRequest {

    private String url;
    private int timeoutMs;
    private CampusIngestFetchRequest ingestRequest;
    private String endpointKey;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(int timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public CampusIngestFetchRequest getIngestRequest() {
        return ingestRequest;
    }

    public void setIngestRequest(CampusIngestFetchRequest ingestRequest) {
        this.ingestRequest = ingestRequest;
    }

    public String getEndpointKey() {
        return endpointKey;
    }

    public void setEndpointKey(String endpointKey) {
        this.endpointKey = endpointKey;
    }
}
