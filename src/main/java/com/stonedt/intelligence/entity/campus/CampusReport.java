package com.stonedt.intelligence.entity.campus;

import lombok.Data;

import java.util.Date;

@Data
public class CampusReport {

    private Long id;
    private Long reportId;
    private String reportTitle;
    private String reportType;
    private String reportStatus;
    private Long templateId;
    private Long eventId;
    private Date periodStartTime;
    private Date periodEndTime;
    private String reportSummary;
    private String reportContent;
    private String reportFormat;
    private String fileName;
    private String filePath;
    private Long generatedBy;
    private Date generateTime;
    private Long archiveUserId;
    private Date archiveTime;
    private String archiveOpinion;
    private Integer deleted;
    private Long createUserId;
    private Long updateUserId;
    private Date createTime;
    private Date updateTime;
}
