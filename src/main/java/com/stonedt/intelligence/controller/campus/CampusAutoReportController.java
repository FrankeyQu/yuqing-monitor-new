package com.stonedt.intelligence.controller.campus;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageInfo;
import com.stonedt.intelligence.entity.User;
import com.stonedt.intelligence.entity.campus.CampusReport;
import com.stonedt.intelligence.entity.campus.CampusReportGenerationLog;
import com.stonedt.intelligence.entity.campus.CampusReportJob;
import com.stonedt.intelligence.service.campus.CampusAuditLogService;
import com.stonedt.intelligence.service.campus.CampusAutoReportService;
import com.stonedt.intelligence.util.UserUtil;
import com.stonedt.intelligence.vo.ResultVO;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/campus/auto-report")
public class CampusAutoReportController {

    private final CampusAutoReportService campusAutoReportService;
    private final CampusAuditLogService campusAuditLogService;
    private final UserUtil userUtil;

    public CampusAutoReportController(CampusAutoReportService campusAutoReportService,
                                      CampusAuditLogService campusAuditLogService,
                                      UserUtil userUtil) {
        this.campusAutoReportService = campusAutoReportService;
        this.campusAuditLogService = campusAuditLogService;
        this.userUtil = userUtil;
    }

    @GetMapping("/job/list")
    public ResultVO<PageInfo<CampusReportJob>> listJobs(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String reportType,
            @RequestParam(required = false) String jobStatus) {
        return ResultVO.success(campusAutoReportService.listJobs(pageNum, pageSize,
                keyword, reportType, jobStatus));
    }

    @PostMapping("/job/save")
    public ResultVO<CampusReportJob> saveJob(@RequestBody CampusReportJob job,
                                             HttpServletRequest request) {
        try {
            User user = userUtil.getuser(request);
            CampusReportJob saved = campusAutoReportService.saveJob(job, user.getUser_id());
            campusAuditLogService.record(request, "自动报告", "保存任务", "campus_report_job",
                    String.valueOf(saved.getReportJobId()), JSON.toJSONString(job), true, null);
            return ResultVO.success(saved);
        } catch (Exception e) {
            campusAuditLogService.record(request, "自动报告", "保存任务", "campus_report_job",
                    job == null ? null : String.valueOf(job.getReportJobId()), JSON.toJSONString(job), false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @PostMapping("/job/update-status")
    public ResultVO<CampusReportJob> updateJobStatus(@RequestParam Long reportJobId,
                                                     @RequestParam String jobStatus,
                                                     HttpServletRequest request) {
        String params = "reportJobId=" + reportJobId + "&jobStatus=" + jobStatus;
        try {
            User user = userUtil.getuser(request);
            CampusReportJob saved = campusAutoReportService.updateJobStatus(reportJobId, jobStatus, user.getUser_id());
            campusAuditLogService.record(request, "自动报告", "任务状态变更", "campus_report_job",
                    String.valueOf(reportJobId), params, true, null);
            return ResultVO.success(saved);
        } catch (Exception e) {
            campusAuditLogService.record(request, "自动报告", "任务状态变更", "campus_report_job",
                    String.valueOf(reportJobId), params, false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @PostMapping("/job/delete")
    public ResultVO<Void> deleteJob(@RequestParam Long reportJobId, HttpServletRequest request) {
        String params = "reportJobId=" + reportJobId;
        try {
            User user = userUtil.getuser(request);
            campusAutoReportService.deleteJob(reportJobId, user.getUser_id());
            campusAuditLogService.record(request, "自动报告", "删除任务", "campus_report_job",
                    String.valueOf(reportJobId), params, true, null);
            return ResultVO.success();
        } catch (Exception e) {
            campusAuditLogService.record(request, "自动报告", "删除任务", "campus_report_job",
                    String.valueOf(reportJobId), params, false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @PostMapping("/job/run")
    public ResultVO<CampusReport> runJob(@RequestParam Long reportJobId, HttpServletRequest request) {
        String params = "reportJobId=" + reportJobId;
        try {
            User user = userUtil.getuser(request);
            CampusReport report = campusAutoReportService.runJob(reportJobId, user.getUser_id());
            campusAuditLogService.record(request, "自动报告", "运行任务", "campus_report_job",
                    String.valueOf(reportJobId), params, true, null);
            return ResultVO.success(report);
        } catch (Exception e) {
            campusAuditLogService.record(request, "自动报告", "运行任务", "campus_report_job",
                    String.valueOf(reportJobId), params, false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @GetMapping("/log/list")
    public ResultVO<List<CampusReportGenerationLog>> listLogs(@RequestParam Long reportJobId) {
        return ResultVO.success(campusAutoReportService.listLogs(reportJobId));
    }
}
