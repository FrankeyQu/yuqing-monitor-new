package com.stonedt.intelligence.entity.campus;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.Date;

@Data
public class CampusAlert {

    @JsonSerialize(using = ToStringSerializer.class, as = Long.class)
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class, as = Long.class)
    private Long alertId;
    private String alertTitle;
    private String alertContent;
    private String alertSource;
    @JsonSerialize(using = ToStringSerializer.class, as = Long.class)
    private Long sourceObjectId;
    @JsonSerialize(using = ToStringSerializer.class, as = Long.class)
    private Long ruleId;
    private String riskLevel;
    private String matchedKeywords;
    private String evidenceJson;
    private String alertStatus;
    private String handleOpinion;
    @JsonSerialize(using = ToStringSerializer.class, as = Long.class)
    private Long handlerUserId;
    private Date handleTime;
    private Integer deleted;
    private Long createUserId;
    private Long updateUserId;
    private Date createTime;
    private Date updateTime;
}
