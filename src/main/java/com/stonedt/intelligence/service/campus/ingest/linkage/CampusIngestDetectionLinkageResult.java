package com.stonedt.intelligence.service.campus.ingest.linkage;

import org.apache.commons.lang3.StringUtils;

public class CampusIngestDetectionLinkageResult {

    private int triggerCount;
    private int hitCount;
    private int alertCount;
    private String errorMessage;

    public void addRunResult(Integer hitCount, Integer alertCount) {
        this.triggerCount++;
        this.hitCount += hitCount == null ? 0 : hitCount;
        this.alertCount += alertCount == null ? 0 : alertCount;
    }

    public void appendError(String message) {
        if (StringUtils.isBlank(message)) {
            return;
        }
        if (StringUtils.isBlank(errorMessage)) {
            errorMessage = StringUtils.left(message, 2048);
            return;
        }
        errorMessage = StringUtils.left(errorMessage + "; " + message, 2048);
    }

    public int getTriggerCount() {
        return triggerCount;
    }

    public int getHitCount() {
        return hitCount;
    }

    public int getAlertCount() {
        return alertCount;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean hasResult() {
        return triggerCount > 0 || hitCount > 0 || alertCount > 0 || StringUtils.isNotBlank(errorMessage);
    }
}
