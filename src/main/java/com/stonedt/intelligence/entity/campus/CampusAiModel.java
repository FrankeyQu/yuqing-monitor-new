package com.stonedt.intelligence.entity.campus;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class CampusAiModel {

    private Long id;
    private Long modelId;
    private String providerCode;
    private String modelCode;
    private String modelName;
    private Integer contextLength;
    private BigDecimal defaultTemperature;
    private Integer defaultMaxTokens;
    private Integer supportStream;
    private Integer enabled;
    private String remark;
    private Integer deleted;
    private Long createUserId;
    private Long updateUserId;
    private Date createTime;
    private Date updateTime;
}
