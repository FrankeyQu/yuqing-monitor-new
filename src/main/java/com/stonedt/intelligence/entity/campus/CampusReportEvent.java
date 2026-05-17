package com.stonedt.intelligence.entity.campus;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.Date;

@Data
public class CampusReportEvent {

    @JsonSerialize(using = ToStringSerializer.class, as = Long.class)
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class, as = Long.class)
    private Long relationId;
    @JsonSerialize(using = ToStringSerializer.class, as = Long.class)
    private Long reportId;
    @JsonSerialize(using = ToStringSerializer.class, as = Long.class)
    private Long eventId;
    private Integer deleted;
    @JsonSerialize(using = ToStringSerializer.class, as = Long.class)
    private Long createUserId;
    private Date createTime;
}
