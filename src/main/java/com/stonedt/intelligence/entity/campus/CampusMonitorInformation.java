package com.stonedt.intelligence.entity.campus;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.Date;

@Data
public class CampusMonitorInformation {

    private String infoType;
    @JsonSerialize(using = ToStringSerializer.class, as = Long.class)
    private Long infoId;
    @JsonSerialize(using = ToStringSerializer.class, as = Long.class)
    private Long monitorResultId;
    @JsonSerialize(using = ToStringSerializer.class, as = Long.class)
    private Long clueId;
    @JsonSerialize(using = ToStringSerializer.class, as = Long.class)
    private Long monitorTaskId;
    @JsonSerialize(using = ToStringSerializer.class, as = Long.class)
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
    private Date collectTime;
    private String publishTimeStatus;
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
    @JsonSerialize(using = ToStringSerializer.class, as = Long.class)
    private Long alertId;
    private Boolean riskMarked;
    private Long likeCount;
    private Long commentCount;
    private Long shareCount;
    private Long collectCount;
    private Long viewCount;
}
