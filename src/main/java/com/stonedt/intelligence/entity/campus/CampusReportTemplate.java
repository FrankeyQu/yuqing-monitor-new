package com.stonedt.intelligence.entity.campus;

import lombok.Data;

import java.util.Date;

@Data
public class CampusReportTemplate {

    private Long id;
    private Long templateId;
    private String templateName;
    private String reportType;
    private String templateContent;
    private Integer status;
    private String remark;
    private Integer deleted;
    private Long createUserId;
    private Long updateUserId;
    private Date createTime;
    private Date updateTime;
}
