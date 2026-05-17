package com.stonedt.intelligence.dao.campus;

import com.stonedt.intelligence.entity.campus.CampusAnalysisTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CampusAnalysisTaskDao {

    int insert(CampusAnalysisTask task);

    int updateStatus(@Param("analysisTaskId") Long analysisTaskId,
                     @Param("taskStatus") String taskStatus,
                     @Param("errorMessage") String errorMessage,
                     @Param("updateUserId") Long updateUserId);

    CampusAnalysisTask selectByTaskId(@Param("analysisTaskId") Long analysisTaskId);

    List<CampusAnalysisTask> list(@Param("objectType") String objectType,
                                  @Param("objectId") Long objectId,
                                  @Param("analysisType") String analysisType,
                                  @Param("taskStatus") String taskStatus);
}
