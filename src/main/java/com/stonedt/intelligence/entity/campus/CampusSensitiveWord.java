package com.stonedt.intelligence.entity.campus;

import lombok.Data;

import java.util.Date;

@Data
public class CampusSensitiveWord {

    private Long id;
    private Long wordId;
    private String wordText;
    private String wordCategory;
    private String riskLevel;
    private String matchType;
    private Integer status;
    private String remark;
    private Integer deleted;
    private Long createUserId;
    private Long updateUserId;
    private Date createTime;
    private Date updateTime;
}
