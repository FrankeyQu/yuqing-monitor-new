package com.stonedt.intelligence.dao.campus;

import com.stonedt.intelligence.entity.campus.CampusReportJob;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CampusReportJobDao {

    int insert(CampusReportJob job);

    int update(CampusReportJob job);

    int updateStatus(@Param("reportJobId") Long reportJobId,
                     @Param("jobStatus") String jobStatus,
                     @Param("updateUserId") Long updateUserId);

    int updateLastRunTime(@Param("reportJobId") Long reportJobId,
                          @Param("updateUserId") Long updateUserId);

    int logicalDelete(@Param("reportJobId") Long reportJobId,
                      @Param("updateUserId") Long updateUserId);

    CampusReportJob selectByJobId(@Param("reportJobId") Long reportJobId);

    List<CampusReportJob> list(@Param("keyword") String keyword,
                               @Param("reportType") String reportType,
                               @Param("jobStatus") String jobStatus);
}
