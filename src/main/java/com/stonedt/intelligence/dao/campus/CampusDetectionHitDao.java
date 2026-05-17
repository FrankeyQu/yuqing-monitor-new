package com.stonedt.intelligence.dao.campus;

import com.stonedt.intelligence.entity.campus.CampusDetectionHit;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CampusDetectionHitDao {

    int insert(CampusDetectionHit hit);

    int updateStatus(@Param("hitId") Long hitId,
                     @Param("hitStatus") String hitStatus,
                     @Param("alertId") Long alertId,
                     @Param("clueId") Long clueId,
                     @Param("updateUserId") Long updateUserId);

    int countExisting(@Param("detectionTaskId") Long detectionTaskId,
                      @Param("objectType") String objectType,
                      @Param("objectId") Long objectId,
                      @Param("ruleId") Long ruleId,
                      @Param("matchedKeywords") String matchedKeywords);

    CampusDetectionHit selectByHitId(@Param("hitId") Long hitId);

    List<CampusDetectionHit> list(@Param("detectionTaskId") Long detectionTaskId,
                                  @Param("topicId") Long topicId,
                                  @Param("objectType") String objectType,
                                  @Param("hitStatus") String hitStatus,
                                  @Param("riskLevel") String riskLevel,
                                  @Param("keyword") String keyword);
}
