package com.stonedt.intelligence.entity.campus;

import lombok.Data;

import java.util.Date;

@Data
public class CampusEventClue {

    private Long id;
    private Long relationId;
    private Long eventId;
    private Long clueId;
    private Integer deleted;
    private Long createUserId;
    private Date createTime;
}
