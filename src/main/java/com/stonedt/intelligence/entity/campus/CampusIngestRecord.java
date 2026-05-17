package com.stonedt.intelligence.entity.campus;

import lombok.Data;

import java.util.Date;

@Data
public class CampusIngestRecord {

    private Long id;
    private Long recordId;
    private Long runId;
    private Long sourceId;
    private Long taskId;
    private String externalId;
    private String contentHash;
    private String platform;
    private String contentType;
    private String title;
    private String content;
    private String originalUrl;
    private Date publishTime;
    private String authorName;
    private Long accountId;
    private Long accountTaskId;
    private String keywords;
    private String riskLevel;
    private String sentiment;
    private String language;
    private Long likeCount;
    private Long commentCount;
    private Long shareCount;
    private Long collectCount;
    private Long viewCount;
    private String rawData;
    private String normalizedStatus;
    private String targetType;
    private Long targetId;
    private String errorMessage;
    private Integer deleted;
    private Long createUserId;
    private Long updateUserId;
    private Date createTime;
    private Date updateTime;
}
