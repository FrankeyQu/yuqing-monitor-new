package com.stonedt.intelligence.service.campus.ai;

import lombok.Data;

@Data
public class CampusAiChatResponse {

    private String content;
    private Integer httpStatus;
    private Integer promptTokens;
    private Integer completionTokens;
    private Integer totalTokens;
}
