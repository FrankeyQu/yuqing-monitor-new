package com.stonedt.intelligence.entity.campus;

import lombok.Data;

import java.util.Date;

@Data
public class CampusAnalysisTask {

    private Long id;
    private Long analysisTaskId;
    private String objectType;
    private Long objectId;
    private String analysisType;
    private String taskStatus;
    private String requestPayload;
    private String modelProvider;
    private String modelName;
    private String errorMessage;
    private Integer deleted;
    private Long createUserId;
    private Long updateUserId;
    private Date createTime;
    private Date updateTime;
}
