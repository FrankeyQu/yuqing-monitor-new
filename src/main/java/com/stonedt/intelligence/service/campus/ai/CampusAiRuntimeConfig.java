package com.stonedt.intelligence.service.campus.ai;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CampusAiRuntimeConfig {

    private String featureCode;
    private String providerCode;
    private String modelCode;
    private String endpoint;
    private String credentialRef;
    private String credentialValue;
    private Integer timeoutMs;
    private Integer maxTokens;
    private BigDecimal temperature;
    private boolean featureEnabled;
    private boolean providerEnabled;
    private boolean modelEnabled;
    private boolean logPrompt;
    private String failureStrategy;
}
