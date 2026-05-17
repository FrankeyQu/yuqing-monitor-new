package com.stonedt.intelligence.service.campus.ingest.publicweb;

public class PublicWebIngestException extends RuntimeException {

    public PublicWebIngestException(String message) {
        super(message);
    }

    public PublicWebIngestException(String message, Throwable cause) {
        super(message, cause);
    }
}
