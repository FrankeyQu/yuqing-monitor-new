package com.stonedt.intelligence.dao.campus;

import com.stonedt.intelligence.entity.campus.CampusReportTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CampusReportTemplateDao {

    int insert(CampusReportTemplate template);

    int update(CampusReportTemplate template);

    int logicalDelete(@Param("templateId") Long templateId, @Param("updateUserId") Long updateUserId);

    CampusReportTemplate selectByTemplateId(@Param("templateId") Long templateId);

    List<CampusReportTemplate> list(@Param("keyword") String keyword,
                                    @Param("reportType") String reportType,
                                    @Param("status") Integer status);
}
