package com.stonedt.intelligence.entity.campus;

import lombok.Data;

import java.util.Date;

@Data
public class CampusReportJob {

    private Long id;
    private Long reportJobId;
    private String jobName;
    private String reportType;
    private Long templateId;
    private String periodRule;
    private String scheduleCron;
    private String outputFormat;
    private String jobStatus;
    private Date lastRunTime;
    private Date nextRunTime;
    private Long reviewerUserId;
    private String description;
    private Integer deleted;
    private Long createUserId;
    private Long updateUserId;
    private Date createTime;
    private Date updateTime;
}
