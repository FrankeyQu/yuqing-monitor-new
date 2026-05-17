package com.stonedt.intelligence.service.campus.ingest.normalize;

public class CampusIngestDedupResult {

    private final String status;
    private final Long recordId;
    private final String message;

    private CampusIngestDedupResult(String status, Long recordId, String message) {
        this.status = status;
        this.recordId = recordId;
        this.message = message;
    }

    public static CampusIngestDedupResult inserted(Long recordId) {
        return new CampusIngestDedupResult(CampusIngestDedupStatus.INSERTED, recordId, null);
    }

    public static CampusIngestDedupResult duplicateExternalId(Long recordId) {
        return new CampusIngestDedupResult(CampusIngestDedupStatus.DUPLICATE_EXTERNAL_ID,
                recordId, "external id duplicated");
    }

    public static CampusIngestDedupResult duplicateContentHash(Long recordId) {
        return new CampusIngestDedupResult(CampusIngestDedupStatus.DUPLICATE_CONTENT_HASH,
                recordId, "content hash duplicated");
    }

    public static CampusIngestDedupResult invalid(String message) {
        return new CampusIngestDedupResult(CampusIngestDedupStatus.INVALID, null, message);
    }

    public boolean isInserted() {
        return CampusIngestDedupStatus.INSERTED.equals(status);
    }

    public boolean isDuplicate() {
        return CampusIngestDedupStatus.DUPLICATE_EXTERNAL_ID.equals(status)
                || CampusIngestDedupStatus.DUPLICATE_CONTENT_HASH.equals(status);
    }

    public boolean isInvalid() {
        return CampusIngestDedupStatus.INVALID.equals(status);
    }

    public String getStatus() {
        return status;
    }

    public Long getRecordId() {
        return recordId;
    }

    public String getMessage() {
        return message;
    }
}
