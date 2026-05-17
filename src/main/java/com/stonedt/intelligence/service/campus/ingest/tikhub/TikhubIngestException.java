package com.stonedt.intelligence.service.campus.ingest.tikhub;

public class TikhubIngestException extends RuntimeException {

    public TikhubIngestException(String message) {
        super(TikhubSanitizer.sanitizeError(message));
    }
}
