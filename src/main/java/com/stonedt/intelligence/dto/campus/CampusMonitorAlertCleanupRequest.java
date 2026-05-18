package com.stonedt.intelligence.dto.campus;

import lombok.Data;

@Data
public class CampusMonitorAlertCleanupRequest {

    private Integer maxCount;
    private Boolean includeLinkedClue;
    private String confirmText;
}
