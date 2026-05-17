package com.stonedt.intelligence.entity.campus;

import lombok.Data;

import java.util.Date;

@Data
public class CampusAccount {

    private Long id;
    private Long accountId;
    private String platform;
    private String accountName;
    private String accountUid;
    private String homepageUrl;
    private String accountType;
    private String relatedPersonDesc;
    private Long relatedDepartmentId;
    private String sourceBasis;
    private String taskNo;
    private String authorizationScope;
    private Date focusStartTime;
    private Date focusEndTime;
    private String focusLevel;
    private Long responsibleDepartmentId;
    private Long responsibleUserId;
    private String auditStatus;
    private String auditOpinion;
    private Long auditUserId;
    private Date auditTime;
    private String accountStatus;
    private String tags;
    private String remark;
    private Integer deleted;
    private Long createUserId;
    private Long updateUserId;
    private Date createTime;
    private Date updateTime;
}
