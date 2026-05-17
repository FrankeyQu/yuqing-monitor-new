package com.stonedt.intelligence.entity.campus;

import lombok.Data;

import java.util.Date;

@Data
public class CampusAiCallLog {

    private Long id;
    private Long callId;
    private String featureCode;
    private String providerCode;
    private String modelCode;
    private String endpoint;
    private Date requestTime;
    private Long durationMs;
    private String callStatus;
    private Integer httpStatus;
    private String errorType;
    private String errorMessage;
    private Integer promptTokens;
    private Integer completionTokens;
    private Integer totalTokens;
    private Integer quotaUnits;
    private String requestSnapshot;
    private String responseSnapshot;
    private Integer deleted;
    private Date createTime;
}
