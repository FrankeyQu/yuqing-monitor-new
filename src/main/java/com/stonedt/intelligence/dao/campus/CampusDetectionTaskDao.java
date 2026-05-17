package com.stonedt.intelligence.dao.campus;

import com.stonedt.intelligence.entity.campus.CampusDetectionTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CampusDetectionTaskDao {

    int insert(CampusDetectionTask task);

    int update(CampusDetectionTask task);

    int updateStatus(@Param("detectionTaskId") Long detectionTaskId,
                     @Param("taskStatus") String taskStatus,
                     @Param("updateUserId") Long updateUserId);

    int updateLastRunTime(@Param("detectionTaskId") Long detectionTaskId,
                          @Param("updateUserId") Long updateUserId);

    int logicalDelete(@Param("detectionTaskId") Long detectionTaskId,
                      @Param("updateUserId") Long updateUserId);

    CampusDetectionTask selectByTaskId(@Param("detectionTaskId") Long detectionTaskId);

    List<CampusDetectionTask> list(@Param("keyword") String keyword,
                                   @Param("topicId") Long topicId,
                                   @Param("taskStatus") String taskStatus);

    List<CampusDetectionTask> listActiveTasks();
}
