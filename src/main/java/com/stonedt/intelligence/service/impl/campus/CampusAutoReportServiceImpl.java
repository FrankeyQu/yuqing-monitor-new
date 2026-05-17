package com.stonedt.intelligence.service.impl.campus;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.stonedt.intelligence.dao.campus.CampusReportGenerationLogDao;
import com.stonedt.intelligence.dao.campus.CampusReportJobDao;
import com.stonedt.intelligence.entity.campus.CampusReport;
import com.stonedt.intelligence.entity.campus.CampusReportGenerationLog;
import com.stonedt.intelligence.entity.campus.CampusReportJob;
import com.stonedt.intelligence.service.campus.CampusAutoReportService;
import com.stonedt.intelligence.service.campus.CampusReportService;
import com.stonedt.intelligence.util.SnowflakeUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class CampusAutoReportServiceImpl implements CampusAutoReportService {

    private static final String STATUS_PAUSED = "paused";
    private static final String FORMAT_MARKDOWN = "markdown";
    private static final String RUN_RUNNING = "running";
    private static final String RUN_SUCCESS = "success";
    private static final String RUN_FAILED = "failed";

    private final CampusReportJobDao campusReportJobDao;
    private final CampusReportGenerationLogDao campusReportGenerationLogDao;
    private final CampusReportService campusReportService;

    public CampusAutoReportServiceImpl(CampusReportJobDao campusReportJobDao,
                                       CampusReportGenerationLogDao campusReportGenerationLogDao,
                                       CampusReportService campusReportService) {
        this.campusReportJobDao = campusReportJobDao;
        this.campusReportGenerationLogDao = campusReportGenerationLogDao;
        this.campusReportService = campusReportService;
    }

    @Override
    public CampusReportJob saveJob(CampusReportJob job, Long operatorUserId) {
        validateJob(job);
        if (job.getReportJobId() == null) {
            job.setReportJobId(SnowflakeUtil.getId());
            job.setPeriodRule(StringUtils.defaultIfBlank(job.getPeriodRule(), "daily"));
            job.setOutputFormat(StringUtils.defaultIfBlank(job.getOutputFormat(), FORMAT_MARKDOWN));
            job.setJobStatus(StringUtils.defaultIfBlank(job.getJobStatus(), STATUS_PAUSED));
            job.setDeleted(0);
            job.setCreateUserId(operatorUserId);
            job.setUpdateUserId(operatorUserId);
            campusReportJobDao.insert(job);
            return campusReportJobDao.selectByJobId(job.getReportJobId());
        }
        requireJob(job.getReportJobId());
        job.setUpdateUserId(operatorUserId);
        campusReportJobDao.update(job);
        return campusReportJobDao.selectByJobId(job.getReportJobId());
    }

    @Override
    public CampusReportJob updateJobStatus(Long reportJobId, String jobStatus, Long operatorUserId) {
        requireJob(reportJobId);
        if (StringUtils.isBlank(jobStatus)) {
            throw new IllegalArgumentException("任务状态不能为空");
        }
        campusReportJobDao.updateStatus(reportJobId, jobStatus, operatorUserId);
        return campusReportJobDao.selectByJobId(reportJobId);
    }

    @Override
    public void deleteJob(Long reportJobId, Long operatorUserId) {
        requireJob(reportJobId);
        campusReportJobDao.logicalDelete(reportJobId, operatorUserId);
    }

    @Override
    public PageInfo<CampusReportJob> listJobs(Integer pageNum,
                                              Integer pageSize,
                                              String keyword,
                                              String reportType,
                                              String jobStatus) {
        PageHelper.startPage(defaultPageNum(pageNum), defaultPageSize(pageSize));
        return new PageInfo<>(campusReportJobDao.list(keyword, reportType, jobStatus));
    }

    @Override
    public CampusReport runJob(Long reportJobId, Long operatorUserId) {
        CampusReportJob job = requireJob(reportJobId);
        CampusReportGenerationLog log = startLog(job, operatorUserId);
        try {
            CampusReport report = buildReport(job);
            CampusReport saved = campusReportService.saveReport(report, operatorUserId);
            CampusReport generated = campusReportService.generate(saved.getReportId(), operatorUserId);
            campusReportGenerationLogDao.finish(log.getGenerationLogId(), generated.getReportId(), RUN_SUCCESS, null);
            campusReportJobDao.updateLastRunTime(reportJobId, operatorUserId);
            return generated;
        } catch (Exception e) {
            campusReportGenerationLogDao.finish(log.getGenerationLogId(), null, RUN_FAILED, e.getMessage());
            throw asRuntimeException(e);
        }
    }

    @Override
    public List<CampusReportGenerationLog> listLogs(Long reportJobId) {
        requireJob(reportJobId);
        return campusReportGenerationLogDao.listByJobId(reportJobId);
    }

    private CampusReportGenerationLog startLog(CampusReportJob job, Long operatorUserId) {
        CampusReportGenerationLog log = new CampusReportGenerationLog();
        log.setGenerationLogId(SnowflakeUtil.getId());
        log.setReportJobId(job.getReportJobId());
        log.setRunStatus(RUN_RUNNING);
        log.setStartTime(new Date());
        log.setCreateUserId(operatorUserId);
        campusReportGenerationLogDao.insert(log);
        return log;
    }

    private CampusReport buildReport(CampusReportJob job) {
        Date[] period = resolvePeriod(job.getPeriodRule());
        CampusReport report = new CampusReport();
        report.setReportTitle(job.getJobName() + "-" + new SimpleDateFormat("yyyyMMddHHmm").format(new Date()));
        report.setReportType(job.getReportType());
        report.setTemplateId(job.getTemplateId());
        report.setPeriodStartTime(period[0]);
        report.setPeriodEndTime(period[1]);
        report.setReportSummary("由自动报告任务生成，内容需人工复核后归档或发布。");
        report.setReportFormat(StringUtils.defaultIfBlank(job.getOutputFormat(), FORMAT_MARKDOWN));
        return report;
    }

    private Date[] resolvePeriod(String periodRule) {
        Calendar end = Calendar.getInstance();
        Calendar start = Calendar.getInstance();
        if ("weekly".equals(periodRule)) {
            start.add(Calendar.DAY_OF_MONTH, -7);
        } else if ("monthly".equals(periodRule)) {
            start.add(Calendar.MONTH, -1);
        } else {
            start.add(Calendar.DAY_OF_MONTH, -1);
        }
        return new Date[]{start.getTime(), end.getTime()};
    }

    private void validateJob(CampusReportJob job) {
        if (job == null) {
            throw new IllegalArgumentException("自动报告任务不能为空");
        }
        if (StringUtils.isBlank(job.getJobName())) {
            throw new IllegalArgumentException("任务名称不能为空");
        }
        if (StringUtils.isBlank(job.getReportType())) {
            throw new IllegalArgumentException("报告类型不能为空");
        }
    }

    private CampusReportJob requireJob(Long reportJobId) {
        if (reportJobId == null) {
            throw new IllegalArgumentException("自动报告任务ID不能为空");
        }
        CampusReportJob job = campusReportJobDao.selectByJobId(reportJobId);
        if (job == null) {
            throw new IllegalArgumentException("自动报告任务不存在");
        }
        return job;
    }

    private RuntimeException asRuntimeException(Exception e) {
        if (e instanceof RuntimeException) {
            return (RuntimeException) e;
        }
        return new IllegalStateException(e.getMessage(), e);
    }

    private int defaultPageNum(Integer pageNum) {
        return pageNum == null || pageNum < 1 ? 1 : pageNum;
    }

    private int defaultPageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return 10;
        }
        return Math.min(pageSize, 100);
    }
}
