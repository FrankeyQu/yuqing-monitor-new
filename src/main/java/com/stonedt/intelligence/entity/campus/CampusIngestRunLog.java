package com.stonedt.intelligence.entity.campus;

import lombok.Data;

import java.util.Date;

@Data
public class CampusIngestRunLog {

    private Long id;
    private Long runId;
    private Long taskId;
    private String runStatus;
    private String triggerType;
    private Date startTime;
    private Date endTime;
    private Long durationMs;
    private Integer fetchedCount;
    private Integer successCount;
    private Integer duplicateCount;
    private Integer invalidCount;
    private Integer detectionTriggerCount;
    private Integer detectionHitCount;
    private Integer detectionAlertCount;
    private String detectionErrorMessage;
    private Integer failCount;
    private String errorMessage;
    private String errorType;
    private Integer retryCount;
    private String schedulerNode;
    private Long createUserId;
    private Date createTime;
}
