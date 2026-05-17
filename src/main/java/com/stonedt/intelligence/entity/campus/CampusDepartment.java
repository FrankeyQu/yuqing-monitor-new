package com.stonedt.intelligence.entity.campus;

import lombok.Data;

import java.util.Date;

@Data
public class CampusDepartment {

    private Long id;
    private Long departmentId;
    private Long parentId;
    private String departmentName;
    private String departmentCode;
    private String departmentType;
    private Long leaderUserId;
    private String contactPhone;
    private Integer sortNo;
    private Integer status;
    private Integer deleted;
    private Long createUserId;
    private Long updateUserId;
    private Date createTime;
    private Date updateTime;
}
