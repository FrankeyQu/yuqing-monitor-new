package com.stonedt.intelligence.dao.campus;

import com.stonedt.intelligence.entity.campus.CampusAccountTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CampusAccountTaskDao {

    int insert(CampusAccountTask task);

    int updateStatus(@Param("taskId") Long taskId,
                     @Param("taskStatus") String taskStatus,
                     @Param("updateUserId") Long updateUserId);

    CampusAccountTask selectByTaskId(@Param("taskId") Long taskId);

    List<CampusAccountTask> listByAccountId(@Param("accountId") Long accountId);
}
