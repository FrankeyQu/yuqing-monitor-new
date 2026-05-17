package com.stonedt.intelligence.dao.campus;

import com.stonedt.intelligence.entity.campus.CampusEvent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Mapper
public interface CampusEventDao {

    int insert(CampusEvent event);

    int update(CampusEvent event);

    int updateStatus(@Param("eventId") Long eventId,
                     @Param("eventStatus") String eventStatus,
                     @Param("updateUserId") Long updateUserId);

    int rate(@Param("eventId") Long eventId,
             @Param("riskLevel") String riskLevel,
             @Param("disposalRequirement") String disposalRequirement,
             @Param("updateUserId") Long updateUserId);

    int archive(@Param("eventId") Long eventId,
                @Param("archiveConclusion") String archiveConclusion,
                @Param("updateUserId") Long updateUserId);

    CampusEvent selectByEventId(@Param("eventId") Long eventId);

    Map<String, Object> getEventSource(@Param("eventId") Long eventId);

    List<CampusEvent> list(@Param("keyword") String keyword,
                           @Param("riskLevel") String riskLevel,
                           @Param("eventStatus") String eventStatus,
                           @Param("startTime") Date startTime,
                           @Param("endTime") Date endTime);
}
