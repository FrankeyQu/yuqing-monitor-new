package com.stonedt.intelligence.entity.campus;

import lombok.Data;

import java.util.Date;

@Data
public class CampusDetectionRunLog {

    private Long id;
    private Long runLogId;
    private Long detectionTaskId;
    private String runStatus;
    private String triggerType;
    private String triggerObjectType;
    private Long triggerObjectId;
    private Date startTime;
    private Date endTime;
    private Integer scannedCount;
    private Integer hitCount;
    private Integer alertCount;
    private String errorMessage;
    private Long createUserId;
    private Date createTime;
}
