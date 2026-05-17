package com.stonedt.intelligence.entity.campus;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.Date;

@Data
public class CampusReportGenerationLog {

    @JsonSerialize(using = ToStringSerializer.class, as = Long.class)
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class, as = Long.class)
    private Long generationLogId;
    @JsonSerialize(using = ToStringSerializer.class, as = Long.class)
    private Long reportJobId;
    @JsonSerialize(using = ToStringSerializer.class, as = Long.class)
    private Long reportId;
    private String generationMode;
    private String runStatus;
    private Date startTime;
    private Date endTime;
    private Long durationMs;
    private String errorMessage;
    @JsonSerialize(using = ToStringSerializer.class, as = Long.class)
    private Long createUserId;
    private Date createTime;
}
