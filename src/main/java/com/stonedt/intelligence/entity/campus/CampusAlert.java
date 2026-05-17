package com.stonedt.intelligence.entity.campus;

import lombok.Data;

import java.util.Date;

@Data
public class CampusAlert {

    private Long id;
    private Long alertId;
    private String alertTitle;
    private String alertContent;
    private String alertSource;
    private Long sourceObjectId;
    private Long ruleId;
    private String riskLevel;
    private String matchedKeywords;
    private String evidenceJson;
    private String alertStatus;
    private String handleOpinion;
    private Long handlerUserId;
    private Date handleTime;
    private Integer deleted;
    private Long createUserId;
    private Long updateUserId;
    private Date createTime;
    private Date updateTime;
}
