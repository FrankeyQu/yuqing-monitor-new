package com.stonedt.intelligence.service.campus;

import com.github.pagehelper.PageInfo;
import com.stonedt.intelligence.entity.campus.CampusReport;
import com.stonedt.intelligence.entity.campus.CampusReportEvent;
import com.stonedt.intelligence.entity.campus.CampusReportTemplate;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;

import java.util.Date;
import java.util.List;

public interface CampusReportService {

    CampusReportTemplate saveTemplate(CampusReportTemplate template, Long operatorUserId);

    void deleteTemplate(Long templateId, Long operatorUserId);

    PageInfo<CampusReportTemplate> listTemplates(Integer pageNum,
                                                 Integer pageSize,
                                                 String keyword,
                                                 String reportType,
                                                 Integer status);

    CampusReport saveReport(CampusReport report, Long operatorUserId);

    CampusReport detail(Long reportId);

    PageInfo<CampusReport> listReports(Integer pageNum,
                                       Integer pageSize,
                                       String keyword,
                                       String reportType,
                                       String reportStatus,
                                       Date startTime,
                                       Date endTime);

    CampusReport generate(Long reportId, Long operatorUserId);

    CampusReport generateAi(Long reportId, Long operatorUserId, StringBuilder streamOutput);

    CampusReport archive(Long reportId, String archiveOpinion, Long operatorUserId);

    void deleteReport(Long reportId, Long operatorUserId);

    List<CampusReportEvent> listReportEvents(Long reportId);

    ResponseEntity<InputStreamResource> download(Long reportId);
}
