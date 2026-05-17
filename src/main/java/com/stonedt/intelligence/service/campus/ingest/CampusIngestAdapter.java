package com.stonedt.intelligence.service.campus.ingest;

public interface CampusIngestAdapter {

    String adapterType();

    CampusIngestFetchResponse fetch(CampusIngestFetchRequest request);
}
