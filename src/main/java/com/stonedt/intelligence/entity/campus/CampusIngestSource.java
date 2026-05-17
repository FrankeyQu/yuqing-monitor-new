package com.stonedt.intelligence.entity.campus;

import lombok.Data;

import java.util.Date;

@Data
public class CampusIngestSource {

    private Long id;
    private Long sourceId;
    private String sourceName;
    private String sourceType;
    private String platform;
    private String accessEndpoint;
    private String authorizationBasis;
    private String authorizationScope;
    private Long responsibleDepartmentId;
    private Integer enabled;
    private String remark;
    private Integer deleted;
    private Long createUserId;
    private Long updateUserId;
    private Date createTime;
    private Date updateTime;
}
