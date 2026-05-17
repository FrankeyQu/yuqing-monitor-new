package com.stonedt.intelligence.entity.campus;

import lombok.Data;

import java.util.Date;

@Data
public class CampusEvent {

    private Long id;
    private Long eventId;
    private String eventTitle;
    private String eventType;
    private String eventSummary;
    private Date firstPublishTime;
    private Date discoverTime;
    private String riskLevel;
    private String impactScope;
    private Long involvedDepartmentId;
    private Integer currentHeat;
    private String eventStatus;
    private String disposalRequirement;
    private String archiveConclusion;
    private Integer deleted;
    private Long createUserId;
    private Long updateUserId;
    private Date createTime;
    private Date updateTime;
}
