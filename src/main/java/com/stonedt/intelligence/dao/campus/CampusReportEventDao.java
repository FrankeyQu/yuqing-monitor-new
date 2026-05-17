package com.stonedt.intelligence.dao.campus;

import com.stonedt.intelligence.entity.campus.CampusReportEvent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CampusReportEventDao {

    int insert(CampusReportEvent relation);

    int logicalDeleteByReportId(@Param("reportId") Long reportId);

    List<CampusReportEvent> listByReportId(@Param("reportId") Long reportId);
}
