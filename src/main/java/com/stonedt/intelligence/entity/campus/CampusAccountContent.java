package com.stonedt.intelligence.entity.campus;

import lombok.Data;

import java.util.Date;

@Data
public class CampusAccountContent {

    private Long id;
    private Long contentId;
    private Long accountId;
    private Long taskId;
    private String platform;
    private String contentType;
    private String contentTitle;
    private String contentText;
    private String originalUrl;
    private Date publishTime;
    private Date captureTime;
    private String riskLevel;
    private String sentiment;
    private String keywords;
    private Long likeCount;
    private Long commentCount;
    private Long shareCount;
    private Long collectCount;
    private Long viewCount;
    private String rawData;
    private Long clueId;
    private Integer deleted;
    private Long createUserId;
    private Long updateUserId;
    private Date createTime;
    private Date updateTime;
}
