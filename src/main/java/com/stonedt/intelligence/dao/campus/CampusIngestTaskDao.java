package com.stonedt.intelligence.dao.campus;

import com.stonedt.intelligence.entity.campus.CampusIngestTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface CampusIngestTaskDao {

    int insert(CampusIngestTask task);

    int update(CampusIngestTask task);

    int updateStatus(@Param("taskId") Long taskId,
                     @Param("taskStatus") String taskStatus,
                     @Param("updateUserId") Long updateUserId);

    int updateLastRunTime(@Param("taskId") Long taskId, @Param("updateUserId") Long updateUserId);

    int updateNextRunTime(@Param("taskId") Long taskId,
                          @Param("nextRunTime") Date nextRunTime,
                          @Param("updateUserId") Long updateUserId);

    List<CampusIngestTask> listDueTasks(@Param("now") Date now, @Param("limit") Integer limit);

    int acquireScheduleLock(@Param("taskId") Long taskId,
                            @Param("now") Date now,
                            @Param("lockUntil") Date lockUntil);

    int acquireExecutionLock(@Param("taskId") Long taskId,
                             @Param("now") Date now,
                             @Param("lockUntil") Date lockUntil,
                             @Param("updateUserId") Long updateUserId);

    int releaseScheduleLock(@Param("taskId") Long taskId);

    int releaseScheduleLockBefore(@Param("taskId") Long taskId,
                                  @Param("lockUntil") Date lockUntil);

    int markScheduleSuccess(@Param("taskId") Long taskId,
                            @Param("nextRunTime") Date nextRunTime,
                            @Param("lockUntil") Date lockUntil,
                            @Param("updateUserId") Long updateUserId);

    int markScheduleFailure(@Param("taskId") Long taskId,
                            @Param("nextRunTime") Date nextRunTime,
                            @Param("errorType") String errorType,
                            @Param("currentRetryCount") Integer currentRetryCount,
                            @Param("taskStatus") String taskStatus,
                            @Param("lockUntil") Date lockUntil,
                            @Param("updateUserId") Long updateUserId);

    int resetDailyQuota(@Param("taskId") Long taskId,
                        @Param("quotaStatDate") Date quotaStatDate,
                        @Param("updateUserId") Long updateUserId);

    int increaseDailyQuotaUsed(@Param("taskId") Long taskId,
                               @Param("quotaStatDate") Date quotaStatDate,
                               @Param("costUnits") Integer costUnits,
                               @Param("updateUserId") Long updateUserId);

    int logicalDelete(@Param("taskId") Long taskId, @Param("updateUserId") Long updateUserId);

    CampusIngestTask selectByTaskId(@Param("taskId") Long taskId);

    List<CampusIngestTask> list(@Param("keyword") String keyword,
                                @Param("sourceId") Long sourceId,
                                @Param("targetType") String targetType,
                                @Param("taskStatus") String taskStatus);
}
