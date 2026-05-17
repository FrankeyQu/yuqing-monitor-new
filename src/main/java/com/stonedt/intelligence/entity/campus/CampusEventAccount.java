package com.stonedt.intelligence.entity.campus;

import lombok.Data;

import java.util.Date;

@Data
public class CampusEventAccount {

    private Long id;
    private Long relationId;
    private Long eventId;
    private Long accountId;
    private Integer deleted;
    private Long createUserId;
    private Date createTime;
}
