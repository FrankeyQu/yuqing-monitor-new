package com.stonedt.intelligence.controller.campus;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageInfo;
import com.stonedt.intelligence.entity.User;
import com.stonedt.intelligence.entity.campus.CampusReport;
import com.stonedt.intelligence.entity.campus.CampusReportEvent;
import com.stonedt.intelligence.entity.campus.CampusReportTemplate;
import com.stonedt.intelligence.service.campus.CampusAuditLogService;
import com.stonedt.intelligence.service.campus.CampusReportService;
import com.stonedt.intelligence.service.campus.ReportExportService;
import com.stonedt.intelligence.util.UserUtil;
import com.stonedt.intelligence.vo.ResultVO;
import org.springframework.core.io.InputStreamResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/campus/report")
public class CampusReportController {

    private final CampusReportService campusReportService;
    private final CampusAuditLogService campusAuditLogService;
    private final ReportExportService reportExportService;
    private final UserUtil userUtil;

    public CampusReportController(CampusReportService campusReportService,
                                  CampusAuditLogService campusAuditLogService,
                                  ReportExportService reportExportService,
                                  UserUtil userUtil) {
        this.campusReportService = campusReportService;
        this.campusAuditLogService = campusAuditLogService;
        this.reportExportService = reportExportService;
        this.userUtil = userUtil;
    }

    @GetMapping("/template/list")
    public ResultVO<PageInfo<CampusReportTemplate>> listTemplates(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String reportType,
            @RequestParam(required = false) Integer status) {
        return ResultVO.success(campusReportService.listTemplates(pageNum, pageSize, keyword, reportType, status));
    }

    @PostMapping("/template/save")
    public ResultVO<CampusReportTemplate> saveTemplate(@RequestBody CampusReportTemplate template,
                                                       HttpServletRequest request) {
        try {
            User user = userUtil.getuser(request);
            CampusReportTemplate saved = campusReportService.saveTemplate(template, user.getUser_id());
            campusAuditLogService.record(request, "报告归档", "保存模板", "campus_report_template",
                    String.valueOf(saved.getTemplateId()), JSON.toJSONString(template), true, null);
            return ResultVO.success(saved);
        } catch (Exception e) {
            campusAuditLogService.record(request, "报告归档", "保存模板", "campus_report_template",
                    template == null ? null : String.valueOf(template.getTemplateId()), JSON.toJSONString(template), false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @PostMapping("/template/delete")
    public ResultVO<Void> deleteTemplate(@RequestParam Long templateId, HttpServletRequest request) {
        String params = "templateId=" + templateId;
        try {
            User user = userUtil.getuser(request);
            campusReportService.deleteTemplate(templateId, user.getUser_id());
            campusAuditLogService.record(request, "报告归档", "删除模板", "campus_report_template",
                    String.valueOf(templateId), params, true, null);
            return ResultVO.success();
        } catch (Exception e) {
            campusAuditLogService.record(request, "报告归档", "删除模板", "campus_report_template",
                    String.valueOf(templateId), params, false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @GetMapping("/list")
    public ResultVO<PageInfo<CampusReport>> listReports(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String reportType,
            @RequestParam(required = false) String reportStatus,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") Date startTime,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") Date endTime) {
        return ResultVO.success(campusReportService.listReports(pageNum, pageSize, keyword,
                reportType, reportStatus, startTime, endTime));
    }

    @GetMapping("/detail")
    public ResultVO<CampusReport> detail(@RequestParam Long reportId) {
        return ResultVO.success(campusReportService.detail(reportId));
    }

    @GetMapping("/events")
    public ResultVO<List<CampusReportEvent>> listReportEvents(@RequestParam Long reportId) {
        return ResultVO.success(campusReportService.listReportEvents(reportId));
    }

    @PostMapping("/save")
    public ResultVO<CampusReport> saveReport(@RequestBody CampusReport report, HttpServletRequest request) {
        try {
            User user = userUtil.getuser(request);
            CampusReport saved = campusReportService.saveReport(report, user.getUser_id());
            campusAuditLogService.record(request, "报告归档", "保存报告", "campus_report",
                    String.valueOf(saved.getReportId()), JSON.toJSONString(report), true, null);
            return ResultVO.success(saved);
        } catch (Exception e) {
            campusAuditLogService.record(request, "报告归档", "保存报告", "campus_report",
                    report == null ? null : String.valueOf(report.getReportId()), JSON.toJSONString(report), false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @PostMapping("/generate")
    public ResultVO<CampusReport> generate(@RequestParam Long reportId, HttpServletRequest request) {
        String params = "reportId=" + reportId;
        try {
            User user = userUtil.getuser(request);
            CampusReport saved = campusReportService.generate(reportId, user.getUser_id());
            campusAuditLogService.record(request, "报告归档", "生成报告", "campus_report",
                    String.valueOf(reportId), params, true, null);
            return ResultVO.success(saved);
        } catch (Exception e) {
            campusAuditLogService.record(request, "报告归档", "生成报告", "campus_report",
                    String.valueOf(reportId), params, false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @PostMapping("/archive")
    public ResultVO<CampusReport> archive(@RequestParam Long reportId,
                                          @RequestParam(required = false) String archiveOpinion,
                                          HttpServletRequest request) {
        String params = "reportId=" + reportId;
        try {
            User user = userUtil.getuser(request);
            CampusReport saved = campusReportService.archive(reportId, archiveOpinion, user.getUser_id());
            campusAuditLogService.record(request, "报告归档", "归档报告", "campus_report",
                    String.valueOf(reportId), params, true, null);
            return ResultVO.success(saved);
        } catch (Exception e) {
            campusAuditLogService.record(request, "报告归档", "归档报告", "campus_report",
                    String.valueOf(reportId), params, false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @PostMapping("/delete")
    public ResultVO<Void> deleteReport(@RequestParam Long reportId, HttpServletRequest request) {
        String params = "reportId=" + reportId;
        try {
            User user = userUtil.getuser(request);
            campusReportService.deleteReport(reportId, user.getUser_id());
            campusAuditLogService.record(request, "报告归档", "删除报告", "campus_report",
                    String.valueOf(reportId), params, true, null);
            return ResultVO.success();
        } catch (Exception e) {
            campusAuditLogService.record(request, "报告归档", "删除报告", "campus_report",
                    String.valueOf(reportId), params, false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @GetMapping("/download")
    public ResponseEntity<InputStreamResource> download(@RequestParam Long reportId, HttpServletRequest request) {
        try {
            campusAuditLogService.record(request, "报告归档", "下载报告", "campus_report",
                    String.valueOf(reportId), "reportId=" + reportId, true, null);
            return campusReportService.download(reportId);
        } catch (Exception e) {
            campusAuditLogService.record(request, "报告归档", "下载报告", "campus_report",
                    String.valueOf(reportId), "reportId=" + reportId, false, e.getMessage());
            return ResponseEntity.status(400).build();
        }
    }

    // ==================== AI Report Generation ====================

    /**
     * Generate AI-powered report content (non-streaming).
     * Returns the full AI-generated markdown content once complete.
     */
    @PostMapping("/generate-ai")
    public ResultVO<CampusReport> generateAi(@RequestParam Long reportId, HttpServletRequest request) {
        String params = "reportId=" + reportId;
        try {
            User user = userUtil.getuser(request);
            CampusReport saved = campusReportService.generateAi(reportId, user.getUser_id(), null);
            campusAuditLogService.record(request, "报告归档", "AI生成报告", "campus_report",
                    String.valueOf(reportId), params, true, null);
            return ResultVO.success(saved);
        } catch (Exception e) {
            campusAuditLogService.record(request, "报告归档", "AI生成报告", "campus_report",
                    String.valueOf(reportId), params, false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    /**
     * Generate AI-powered report content with SSE streaming.
     * Streams markdown chunks to the client in real time.
     */
    @GetMapping(value = "/generate-ai-stream", produces = "text/event-stream;charset=UTF-8")
    public SseEmitter generateAiStream(@RequestParam Long reportId, HttpServletRequest request) {
        SseEmitter emitter = new SseEmitter(300000L);
        User user = userUtil.getuser(request);
        Long operatorUserId = user == null ? null : user.getUser_id();

        new Thread(() -> {
            try {
                StringBuilder streamOutput = new StringBuilder();
                campusReportService.generateAi(reportId, operatorUserId, streamOutput, chunk -> {
                    try {
                        emitter.send(SseEmitter.event().name("message").data(chunk));
                    } catch (IOException ex) {
                        throw new IllegalStateException("AI流式连接已中断", ex);
                    }
                });
                campusAuditLogService.record(request, "报告归档", "AI流式生成报告", "campus_report",
                        String.valueOf(reportId), "reportId=" + reportId, true, null);
                emitter.send(SseEmitter.event().name("done").data("[DONE]"));
                emitter.complete();
            } catch (Exception e) {
                try {
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data(e.getMessage()));
                } catch (IOException ignored) {
                    // ignore
                }
                campusAuditLogService.record(request, "报告归档", "AI流式生成报告", "campus_report",
                        String.valueOf(reportId), "reportId=" + reportId, false, e.getMessage());
                emitter.completeWithError(e);
            }
        }).start();

        return emitter;
    }

    // ==================== docx/pptx Export ====================

    /**
     * Download report as .docx file.
     */
    @GetMapping("/download-docx")
    public ResponseEntity<byte[]> downloadDocx(@RequestParam Long reportId, HttpServletRequest request) {
        try {
            CampusReport report = campusReportService.detail(reportId);
            String content = report.getReportContent() != null ? report.getReportContent() : "";
            byte[] docxBytes = reportExportService.exportDocx(
                    report.getReportTitle(), content, report.getReportType());

            String fileName = buildExportFileName(report, ".docx");

            campusAuditLogService.record(request, "报告归档", "导出docx", "campus_report",
                    String.valueOf(reportId), "reportId=" + reportId, true, null);

            HttpHeaders headers = new HttpHeaders();
            headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
            headers.add("Content-Disposition", "attachment; filename="
                    + URLEncoder.encode(fileName, "UTF-8"));
            headers.add("Pragma", "no-cache");
            headers.add("Expires", "0");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType(
                            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                    .body(docxBytes);
        } catch (Exception e) {
            campusAuditLogService.record(request, "报告归档", "导出docx", "campus_report",
                    String.valueOf(reportId), "reportId=" + reportId, false, e.getMessage());
            return ResponseEntity.status(400).build();
        }
    }

    /**
     * Download report as .pptx file.
     */
    @GetMapping("/download-pptx")
    public ResponseEntity<byte[]> downloadPptx(@RequestParam Long reportId, HttpServletRequest request) {
        try {
            CampusReport report = campusReportService.detail(reportId);
            String content = report.getReportContent() != null ? report.getReportContent() : "";
            byte[] pptxBytes = reportExportService.exportPptx(
                    report.getReportTitle(), content, report.getReportType());

            String fileName = buildExportFileName(report, ".pptx");

            campusAuditLogService.record(request, "报告归档", "导出pptx", "campus_report",
                    String.valueOf(reportId), "reportId=" + reportId, true, null);

            HttpHeaders headers = new HttpHeaders();
            headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
            headers.add("Content-Disposition", "attachment; filename="
                    + URLEncoder.encode(fileName, "UTF-8"));
            headers.add("Pragma", "no-cache");
            headers.add("Expires", "0");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType(
                            "application/vnd.openxmlformats-officedocument.presentationml.presentation"))
                    .body(pptxBytes);
        } catch (Exception e) {
            campusAuditLogService.record(request, "报告归档", "导出pptx", "campus_report",
                    String.valueOf(reportId), "reportId=" + reportId, false, e.getMessage());
            return ResponseEntity.status(400).build();
        }
    }

    // ==================== private helpers ====================

    private String buildExportFileName(CampusReport report, String extension) {
        String title = report.getReportTitle() != null
                ? report.getReportTitle().replaceAll("[\\\\/:*?\"<>|\\s]+", "_")
                : "campus-report";
        return title + extension;
    }
}
