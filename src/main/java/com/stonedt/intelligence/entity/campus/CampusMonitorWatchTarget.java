package com.stonedt.intelligence.entity.campus;

import lombok.Data;

import java.util.Date;

@Data
public class CampusMonitorWatchTarget {

    private Long id;
    private Long targetId;
    private Long monitorTaskId;
    private String targetType;
    private String platform;
    private Long accountId;
    private String accountName;
    private String accountUid;
    private String linkUrl;
    private String sourceObjectType;
    private Long sourceObjectId;
    private String authorizationScope;
    private String keywordScope;
    private String targetStatus;
    private String remark;
    private Integer deleted;
    private Long createUserId;
    private Long updateUserId;
    private Date createTime;
    private Date updateTime;
}
