package com.stonedt.intelligence.dto.campus;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CampusMonitorTaskAiDiagnosis {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long monitorTaskId;
    private String taskName;
    private String summary;
    private List<String> keywordSuggestions = new ArrayList<>();
    private List<String> negativeWordSuggestions = new ArrayList<>();
    private List<String> excludeWordSuggestions = new ArrayList<>();
    private List<String> platformSuggestions = new ArrayList<>();
    private String frequencySuggestion;
    private String alertModeSuggestion;
    private List<String> risks = new ArrayList<>();
    private List<String> suggestions = new ArrayList<>();
    private String rawText;
    private String providerCode;
    private String modelCode;
}
