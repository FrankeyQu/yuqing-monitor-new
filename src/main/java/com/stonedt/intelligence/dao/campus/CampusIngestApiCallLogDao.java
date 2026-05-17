package com.stonedt.intelligence.dao.campus;

import com.stonedt.intelligence.entity.campus.CampusIngestApiCallLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface CampusIngestApiCallLogDao {

    int insert(CampusIngestApiCallLog callLog);

    CampusIngestApiCallLog selectByCallId(@Param("callId") Long callId);

    List<CampusIngestApiCallLog> list(@Param("taskId") Long taskId,
                                      @Param("runId") Long runId,
                                      @Param("provider") String provider,
                                      @Param("callStatus") String callStatus);

    int deleteBefore(@Param("expireBefore") Date expireBefore,
                     @Param("limit") Integer limit);
}
