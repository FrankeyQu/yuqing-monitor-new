package com.stonedt.intelligence.entity.campus;

import lombok.Data;

import java.util.Date;

@Data
public class CampusPermissionRole {

    private Long id;
    private Long roleId;
    private String roleCode;
    private String roleName;
    private String roleType;
    private String dataScope;
    private Integer status;
    private String remark;
    private Integer deleted;
    private Long createUserId;
    private Long updateUserId;
    private Date createTime;
    private Date updateTime;
}
