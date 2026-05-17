package com.stonedt.intelligence.dao.campus;

import com.stonedt.intelligence.entity.campus.CampusAnalysisResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CampusAnalysisResultDao {

    int insert(CampusAnalysisResult result);

    int review(@Param("analysisResultId") Long analysisResultId,
               @Param("adoptionStatus") String adoptionStatus,
               @Param("reviewOpinion") String reviewOpinion,
               @Param("reviewerUserId") Long reviewerUserId,
               @Param("updateUserId") Long updateUserId);

    CampusAnalysisResult selectByResultId(@Param("analysisResultId") Long analysisResultId);

    List<CampusAnalysisResult> list(@Param("analysisTaskId") Long analysisTaskId,
                                    @Param("objectType") String objectType,
                                    @Param("objectId") Long objectId,
                                    @Param("analysisType") String analysisType,
                                    @Param("adoptionStatus") String adoptionStatus);
}
