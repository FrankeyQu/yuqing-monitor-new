package com.stonedt.intelligence.entity.campus;

import lombok.Data;

import java.util.Date;

@Data
public class CampusDictItem {

    private Long id;
    private String dictType;
    private String itemCode;
    private String itemName;
    private String itemValue;
    private String description;
    private Integer sortNo;
    private Integer status;
    private Integer deleted;
    private Long createUserId;
    private Long updateUserId;
    private Date createTime;
    private Date updateTime;
}
