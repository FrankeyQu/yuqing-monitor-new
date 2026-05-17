package com.stonedt.intelligence.entity.campus;

import lombok.Data;

import java.util.Date;

@Data
public class CampusAiFeatureBinding {

    private Long id;
    private Long bindingId;
    private String featureCode;
    private String featureName;
    private String featureType;
    private String providerCode;
    private String modelCode;
    private String fallbackProviderCode;
    private String fallbackModelCode;
    private Integer enabled;
    private String failureStrategy;
    private Integer timeoutMs;
    private Integer dailyQuotaLimit;
    private Integer quotaUsedToday;
    private Date quotaStatDate;
    private Integer logPrompt;
    private String remark;
    private Integer deleted;
    private Long createUserId;
    private Long updateUserId;
    private Date createTime;
    private Date updateTime;
}
