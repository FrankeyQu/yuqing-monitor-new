package com.stonedt.intelligence.entity.campus;

import lombok.Data;

import java.util.Date;

@Data
public class CampusPublicWebWhitelist {

    private Long id;
    private Long whitelistId;
    private String siteName;
    private String siteDomain;
    private String baseUrl;
    private String allowedPathPrefix;
    private String authorizationBasis;
    private String authorizationScope;
    private String robotsPolicy;
    private Integer rateLimitSeconds;
    private Integer maxDepth;
    private Long responsibleDepartmentId;
    private Integer enabled;
    private String remark;
    private Integer deleted;
    private Long createUserId;
    private Long updateUserId;
    private Date createTime;
    private Date updateTime;
}
