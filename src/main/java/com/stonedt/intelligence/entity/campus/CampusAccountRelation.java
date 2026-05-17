package com.stonedt.intelligence.entity.campus;

import lombok.Data;

import java.util.Date;

@Data
public class CampusAccountRelation {

    private Long id;
    private Long relationId;
    private Long accountId;
    private String relationType;
    private Long relationObjectId;
    private String relationDesc;
    private Integer deleted;
    private Long createUserId;
    private Date createTime;
}
