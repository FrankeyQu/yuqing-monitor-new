package com.stonedt.intelligence.service.campus.ingest.extract;

public class ContentExtractionException extends RuntimeException {

    public ContentExtractionException(String message) {
        super(message);
    }

    public ContentExtractionException(String message, Throwable cause) {
        super(message, cause);
    }
}
