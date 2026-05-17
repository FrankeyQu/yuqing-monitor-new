package com.stonedt.intelligence.service.campus.ingest.tikhub;

public class TikhubEndpointDefinition {

    private final String endpointKey;
    private final String method;
    private final String path;
    private final boolean implemented;

    public TikhubEndpointDefinition(String endpointKey, String method, String path, boolean implemented) {
        this.endpointKey = endpointKey;
        this.method = method;
        this.path = path;
        this.implemented = implemented;
    }

    public String getEndpointKey() {
        return endpointKey;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public boolean isImplemented() {
        return implemented;
    }
}
