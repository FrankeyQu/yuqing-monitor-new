package com.stonedt.intelligence.entity.campus;

import lombok.Data;

import java.util.Date;

@Data
public class CampusMonitorTask {

    private Long id;
    private Long monitorTaskId;
    private String taskName;
    private String monitorSubject;
    private String subjectAliases;
    private String keywords;
    private String keywordsI18n;
    private String negativeWords;
    private String negativeWordsI18n;
    private String excludeWords;
    private String excludeWordsI18n;
    private String platformScope;
    private Integer scanFrequencyMinutes;
    private Integer scheduleEnabled;
    private Integer displayEnabled;
    private Integer autoIngestEnabled;
    private String alertMode;
    private String taskStatus;
    private Date lastRunTime;
    private Long lastRunLogId;
    private Date lastCollectTime;
    private Integer lastMatchCount;
    private Integer displayResultCount;
    private String lastErrorMessage;
    private String ingestCapabilityStatus;
    private Date nextRunTime;
    private Date scheduleLockUntil;
    private String ingestTaskIds;
    private String ingestTaskNames;
    private String remark;
    private Integer deleted;
    private Long createUserId;
    private Long updateUserId;
    private Date createTime;
    private Date updateTime;
}
