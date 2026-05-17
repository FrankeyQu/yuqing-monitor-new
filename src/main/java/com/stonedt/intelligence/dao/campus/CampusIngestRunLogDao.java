package com.stonedt.intelligence.dao.campus;

import com.stonedt.intelligence.entity.campus.CampusIngestRunLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface CampusIngestRunLogDao {

    int insert(CampusIngestRunLog runLog);

    int finish(@Param("runId") Long runId,
               @Param("runStatus") String runStatus,
               @Param("fetchedCount") Integer fetchedCount,
               @Param("successCount") Integer successCount,
               @Param("duplicateCount") Integer duplicateCount,
               @Param("invalidCount") Integer invalidCount,
               @Param("failCount") Integer failCount,
               @Param("errorMessage") String errorMessage,
               @Param("durationMs") Long durationMs,
               @Param("errorType") String errorType);

    CampusIngestRunLog selectByRunId(@Param("runId") Long runId);

    int updateDetectionSummary(@Param("runId") Long runId,
                               @Param("detectionTriggerCount") Integer detectionTriggerCount,
                               @Param("detectionHitCount") Integer detectionHitCount,
                               @Param("detectionAlertCount") Integer detectionAlertCount,
                               @Param("detectionErrorMessage") String detectionErrorMessage);

    List<CampusIngestRunLog> listByTaskId(@Param("taskId") Long taskId);

    List<CampusIngestRunLog> list(@Param("taskId") Long taskId,
                                  @Param("runStatus") String runStatus,
                                  @Param("errorType") String errorType,
                                  @Param("triggerType") String triggerType);

    int deleteBefore(@Param("expireBefore") Date expireBefore,
                     @Param("limit") Integer limit);
}
