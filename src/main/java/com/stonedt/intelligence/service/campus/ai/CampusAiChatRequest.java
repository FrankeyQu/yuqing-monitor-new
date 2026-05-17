package com.stonedt.intelligence.service.campus.ai;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CampusAiChatRequest {

    private String featureCode;
    private String systemPrompt;
    private String userPrompt;
    private Integer maxTokens;
    private BigDecimal temperature;
    private Boolean stream;
}
