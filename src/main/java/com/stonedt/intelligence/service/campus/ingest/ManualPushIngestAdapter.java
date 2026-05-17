package com.stonedt.intelligence.service.campus.ingest;

import org.springframework.stereotype.Component;

@Component
public class ManualPushIngestAdapter implements CampusIngestAdapter {

    @Override
    public String adapterType() {
        return "manual_push";
    }

    @Override
    public CampusIngestFetchResponse fetch(CampusIngestFetchRequest request) {
        return CampusIngestFetchResponse.empty("manual_push adapter has no remote records to fetch");
    }
}
