package com.stonedt.intelligence.entity.campus;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.Date;

@Data
public class CampusReportJob {

    @JsonSerialize(using = ToStringSerializer.class, as = Long.class)
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class, as = Long.class)
    private Long reportJobId;
    private String jobName;
    private String reportType;
    private String generationMode;
    private String scopeType;
    private String scopeKeywords;
    private String excludeKeywords;
    private String platformScope;
    private String riskLevels;
    private String departmentScope;
    private String monitorTaskIds;
    private String analysisProfile;
    @JsonSerialize(using = ToStringSerializer.class, as = Long.class)
    private Long templateId;
    private String periodRule;
    private String scheduleCron;
    private String outputFormat;
    private String jobStatus;
    private Date lastRunTime;
    private Date nextRunTime;
    private Date scheduleLockUntil;
    @JsonSerialize(using = ToStringSerializer.class, as = Long.class)
    private Long reviewerUserId;
    private String description;
    private Integer deleted;
    @JsonSerialize(using = ToStringSerializer.class, as = Long.class)
    private Long createUserId;
    @JsonSerialize(using = ToStringSerializer.class, as = Long.class)
    private Long updateUserId;
    private Date createTime;
    private Date updateTime;
}
