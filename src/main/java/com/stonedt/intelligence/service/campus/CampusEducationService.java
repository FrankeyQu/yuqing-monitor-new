package com.stonedt.intelligence.service.campus;

import com.github.pagehelper.PageInfo;
import com.stonedt.intelligence.entity.campus.CampusEducationBaiduTaskRequest;
import com.stonedt.intelligence.entity.campus.CampusIngestTask;
import com.stonedt.intelligence.entity.campus.CampusSchoolSubject;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface CampusEducationService {

    CampusSchoolSubject saveSchool(CampusSchoolSubject school, Long operatorUserId);

    void deleteSchool(Long schoolId, Long operatorUserId);

    PageInfo<CampusSchoolSubject> listSchools(Integer pageNum,
                                              Integer pageSize,
                                              String keyword,
                                              String region,
                                              String educationStage,
                                              Integer status);

    List<Map<String, Object>> listTopics(String topicType, Date startTime, Date endTime, Integer limit);

    List<Map<String, Object>> schoolSentimentRanking(String keyword, Date startTime, Date endTime, Integer limit);

    CampusIngestTask createBaiduTask(CampusEducationBaiduTaskRequest request, Long operatorUserId);

    Map<String, Object> createAndRunBaiduTask(CampusEducationBaiduTaskRequest request, Long operatorUserId);

    Map<String, Integer> importSchools(String csvContent, Long operatorUserId);

    String schoolImportTemplate();
}
