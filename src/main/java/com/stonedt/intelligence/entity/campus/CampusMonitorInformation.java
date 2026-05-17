package com.stonedt.intelligence.entity.campus;

import lombok.Data;

import java.util.Date;

@Data
public class CampusMonitorInformation {

    private String infoType;
    private Long infoId;
    private Long monitorResultId;
    private Long clueId;
    private Long monitorTaskId;
    private Long ingestRecordId;
    private String title;
    private String content;
    private String summary;
    private String contentCaptureStatus;
    private String contentCaptureLabel;
    private String originalUrl;
    private String platform;
    private String sourcePlatform;
    private String sourceSubPlatform;
    private String authorName;
    private String involvedAccount;
    private Date publishTime;
    private Date discoverTime;
    private Date createTime;
    private Date infoTime;
    private String language;
    private String matchedSubjects;
    private String matchedKeywords;
    private String matchedNegativeWords;
    private String keywords;
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
    private String clueStatus;
    private Long alertId;
    private Long likeCount;
    private Long commentCount;
    private Long shareCount;
    private Long collectCount;
    private Long viewCount;
}
