package com.stonedt.intelligence.dto.campus;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CampusMonitorAiAnalyzeResponse {

    private Integer successCount = 0;
    private Integer failCount = 0;
    private Integer skipCount = 0;
    private List<Item> items = new ArrayList<>();

    public void add(Item item) {
        if (item == null) {
            return;
        }
        items.add(item);
        if (Boolean.TRUE.equals(item.getSuccess())) {
            successCount++;
        } else if (Boolean.TRUE.equals(item.getSkipped())) {
            skipCount++;
        } else {
            failCount++;
        }
    }

    @Data
    public static class Item {
        @JsonSerialize(using = ToStringSerializer.class)
        private Long monitorResultId;
        private Boolean success;
        private Boolean skipped;
        private String message;
        private String sentiment;
        private String aiSummary;
        private String aiHitRecommendation;
        private String aiHitReason;
        private Integer aiConfidence;
        private String riskLevel;
        private Integer riskScore;
        private Integer schoolRelevanceScore;
        private String topicCategory;
        private String topicSubCategory;
    }
}
