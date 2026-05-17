package com.stonedt.intelligence.entity.campus;

import lombok.Data;

import java.util.Date;

@Data
public class CampusDetectionTask {

    private Long id;
    private Long detectionTaskId;
    private Long topicId;
    private String taskName;
    private String objectTypes;
    private String taskStatus;
    private Integer scanWindowHours;
    private Integer autoAlert;
    private Date lastRunTime;
    private Date nextRunTime;
    private String description;
    private Integer deleted;
    private Long createUserId;
    private Long updateUserId;
    private Date createTime;
    private Date updateTime;
}
