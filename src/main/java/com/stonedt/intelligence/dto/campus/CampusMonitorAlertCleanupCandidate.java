package com.stonedt.intelligence.dto.campus;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.Date;

@Data
public class CampusMonitorAlertCleanupCandidate {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long monitorResultId;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long alertId;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long clueId;

    private String taskName;
    private String alertMode;
    private String platform;
    private String riskLevel;
    private Integer riskScore;
    private String sentiment;
    private String matchedKeywords;
    private String matchedNegativeWords;
    private String title;
    private Date publishTime;
    private Date createTime;
}
