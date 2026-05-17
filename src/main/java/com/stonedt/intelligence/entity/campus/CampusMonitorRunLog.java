package com.stonedt.intelligence.entity.campus;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.Date;

@Data
public class CampusMonitorRunLog {

    @JsonSerialize(using = ToStringSerializer.class, as = Long.class)
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class, as = Long.class)
    private Long runLogId;
    @JsonSerialize(using = ToStringSerializer.class, as = Long.class)
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
