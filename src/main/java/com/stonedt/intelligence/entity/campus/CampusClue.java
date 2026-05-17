package com.stonedt.intelligence.entity.campus;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.Date;

@Data
public class CampusClue {

    @JsonSerialize(using = ToStringSerializer.class, as = Long.class)
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class, as = Long.class)
    private Long clueId;
    private String clueTitle;
    private String clueContent;
    private String clueSource;
    private String sourcePlatform;
    private String originalUrl;
    private Date publishTime;
    private Date discoverTime;
    @JsonSerialize(using = ToStringSerializer.class, as = Long.class)
    private Long involvedDepartmentId;
    private String involvedAccount;
    private String keywords;
    private String riskLevel;
    private Integer schoolRelevanceScore;
    private String schoolRelevanceReason;
    private String matchedSchoolTerms;
    private String excludedReason;
    private String topicCategory;
    private String topicSubCategory;
    private String topicReason;
    private String sentiment;
    private String language;
    private String clueStatus;
    private String judgeOpinion;
    @JsonSerialize(using = ToStringSerializer.class, as = Long.class)
    private Long judgeUserId;
    private Date judgeTime;
    @JsonSerialize(using = ToStringSerializer.class, as = Long.class)
    private Long eventId;
    private String duplicateKey;
    private String remark;
    private Integer deleted;
    private Long createUserId;
    private Long updateUserId;
    private Date createTime;
    private Date updateTime;
}
