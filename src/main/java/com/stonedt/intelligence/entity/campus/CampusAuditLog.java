package com.stonedt.intelligence.entity.campus;

import lombok.Data;

import java.util.Date;

@Data
public class CampusAuditLog {

    private Long id;
    private Long auditId;
    private Long operatorUserId;
    private String operatorName;
    private Long operatorDepartmentId;
    private String operationType;
    private String moduleName;
    private String objectType;
    private String objectId;
    private String requestMethod;
    private String requestUri;
    private String requestIp;
    private String requestParams;
    private String beforeValue;
    private String afterValue;
    private Integer operationResult;
    private String failureReason;
    private String taskNo;
    private Date createTime;
}
