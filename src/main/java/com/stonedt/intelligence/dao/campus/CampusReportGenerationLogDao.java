package com.stonedt.intelligence.dao.campus;

import com.stonedt.intelligence.entity.campus.CampusReportGenerationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CampusReportGenerationLogDao {

    int insert(CampusReportGenerationLog log);

    int finish(@Param("generationLogId") Long generationLogId,
               @Param("reportId") Long reportId,
               @Param("runStatus") String runStatus,
               @Param("errorMessage") String errorMessage);

    CampusReportGenerationLog selectByLogId(@Param("generationLogId") Long generationLogId);

    List<CampusReportGenerationLog> listByJobId(@Param("reportJobId") Long reportJobId);
}
