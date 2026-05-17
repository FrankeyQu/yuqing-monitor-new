package com.stonedt.intelligence.service.campus.ingest;

import java.util.ArrayList;
import java.util.List;

public class CampusIngestFetchResponse {

    private boolean supported = true;
    private String message;
    private List<CampusIngestItem> records = new ArrayList<>();

    public static CampusIngestFetchResponse empty(String message) {
        CampusIngestFetchResponse response = new CampusIngestFetchResponse();
        response.setMessage(message);
        return response;
    }

    public static CampusIngestFetchResponse unsupported(String message) {
        CampusIngestFetchResponse response = new CampusIngestFetchResponse();
        response.setSupported(false);
        response.setMessage(message);
        return response;
    }

    public boolean isSupported() {
        return supported;
    }

    public void setSupported(boolean supported) {
        this.supported = supported;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<CampusIngestItem> getRecords() {
        return records;
    }

    public void setRecords(List<CampusIngestItem> records) {
        this.records = records;
    }
}
