package com.stonedt.intelligence.service.campus;

import com.github.pagehelper.PageInfo;
import com.stonedt.intelligence.entity.campus.CampusAnalysisResult;
import com.stonedt.intelligence.entity.campus.CampusAnalysisTask;

public interface CampusAnalysisService {

    CampusAnalysisTask createTask(CampusAnalysisTask task, Long operatorUserId);

    PageInfo<CampusAnalysisTask> listTasks(Integer pageNum,
                                           Integer pageSize,
                                           String objectType,
                                           Long objectId,
                                           String analysisType,
                                           String taskStatus);

    CampusAnalysisResult runTask(Long analysisTaskId, Long operatorUserId);

    PageInfo<CampusAnalysisResult> listResults(Integer pageNum,
                                               Integer pageSize,
                                               Long analysisTaskId,
                                               String objectType,
                                               Long objectId,
                                               String analysisType,
                                               String adoptionStatus);

    CampusAnalysisResult reviewResult(Long analysisResultId,
                                      String adoptionStatus,
                                      String reviewOpinion,
                                      Long operatorUserId);
}
