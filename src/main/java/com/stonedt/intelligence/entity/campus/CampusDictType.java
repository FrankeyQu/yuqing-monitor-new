package com.stonedt.intelligence.entity.campus;

import lombok.Data;

import java.util.Date;

@Data
public class CampusDictType {

    private Long id;
    private String dictType;
    private String dictName;
    private String description;
    private Integer sortNo;
    private Integer status;
    private Integer deleted;
    private Long createUserId;
    private Long updateUserId;
    private Date createTime;
    private Date updateTime;
}
