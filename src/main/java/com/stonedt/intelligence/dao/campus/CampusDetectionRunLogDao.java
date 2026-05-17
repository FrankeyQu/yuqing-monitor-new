package com.stonedt.intelligence.dao.campus;

import com.stonedt.intelligence.entity.campus.CampusDetectionRunLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CampusDetectionRunLogDao {

    int insert(CampusDetectionRunLog runLog);

    int finish(@Param("runLogId") Long runLogId,
               @Param("runStatus") String runStatus,
               @Param("scannedCount") Integer scannedCount,
               @Param("hitCount") Integer hitCount,
               @Param("alertCount") Integer alertCount,
               @Param("errorMessage") String errorMessage);

    CampusDetectionRunLog selectByRunLogId(@Param("runLogId") Long runLogId);

    List<CampusDetectionRunLog> listByTaskId(@Param("detectionTaskId") Long detectionTaskId);
}
