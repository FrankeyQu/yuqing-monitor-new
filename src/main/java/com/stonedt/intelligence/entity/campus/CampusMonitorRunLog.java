package com.stonedt.intelligence.entity.campus;

import lombok.Data;

import java.util.Date;

@Data
public class CampusMonitorRunLog {

    private Long id;
    private Long runLogId;
    private Long monitorTaskId;
    private String runStatus;
    private String triggerType;
    private Date startTime;
    private Date endTime;
    private Integer scannedCount;
    private Integer matchCount;
    private Integer negativeCount;
    private Integer alertCount;
    private String errorMessage;
    private String schedulerNode;
    private Long createUserId;
    private Date createTime;
}
