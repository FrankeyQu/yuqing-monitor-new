package com.stonedt.intelligence.entity.campus;

import lombok.Data;

import java.util.Date;

@Data
public class CampusClueAttachment {

    private Long id;
    private Long attachmentId;
    private Long clueId;
    private String fileName;
    private String filePath;
    private String fileType;
    private Long fileSize;
    private Integer deleted;
    private Long createUserId;
    private Date createTime;
}
