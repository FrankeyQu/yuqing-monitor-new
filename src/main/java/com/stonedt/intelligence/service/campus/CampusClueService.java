package com.stonedt.intelligence.service.campus;

import com.github.pagehelper.PageInfo;
import com.stonedt.intelligence.entity.campus.CampusClue;
import com.stonedt.intelligence.entity.campus.CampusClueOperationLog;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface CampusClueService {

    CampusClue save(CampusClue clue, Long operatorUserId, String operatorName);

    CampusClue detail(Long clueId);

    PageInfo<CampusClue> list(Integer pageNum,
                              Integer pageSize,
                              String keyword,
                              String clueSource,
                              String sourcePlatform,
                              String sourceSubPlatform,
                              String riskLevel,
                              String clueStatus,
                              String language,
                              String sentiment,
                              String articleStatus,
                              Date startTime,
                              Date endTime,
                              Date publishTimeStart,
                              Date publishTimeEnd,
                              Date collectTimeStart,
                              Date collectTimeEnd,
                              String matchScope,
                              Boolean similarDedup,
                              String sortBy);

    CampusClue judge(Long clueId,
                     String riskLevel,
                     String judgeOpinion,
                     Long operatorUserId,
                     String operatorName);

    CampusClue archive(Long clueId,
                       String archiveReason,
                       Long operatorUserId,
                       String operatorName);

    CampusClue updateSentimentFromMonitor(Long clueId,
                                          String sentiment,
                                          Long monitorResultId,
                                          Long operatorUserId,
                                          String operatorName);

    void delete(Long clueId, Long operatorUserId, String operatorName);

    List<CampusClueOperationLog> operationLogs(Long clueId);

    List<Map<String, Object>> countByMediaType(String keyword,
                                               String clueSource,
                                               String sourcePlatform,
                                               String sourceSubPlatform,
                                               String riskLevel,
                                               String clueStatus,
                                               String language,
                                               String sentiment,
                                               String articleStatus,
                                               Date startTime,
                                               Date endTime,
                                               Date publishTimeStart,
                                               Date publishTimeEnd,
                                               Date collectTimeStart,
                                               Date collectTimeEnd,
                                               String matchScope,
                                               Boolean similarDedup);

    List<Map<String, Object>> countBySubPlatform(String keyword,
                                                 String clueSource,
                                                 String sourcePlatform,
                                                 String sourceSubPlatform,
                                                 String riskLevel,
                                                 String clueStatus,
                                                 String language,
                                                 String sentiment,
                                                 String articleStatus,
                                                 Date startTime,
                                                 Date endTime,
                                                 Date publishTimeStart,
                                                 Date publishTimeEnd,
                                                 Date collectTimeStart,
                                                 Date collectTimeEnd,
                                                 String matchScope,
                                                 Boolean similarDedup);
}
