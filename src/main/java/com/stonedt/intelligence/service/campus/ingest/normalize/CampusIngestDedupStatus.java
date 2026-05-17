package com.stonedt.intelligence.service.campus.ingest.normalize;

public final class CampusIngestDedupStatus {

    public static final String INSERTED = "inserted";
    public static final String DUPLICATE_EXTERNAL_ID = "duplicate_external_id";
    public static final String DUPLICATE_CONTENT_HASH = "duplicate_content_hash";
    public static final String INVALID = "invalid";
    public static final String FAILED = "failed";

    private CampusIngestDedupStatus() {
    }
}
