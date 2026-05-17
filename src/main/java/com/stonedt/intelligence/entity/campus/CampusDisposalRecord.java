package com.stonedt.intelligence.entity.campus;

import lombok.Data;

import java.util.Date;

@Data
public class CampusDisposalRecord {

    private Long id;
    private Long recordId;
    private Long disposalTaskId;
    private Long eventId;
    private String recordType;
    private String recordContent;
    private Long handlerUserId;
    private String handlerName;
    private Date handleTime;
    private String attachmentDesc;
    private Date createTime;
}
