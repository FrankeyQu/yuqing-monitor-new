package com.stonedt.intelligence.entity.campus;

import lombok.Data;

import java.util.Date;

@Data
public class CampusDetectionRule {

    private Long id;
    private Long ruleId;
    private Long topicId;
    private String ruleName;
    private String ruleType;
    private String ruleCondition;
    private String excludeWords;
    private String riskLevel;
    private Integer enabled;
    private Integer sortNo;
    private String description;
    private Integer deleted;
    private Long createUserId;
    private Long updateUserId;
    private Date createTime;
    private Date updateTime;
}
