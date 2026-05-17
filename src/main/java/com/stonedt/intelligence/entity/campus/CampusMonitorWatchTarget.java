package com.stonedt.intelligence.entity.campus;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.Date;

@Data
public class CampusMonitorWatchTarget {

    @JsonSerialize(using = ToStringSerializer.class, as = Long.class)
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class, as = Long.class)
    private Long targetId;
    @JsonSerialize(using = ToStringSerializer.class, as = Long.class)
    private Long monitorTaskId;
    private String targetType;
    private String platform;
    @JsonSerialize(using = ToStringSerializer.class, as = Long.class)
    private Long accountId;
    private String accountName;
    private String accountUid;
    private String linkUrl;
    private String sourceObjectType;
    @JsonSerialize(using = ToStringSerializer.class, as = Long.class)
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
