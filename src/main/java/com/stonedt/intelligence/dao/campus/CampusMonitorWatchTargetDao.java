package com.stonedt.intelligence.dao.campus;

import com.stonedt.intelligence.entity.campus.CampusMonitorWatchTarget;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CampusMonitorWatchTargetDao {

    int insert(CampusMonitorWatchTarget target);

    int update(CampusMonitorWatchTarget target);

    int logicalDelete(@Param("targetId") Long targetId,
                      @Param("updateUserId") Long updateUserId);

    CampusMonitorWatchTarget selectByTargetId(@Param("targetId") Long targetId);

    List<CampusMonitorWatchTarget> list(@Param("monitorTaskId") Long monitorTaskId,
                                        @Param("targetType") String targetType,
                                        @Param("platform") String platform,
                                        @Param("keyword") String keyword,
                                        @Param("targetStatus") String targetStatus);

    List<CampusMonitorWatchTarget> listActiveByTask(@Param("monitorTaskId") Long monitorTaskId);
}
