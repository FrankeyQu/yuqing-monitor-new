package com.stonedt.intelligence.service.campus.ingest.extract;

public interface ContentExtractionClient {

    String provider();

    boolean isEnabled();

    ContentExtractionResult extract(ContentExtractionRequest request);
}
