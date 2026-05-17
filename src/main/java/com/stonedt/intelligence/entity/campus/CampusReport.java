package com.stonedt.intelligence.entity.campus;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.Date;

@Data
public class CampusReport {

    @JsonSerialize(using = ToStringSerializer.class, as = Long.class)
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class, as = Long.class)
    private Long reportId;
    private String reportTitle;
    private String reportType;
    private String reportStatus;
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
    @JsonSerialize(using = ToStringSerializer.class, as = Long.class)
    private Long eventId;
    private Date periodStartTime;
    private Date periodEndTime;
    private String reportSummary;
    private String reportContent;
    private String reportFormat;
    private String fileName;
    private String filePath;
    private String aiModel;
    private String aiPromptSnapshot;
    @JsonSerialize(using = ToStringSerializer.class, as = Long.class)
    private Long generatedBy;
    private Date generateTime;
    @JsonSerialize(using = ToStringSerializer.class, as = Long.class)
    private Long archiveUserId;
    private Date archiveTime;
    private String archiveOpinion;
    private Integer deleted;
    @JsonSerialize(using = ToStringSerializer.class, as = Long.class)
    private Long createUserId;
    @JsonSerialize(using = ToStringSerializer.class, as = Long.class)
    private Long updateUserId;
    private Date createTime;
    private Date updateTime;
}
