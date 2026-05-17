package com.stonedt.intelligence.entity.campus;

import lombok.Data;

import java.util.Date;

@Data
public class CampusMonitorResult {

    private Long id;
    private Long monitorResultId;
    private Long monitorTaskId;
    private Long ingestRecordId;
    private String title;
    private String content;
    private String originalUrl;
    private String platform;
    private String authorName;
    private Date publishTime;
    private String language;
    private String matchedSubjects;
    private String matchedKeywords;
    private String matchedNegativeWords;
    private String sentiment;
    private String riskLevel;
    private Integer riskScore;
    private Integer schoolRelevanceScore;
    private String schoolRelevanceReason;
    private String matchedSchoolTerms;
    private String excludedReason;
    private String topicCategory;
    private String topicSubCategory;
    private String topicReason;
    private String resultStatus;
    private Long alertId;
    private Long clueId;
    private Long likeCount;
    private Long commentCount;
    private Long shareCount;
    private Long collectCount;
    private Long viewCount;
    private Integer deleted;
    private Long createUserId;
    private Long updateUserId;
    private Date createTime;
    private Date updateTime;
}
