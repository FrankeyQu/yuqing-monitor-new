package com.stonedt.intelligence.dao.campus;

import com.stonedt.intelligence.entity.campus.CampusMonitorTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface CampusMonitorTaskDao {

    int insert(CampusMonitorTask task);

    int update(CampusMonitorTask task);

    int updateStatus(@Param("monitorTaskId") Long monitorTaskId,
                     @Param("taskStatus") String taskStatus,
                     @Param("updateUserId") Long updateUserId);

    int updateDisplayEnabled(@Param("monitorTaskId") Long monitorTaskId,
                             @Param("displayEnabled") Integer displayEnabled,
                             @Param("updateUserId") Long updateUserId);

    int updateLastRun(@Param("monitorTaskId") Long monitorTaskId,
                      @Param("lastRunLogId") Long lastRunLogId,
                      @Param("updateUserId") Long updateUserId);

    int updateRunSummary(@Param("monitorTaskId") Long monitorTaskId,
                         @Param("lastRunLogId") Long lastRunLogId,
                         @Param("lastMatchCount") Integer lastMatchCount,
                         @Param("lastCollectTime") Date lastCollectTime,
                         @Param("ingestCapabilityStatus") String ingestCapabilityStatus,
                         @Param("lastErrorMessage") String lastErrorMessage,
                         @Param("updateUserId") Long updateUserId);

    int updateIngestCapability(@Param("monitorTaskId") Long monitorTaskId,
                               @Param("lastCollectTime") Date lastCollectTime,
                               @Param("ingestCapabilityStatus") String ingestCapabilityStatus,
                               @Param("lastErrorMessage") String lastErrorMessage,
                               @Param("updateUserId") Long updateUserId);

    int updateNextRunTime(@Param("monitorTaskId") Long monitorTaskId,
                          @Param("nextRunTime") Date nextRunTime,
                          @Param("updateUserId") Long updateUserId);

    List<CampusMonitorTask> listDueTasks(@Param("now") Date now,
                                         @Param("limit") Integer limit);

    int acquireScheduleLock(@Param("monitorTaskId") Long monitorTaskId,
                            @Param("now") Date now,
                            @Param("lockUntil") Date lockUntil);

    int acquireExecutionLock(@Param("monitorTaskId") Long monitorTaskId,
                             @Param("now") Date now,
                             @Param("lockUntil") Date lockUntil,
                             @Param("updateUserId") Long updateUserId);

    int releaseScheduleLock(@Param("monitorTaskId") Long monitorTaskId);

    int releaseScheduleLockBefore(@Param("monitorTaskId") Long monitorTaskId,
                                  @Param("lockUntil") Date lockUntil);

    int markScheduleSuccess(@Param("monitorTaskId") Long monitorTaskId,
                             @Param("lastRunLogId") Long lastRunLogId,
                             @Param("nextRunTime") Date nextRunTime,
                             @Param("lockUntil") Date lockUntil,
                             @Param("updateUserId") Long updateUserId);

    int markScheduleFailure(@Param("monitorTaskId") Long monitorTaskId,
                             @Param("lastRunLogId") Long lastRunLogId,
                             @Param("nextRunTime") Date nextRunTime,
                             @Param("lockUntil") Date lockUntil,
                             @Param("updateUserId") Long updateUserId);

    int logicalDelete(@Param("monitorTaskId") Long monitorTaskId,
                      @Param("updateUserId") Long updateUserId);

    CampusMonitorTask selectByTaskId(@Param("monitorTaskId") Long monitorTaskId);

    CampusMonitorTask selectById(@Param("id") Long id);

    List<CampusMonitorTask> list(@Param("keyword") String keyword,
                                 @Param("taskStatus") String taskStatus,
                                 @Param("platform") String platform);

    int countByStatus(@Param("taskStatus") String taskStatus);
}
