package com.stonedt.intelligence.dao.campus;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CampusMonitorIngestTaskRelationDao {

    int upsert(@Param("relationId") Long relationId,
               @Param("monitorTaskId") Long monitorTaskId,
               @Param("ingestTaskId") Long ingestTaskId,
               @Param("operatorUserId") Long operatorUserId);

    int softDeleteMissing(@Param("monitorTaskId") Long monitorTaskId,
                          @Param("ingestTaskIds") List<Long> ingestTaskIds,
                          @Param("operatorUserId") Long operatorUserId);

    int softDeleteAll(@Param("monitorTaskId") Long monitorTaskId,
                      @Param("operatorUserId") Long operatorUserId);

    List<Long> listIngestTaskIds(@Param("monitorTaskId") Long monitorTaskId);
}
