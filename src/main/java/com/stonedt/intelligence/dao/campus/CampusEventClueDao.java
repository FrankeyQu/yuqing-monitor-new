package com.stonedt.intelligence.dao.campus;

import com.stonedt.intelligence.entity.campus.CampusEventClue;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CampusEventClueDao {

    int insert(CampusEventClue relation);

    CampusEventClue selectByEventAndClue(@Param("eventId") Long eventId,
                                         @Param("clueId") Long clueId);

    List<CampusEventClue> listByEventId(@Param("eventId") Long eventId);
}
