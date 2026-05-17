package com.stonedt.intelligence.entity.campus;

import lombok.Data;

import java.util.Date;

@Data
public class CampusIngestTask {

    private Long id;
    private Long taskId;
    private Long sourceId;
    private String taskName;
    private String targetType;
    private String adapterType;
    private String scheduleCron;
    private Integer scheduleEnabled;
    private String fetchConfig;
    private String taskStatus;
    private Date lastRunTime;
    private Date nextRunTime;
    private Date scheduleLockUntil;
    private Integer maxRetryCount;
    private Integer retryIntervalMinutes;
    private Integer consecutiveFailCount;
    private Integer currentRetryCount;
    private String lastErrorType;
    private Integer autoDetectEnabled;
    private String detectionTaskIds;
    private Integer dailyQuotaLimit;
    private Integer dailyQuotaUsed;
    private Date quotaStatDate;
    private Integer autoPauseAfterFailCount;
    private String governanceRemark;
    private String authorizationScope;
    private Integer retentionDays;
    private Integer deleted;
    private Long createUserId;
    private Long updateUserId;
    private Date createTime;
    private Date updateTime;
}
