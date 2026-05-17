package com.stonedt.intelligence.entity.campus;

import lombok.Data;

import java.util.Date;

@Data
public class CampusPermissionApi {

    private Long id;
    private Long apiId;
    private String apiCode;
    private String apiName;
    private String moduleName;
    private String requestMethod;
    private String requestPath;
    private Integer status;
    private String remark;
    private Integer deleted;
    private Long createUserId;
    private Long updateUserId;
    private Date createTime;
    private Date updateTime;
}
