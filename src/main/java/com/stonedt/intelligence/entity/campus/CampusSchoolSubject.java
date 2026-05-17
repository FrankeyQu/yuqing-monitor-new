package com.stonedt.intelligence.entity.campus;

import lombok.Data;

import java.util.Date;

@Data
public class CampusSchoolSubject {

    private Long id;
    private Long schoolId;
    private String schoolName;
    private String schoolAliases;
    private String region;
    private String educationStage;
    private String schoolType;
    private Integer status;
    private String remark;
    private Integer deleted;
    private Long createUserId;
    private Long updateUserId;
    private Date createTime;
    private Date updateTime;
}
