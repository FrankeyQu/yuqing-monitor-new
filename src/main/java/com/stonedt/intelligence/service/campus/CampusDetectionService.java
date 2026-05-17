package com.stonedt.intelligence.service.campus;

import com.github.pagehelper.PageInfo;
import com.stonedt.intelligence.entity.campus.CampusDetectionHit;
import com.stonedt.intelligence.entity.campus.CampusDetectionRule;
import com.stonedt.intelligence.entity.campus.CampusDetectionRunLog;
import com.stonedt.intelligence.entity.campus.CampusDetectionTask;
import com.stonedt.intelligence.entity.campus.CampusDetectionTopic;

public interface CampusDetectionService {

    CampusDetectionTopic saveTopic(CampusDetectionTopic topic, Long operatorUserId);

    void deleteTopic(Long topicId, Long operatorUserId);

    PageInfo<CampusDetectionTopic> listTopics(Integer pageNum,
                                              Integer pageSize,
                                              String keyword,
                                              String topicCategory,
                                              Integer enabled);

    CampusDetectionRule saveRule(CampusDetectionRule rule, Long operatorUserId);

    void deleteRule(Long ruleId, Long operatorUserId);

    PageInfo<CampusDetectionRule> listRules(Integer pageNum,
                                            Integer pageSize,
                                            Long topicId,
                                            String ruleType,
                                            Integer enabled);

    CampusDetectionTask saveTask(CampusDetectionTask task, Long operatorUserId);

    CampusDetectionTask updateTaskStatus(Long detectionTaskId, String taskStatus, Long operatorUserId);

    void deleteTask(Long detectionTaskId, Long operatorUserId);

    PageInfo<CampusDetectionTask> listTasks(Integer pageNum,
                                            Integer pageSize,
                                            String keyword,
                                            Long topicId,
                                            String taskStatus);

    CampusDetectionRunLog runTask(Long detectionTaskId, Long operatorUserId);

    CampusDetectionRunLog runIngestRecordTask(Long detectionTaskId, Long ingestRunId, Long operatorUserId);

    CampusDetectionHit alertHit(Long hitId, Long operatorUserId);

    CampusDetectionHit ignoreHit(Long hitId, Long operatorUserId);

    PageInfo<CampusDetectionHit> listHits(Integer pageNum,
                                          Integer pageSize,
                                          Long detectionTaskId,
                                          Long topicId,
                                          String objectType,
                                          String hitStatus,
                                          String riskLevel,
                                          String keyword);

    PageInfo<CampusDetectionRunLog> listRunLogs(Integer pageNum,
                                                Integer pageSize,
                                                Long detectionTaskId);
}
