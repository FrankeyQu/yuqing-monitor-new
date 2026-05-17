package com.stonedt.intelligence.service.campus;

import com.github.pagehelper.PageInfo;
import com.stonedt.intelligence.entity.campus.CampusReport;
import com.stonedt.intelligence.entity.campus.CampusReportGenerationLog;
import com.stonedt.intelligence.entity.campus.CampusReportJob;

import java.util.Date;
import java.util.List;

public interface CampusAutoReportService {

    CampusReportJob saveJob(CampusReportJob job, Long operatorUserId);

    CampusReportJob updateJobStatus(Long reportJobId, String jobStatus, Long operatorUserId);

    void deleteJob(Long reportJobId, Long operatorUserId);

    PageInfo<CampusReportJob> listJobs(Integer pageNum,
                                       Integer pageSize,
                                       String keyword,
                                       String reportType,
                                       String jobStatus);

    CampusReport runJob(Long reportJobId, Long operatorUserId);

    List<CampusReportJob> listDueJobs(Date now, int limit);

    boolean acquireScheduleLock(Long reportJobId, Date now, Date lockUntil, Long operatorUserId);

    void releaseScheduleLock(Long reportJobId, Long operatorUserId);

    CampusReport runScheduledJob(Long reportJobId, Long operatorUserId);

    List<CampusReportGenerationLog> listLogs(Long reportJobId);
}
