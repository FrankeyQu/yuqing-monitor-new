package com.stonedt.intelligence.dao.campus;

import com.stonedt.intelligence.entity.campus.CampusDisposalTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CampusDisposalTaskDao {

    int insert(CampusDisposalTask task);

    int updateStatus(@Param("disposalTaskId") Long disposalTaskId,
                     @Param("taskStatus") String taskStatus,
                     @Param("feedbackSummary") String feedbackSummary,
                     @Param("updateUserId") Long updateUserId);

    CampusDisposalTask selectByTaskId(@Param("disposalTaskId") Long disposalTaskId);

    List<CampusDisposalTask> listByEventId(@Param("eventId") Long eventId);
}
