package com.stonedt.intelligence.dto.campus;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.List;

@Data
public class CampusMonitorAiAnalyzeRequest {

    @JsonSerialize(contentUsing = ToStringSerializer.class)
    private List<Long> monitorResultIds;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long monitorTaskId;

    private Integer limit;
}
