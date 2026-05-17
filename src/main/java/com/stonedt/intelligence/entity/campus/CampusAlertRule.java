package com.stonedt.intelligence.entity.campus;

import lombok.Data;

import java.util.Date;

@Data
public class CampusAlertRule {

    private Long id;
    private Long ruleId;
    private String ruleName;
    private String ruleType;
    private String ruleCondition;
    private String riskLevel;
    private Integer enabled;
    private String description;
    private Integer deleted;
    private Long createUserId;
    private Long updateUserId;
    private Date createTime;
    private Date updateTime;
}
