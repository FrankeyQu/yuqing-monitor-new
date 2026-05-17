package com.stonedt.intelligence.entity.campus;

import lombok.Data;

import java.util.Date;

@Data
public class CampusAccountTask {

    private Long id;
    private Long taskId;
    private Long accountId;
    private String taskNo;
    private String taskName;
    private String sourceBasis;
    private String authorizationScope;
    private Date focusStartTime;
    private Date focusEndTime;
    private String taskStatus;
    private Long responsibleDepartmentId;
    private Long responsibleUserId;
    private String remark;
    private Integer deleted;
    private Long createUserId;
    private Long updateUserId;
    private Date createTime;
    private Date updateTime;
}
