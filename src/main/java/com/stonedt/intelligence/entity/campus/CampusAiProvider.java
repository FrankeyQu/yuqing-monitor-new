package com.stonedt.intelligence.entity.campus;

import lombok.Data;

import java.util.Date;

@Data
public class CampusAiProvider {

    private Long id;
    private Long providerId;
    private String providerCode;
    private String providerName;
    private String providerType;
    private String baseUrl;
    private String authType;
    private String credentialRef;
    private Integer enabled;
    private Integer timeoutMs;
    private Integer maxRetries;
    private Integer dailyQuotaLimit;
    private Integer quotaUsedToday;
    private Date quotaStatDate;
    private String remark;
    private Integer deleted;
    private Long createUserId;
    private Long updateUserId;
    private Date createTime;
    private Date updateTime;
}
