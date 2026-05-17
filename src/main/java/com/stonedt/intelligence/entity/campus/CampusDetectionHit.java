package com.stonedt.intelligence.entity.campus;

import lombok.Data;

import java.util.Date;

@Data
public class CampusDetectionHit {

    private Long id;
    private Long hitId;
    private Long detectionTaskId;
    private Long topicId;
    private Long ruleId;
    private String objectType;
    private Long objectId;
    private String objectTitle;
    private String platform;
    private String matchedKeywords;
    private String riskLevel;
    private String hitContent;
    private String hitStatus;
    private Long alertId;
    private Long clueId;
    private Integer deleted;
    private Long createUserId;
    private Long updateUserId;
    private Date createTime;
    private Date updateTime;
}
