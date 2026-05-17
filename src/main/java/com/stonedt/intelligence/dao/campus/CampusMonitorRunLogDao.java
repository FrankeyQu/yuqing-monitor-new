package com.stonedt.intelligence.dao.campus;

import com.stonedt.intelligence.entity.campus.CampusMonitorRunLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface CampusMonitorRunLogDao {

    int insert(CampusMonitorRunLog runLog);

    int finish(@Param("runLogId") Long runLogId,
               @Param("runStatus") String runStatus,
               @Param("scannedCount") Integer scannedCount,
               @Param("matchCount") Integer matchCount,
               @Param("negativeCount") Integer negativeCount,
               @Param("alertCount") Integer alertCount,
               @Param("errorMessage") String errorMessage);

    CampusMonitorRunLog selectByRunLogId(@Param("runLogId") Long runLogId);

    List<CampusMonitorRunLog> listByTaskId(@Param("monitorTaskId") Long monitorTaskId);

    int deleteBefore(@Param("expireBefore") Date expireBefore,
                     @Param("limit") Integer limit);
}
