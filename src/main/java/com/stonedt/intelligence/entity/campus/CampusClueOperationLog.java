package com.stonedt.intelligence.entity.campus;

import lombok.Data;

import java.util.Date;

@Data
public class CampusClueOperationLog {

    private Long id;
    private Long logId;
    private Long clueId;
    private String operationType;
    private String operationContent;
    private String beforeValue;
    private String afterValue;
    private Long operatorUserId;
    private String operatorName;
    private Date createTime;
}
