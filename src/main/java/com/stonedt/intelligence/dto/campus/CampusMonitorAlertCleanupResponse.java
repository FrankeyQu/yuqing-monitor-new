package com.stonedt.intelligence.dto.campus;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CampusMonitorAlertCleanupResponse {

    private Integer successCount = 0;
    private Integer skipCount = 0;
    private Integer failCount = 0;
    private Integer requestedCount = 0;
    private Boolean includeLinkedClue = false;
    private List<CampusMonitorAlertCleanupCandidate> items = new ArrayList<>();
}
