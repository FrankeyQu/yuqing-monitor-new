package com.stonedt.intelligence.service.campus.ingest.baidu;

public class BaiduIngestException extends RuntimeException {

    public BaiduIngestException(String message) {
        super(BaiduIngestSanitizer.sanitizeError(message));
    }

    public BaiduIngestException(String message, Throwable cause) {
        super(BaiduIngestSanitizer.sanitizeError(message), cause);
    }
}
