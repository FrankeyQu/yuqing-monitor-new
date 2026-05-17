package com.stonedt.intelligence.dao.campus;

import com.stonedt.intelligence.entity.campus.CampusReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface CampusReportDao {

    int insert(CampusReport report);

    int update(CampusReport report);

    int archive(@Param("reportId") Long reportId,
                @Param("archiveOpinion") String archiveOpinion,
                @Param("archiveUserId") Long archiveUserId,
                @Param("updateUserId") Long updateUserId);

    int logicalDelete(@Param("reportId") Long reportId, @Param("updateUserId") Long updateUserId);

    CampusReport selectByReportId(@Param("reportId") Long reportId);

    List<CampusReport> list(@Param("keyword") String keyword,
                            @Param("reportType") String reportType,
                            @Param("reportStatus") String reportStatus,
                            @Param("startTime") Date startTime,
                            @Param("endTime") Date endTime);
}
