package com.stonedt.intelligence.entity.campus;

import lombok.Data;

@Data
public class CampusEducationBaiduTaskRequest {

    private Long sourceId;
    private String taskName;
    private String topicType;
    private String region;
    private String schoolName;
    private String keyword;
    private Integer topK;
    private String credentialRef;
    private String authorizationScope;
}
