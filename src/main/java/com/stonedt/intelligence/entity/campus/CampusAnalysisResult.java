package com.stonedt.intelligence.entity.campus;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class CampusAnalysisResult {

    private Long id;
    private Long analysisResultId;
    private Long analysisTaskId;
    private String objectType;
    private Long objectId;
    private String analysisType;
    private String sentiment;
    private String suggestedRiskLevel;
    private String summary;
    private String keywords;
    private String similarObjectIds;
    private BigDecimal confidence;
    private String resultPayload;
    private String assistiveLabel;
    private String adoptionStatus;
    private Long reviewerUserId;
    private Date reviewTime;
    private String reviewOpinion;
    private Integer deleted;
    private Long createUserId;
    private Long updateUserId;
    private Date createTime;
    private Date updateTime;
}
