package com.stonedt.intelligence.entity.campus;

import lombok.Data;

import java.util.Date;

@Data
public class CampusDetectionTopic {

    private Long id;
    private Long topicId;
    private String topicName;
    private String topicCategory;
    private String keywords;
    private String excludeWords;
    private String platformScope;
    private String sourceScope;
    private String riskLevel;
    private Long responsibleDepartmentId;
    private Integer enabled;
    private String description;
    private Integer deleted;
    private Long createUserId;
    private Long updateUserId;
    private Date createTime;
    private Date updateTime;
}
