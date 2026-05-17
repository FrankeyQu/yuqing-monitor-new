package com.stonedt.intelligence.service.campus.ingest;

import com.stonedt.intelligence.entity.campus.CampusIngestSource;
import com.stonedt.intelligence.entity.campus.CampusIngestTask;

public class CampusIngestFetchRequest {

    private Long runId;
    private CampusIngestTask task;
    private CampusIngestSource source;
    private Long operatorUserId;

    public Long getRunId() {
        return runId;
    }

    public void setRunId(Long runId) {
        this.runId = runId;
    }

    public CampusIngestTask getTask() {
        return task;
    }

    public void setTask(CampusIngestTask task) {
        this.task = task;
    }

    public CampusIngestSource getSource() {
        return source;
    }

    public void setSource(CampusIngestSource source) {
        this.source = source;
    }

    public Long getOperatorUserId() {
        return operatorUserId;
    }

    public void setOperatorUserId(Long operatorUserId) {
        this.operatorUserId = operatorUserId;
    }
}
