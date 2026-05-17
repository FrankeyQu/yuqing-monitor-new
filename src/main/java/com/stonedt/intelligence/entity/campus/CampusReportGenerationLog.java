package com.stonedt.intelligence.entity.campus;

import lombok.Data;

import java.util.Date;

@Data
public class CampusReportGenerationLog {

    private Long id;
    private Long generationLogId;
    private Long reportJobId;
    private Long reportId;
    private String runStatus;
    private Date startTime;
    private Date endTime;
    private String errorMessage;
    private Long createUserId;
    private Date createTime;
}
