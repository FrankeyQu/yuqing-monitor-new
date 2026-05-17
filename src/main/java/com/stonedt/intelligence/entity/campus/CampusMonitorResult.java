package com.stonedt.intelligence.entity.campus;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.Date;

@Data
public class CampusMonitorResult {

    @JsonSerialize(using = ToStringSerializer.class, as = Long.class)
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class, as = Long.class)
    private Long monitorResultId;
    @JsonSerialize(using = ToStringSerializer.class, as = Long.class)
    private Long monitorTaskId;
    @JsonSerialize(using = ToStringSerializer.class, as = Long.class)
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
    private String aiSummary;
    private String aiHitRecommendation;
    private String aiHitReason;
    private Integer aiConfidence;
    private Date aiAnalysisTime;
    private String aiProviderCode;
    private String aiModelCode;
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
    @JsonSerialize(using = ToStringSerializer.class, as = Long.class)
    private Long alertId;
    @JsonSerialize(using = ToStringSerializer.class, as = Long.class)
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
