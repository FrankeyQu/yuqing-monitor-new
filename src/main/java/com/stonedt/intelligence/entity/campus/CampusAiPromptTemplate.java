package com.stonedt.intelligence.entity.campus;

import lombok.Data;

import java.util.Date;

@Data
public class CampusAiPromptTemplate {

    private Long id;
    private Long templateId;
    private String featureCode;
    private String templateName;
    private String templateVersion;
    private String systemPrompt;
    private String userPrompt;
    private String outputFormat;
    private Integer enabled;
    private String remark;
    private Integer deleted;
    private Long createUserId;
    private Long updateUserId;
    private Date createTime;
    private Date updateTime;
}
