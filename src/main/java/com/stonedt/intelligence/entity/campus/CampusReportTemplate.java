package com.stonedt.intelligence.entity.campus;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.Date;

@Data
public class CampusReportTemplate {

    @JsonSerialize(using = ToStringSerializer.class, as = Long.class)
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class, as = Long.class)
    private Long templateId;
    private String templateName;
    private String reportType;
    private String templateContent;
    private Integer status;
    private String remark;
    private Integer deleted;
    @JsonSerialize(using = ToStringSerializer.class, as = Long.class)
    private Long createUserId;
    @JsonSerialize(using = ToStringSerializer.class, as = Long.class)
    private Long updateUserId;
    private Date createTime;
    private Date updateTime;
}
