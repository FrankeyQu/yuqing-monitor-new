package com.stonedt.intelligence.service.campus.support;

public class CampusTopicClassification {

    private final String topicCategory;
    private final String topicSubCategory;
    private final String reason;

    public CampusTopicClassification(String topicCategory, String topicSubCategory, String reason) {
        this.topicCategory = topicCategory;
        this.topicSubCategory = topicSubCategory;
        this.reason = reason;
    }

    public String getTopicCategory() {
        return topicCategory;
    }

    public String getTopicSubCategory() {
        return topicSubCategory;
    }

    public String getReason() {
        return reason;
    }
}
