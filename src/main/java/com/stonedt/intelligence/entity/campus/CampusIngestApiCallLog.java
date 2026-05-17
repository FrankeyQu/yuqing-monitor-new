package com.stonedt.intelligence.entity.campus;

import lombok.Data;

import java.util.Date;

@Data
public class CampusIngestApiCallLog {

    private Long id;
    private Long callId;
    private Long runId;
    private Long taskId;
    private Long sourceId;
    private String provider;
    private String endpointKey;
    private String credentialRef;
    private Date requestTime;
    private Long durationMs;
    private String callStatus;
    private Integer httpStatus;
    private String errorType;
    private String errorMessage;
    private Integer costUnits;
    private Date createTime;
}
