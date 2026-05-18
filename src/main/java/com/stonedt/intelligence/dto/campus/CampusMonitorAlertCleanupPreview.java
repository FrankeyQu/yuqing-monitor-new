package com.stonedt.intelligence.dto.campus;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CampusMonitorAlertCleanupPreview {

    private Integer totalCandidateCount = 0;
    private Integer actionableCandidateCount = 0;
    private Integer linkedClueCandidateCount = 0;
    private Integer negativeEvidenceAlertCount = 0;
    private Integer previewLimit = 0;
    private List<CampusMonitorAlertCleanupCandidate> items = new ArrayList<>();
}
