package com.stonedt.intelligence.service.campus.ingest.scheduler;

import org.apache.commons.lang3.StringUtils;

public class CampusIngestRunContext {

    public static final String TRIGGER_MANUAL = "manual";
    public static final String TRIGGER_SCHEDULE = "schedule";
    public static final String TRIGGER_RETRY = "retry";

    private final String triggerType;
    private final Integer retryCount;
    private final String schedulerNode;

    public CampusIngestRunContext(String triggerType, Integer retryCount, String schedulerNode) {
        this.triggerType = StringUtils.defaultIfBlank(triggerType, TRIGGER_MANUAL);
        this.retryCount = retryCount == null ? 0 : Math.max(retryCount, 0);
        this.schedulerNode = schedulerNode;
    }

    public static CampusIngestRunContext manual() {
        return new CampusIngestRunContext(TRIGGER_MANUAL, 0, null);
    }

    public static CampusIngestRunContext scheduled(String triggerType, Integer retryCount, String schedulerNode) {
        return new CampusIngestRunContext(triggerType, retryCount, schedulerNode);
    }

    public boolean isManual() {
        return TRIGGER_MANUAL.equals(triggerType);
    }

    public String getTriggerType() {
        return triggerType;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public String getSchedulerNode() {
        return schedulerNode;
    }
}
