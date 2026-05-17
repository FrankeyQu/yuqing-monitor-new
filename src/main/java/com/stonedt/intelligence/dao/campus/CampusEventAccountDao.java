package com.stonedt.intelligence.dao.campus;

import com.stonedt.intelligence.entity.campus.CampusEventAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CampusEventAccountDao {

    int insert(CampusEventAccount relation);

    List<CampusEventAccount> listByEventId(@Param("eventId") Long eventId);
}
