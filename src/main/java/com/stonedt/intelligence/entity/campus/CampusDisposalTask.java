package com.stonedt.intelligence.entity.campus;

import lombok.Data;

import java.util.Date;

@Data
public class CampusDisposalTask {

    private Long id;
    private Long disposalTaskId;
    private Long eventId;
    private String taskTitle;
    private Long assignedDepartmentId;
    private Long assignedUserId;
    private String disposalRequirement;
    private Date dueTime;
    private String taskStatus;
    private String feedbackSummary;
    private Integer deleted;
    private Long createUserId;
    private Long updateUserId;
    private Date createTime;
    private Date updateTime;
}
