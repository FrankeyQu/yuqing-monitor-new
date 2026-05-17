package com.stonedt.intelligence.entity.campus;

import lombok.Data;

import java.util.Date;

@Data
public class CampusReportEvent {

    private Long id;
    private Long relationId;
    private Long reportId;
    private Long eventId;
    private Integer deleted;
    private Long createUserId;
    private Date createTime;
}
