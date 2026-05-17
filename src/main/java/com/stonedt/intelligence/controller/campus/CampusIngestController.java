package com.stonedt.intelligence.controller.campus;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageInfo;
import com.stonedt.intelligence.entity.User;
import com.stonedt.intelligence.entity.campus.CampusAccountContent;
import com.stonedt.intelligence.entity.campus.CampusClue;
import com.stonedt.intelligence.entity.campus.CampusIngestApiCallLog;
import com.stonedt.intelligence.entity.campus.CampusIngestRecord;
import com.stonedt.intelligence.entity.campus.CampusIngestRunLog;
import com.stonedt.intelligence.entity.campus.CampusIngestSource;
import com.stonedt.intelligence.entity.campus.CampusIngestTask;
import com.stonedt.intelligence.service.campus.CampusAuditLogService;
import com.stonedt.intelligence.service.campus.CampusIngestService;
import com.stonedt.intelligence.util.UserUtil;
import com.stonedt.intelligence.vo.ResultVO;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/campus/ingest")
public class CampusIngestController {

    private final CampusIngestService campusIngestService;
    private final CampusAuditLogService campusAuditLogService;
    private final UserUtil userUtil;

    public CampusIngestController(CampusIngestService campusIngestService,
                                  CampusAuditLogService campusAuditLogService,
                                  UserUtil userUtil) {
        this.campusIngestService = campusIngestService;
        this.campusAuditLogService = campusAuditLogService;
        this.userUtil = userUtil;
    }

    @GetMapping("/source/list")
    public ResultVO<PageInfo<CampusIngestSource>> listSources(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sourceType,
            @RequestParam(required = false) String platform,
            @RequestParam(required = false) Integer enabled) {
        return ResultVO.success(campusIngestService.listSources(pageNum, pageSize,
                keyword, sourceType, platform, enabled));
    }

    @PostMapping("/source/save")
    public ResultVO<CampusIngestSource> saveSource(@RequestBody CampusIngestSource source,
                                                   HttpServletRequest request) {
        try {
            User user = userUtil.getuser(request);
            CampusIngestSource saved = campusIngestService.saveSource(source, user.getUser_id());
            campusAuditLogService.record(request, "数据接入", "保存来源", "campus_ingest_source",
                    String.valueOf(saved.getSourceId()), JSON.toJSONString(source), true, null);
            return ResultVO.success(saved);
        } catch (Exception e) {
            campusAuditLogService.record(request, "数据接入", "保存来源", "campus_ingest_source",
                    source == null ? null : String.valueOf(source.getSourceId()), JSON.toJSONString(source), false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @PostMapping("/source/delete")
    public ResultVO<Void> deleteSource(@RequestParam Long sourceId, HttpServletRequest request) {
        String params = "sourceId=" + sourceId;
        try {
            User user = userUtil.getuser(request);
            campusIngestService.deleteSource(sourceId, user.getUser_id());
            campusAuditLogService.record(request, "数据接入", "删除来源", "campus_ingest_source",
                    String.valueOf(sourceId), params, true, null);
            return ResultVO.success();
        } catch (Exception e) {
            campusAuditLogService.record(request, "数据接入", "删除来源", "campus_ingest_source",
                    String.valueOf(sourceId), params, false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @GetMapping("/task/list")
    public ResultVO<PageInfo<CampusIngestTask>> listTasks(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long sourceId,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) String taskStatus) {
        return ResultVO.success(campusIngestService.listTasks(pageNum, pageSize,
                keyword, sourceId, targetType, taskStatus));
    }

    @PostMapping("/task/save")
    public ResultVO<CampusIngestTask> saveTask(@RequestBody CampusIngestTask task,
                                               HttpServletRequest request) {
        try {
            User user = userUtil.getuser(request);
            CampusIngestTask saved = campusIngestService.saveTask(task, user.getUser_id());
            campusAuditLogService.record(request, "数据接入", "保存任务", "campus_ingest_task",
                    String.valueOf(saved.getTaskId()), JSON.toJSONString(task), true, null);
            return ResultVO.success(saved);
        } catch (Exception e) {
            campusAuditLogService.record(request, "数据接入", "保存任务", "campus_ingest_task",
                    task == null ? null : String.valueOf(task.getTaskId()), JSON.toJSONString(task), false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @PostMapping("/task/update-status")
    public ResultVO<CampusIngestTask> updateTaskStatus(@RequestParam Long taskId,
                                                       @RequestParam String taskStatus,
                                                       HttpServletRequest request) {
        String params = "taskId=" + taskId + "&taskStatus=" + taskStatus;
        try {
            User user = userUtil.getuser(request);
            CampusIngestTask saved = campusIngestService.updateTaskStatus(taskId, taskStatus, user.getUser_id());
            campusAuditLogService.record(request, "数据接入", "任务状态变更", "campus_ingest_task",
                    String.valueOf(taskId), params, true, null);
            return ResultVO.success(saved);
        } catch (Exception e) {
            campusAuditLogService.record(request, "数据接入", "任务状态变更", "campus_ingest_task",
                    String.valueOf(taskId), params, false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @PostMapping("/task/delete")
    public ResultVO<Void> deleteTask(@RequestParam Long taskId, HttpServletRequest request) {
        String params = "taskId=" + taskId;
        try {
            User user = userUtil.getuser(request);
            campusIngestService.deleteTask(taskId, user.getUser_id());
            campusAuditLogService.record(request, "数据接入", "删除任务", "campus_ingest_task",
                    String.valueOf(taskId), params, true, null);
            return ResultVO.success();
        } catch (Exception e) {
            campusAuditLogService.record(request, "数据接入", "删除任务", "campus_ingest_task",
                    String.valueOf(taskId), params, false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @PostMapping("/task/run")
    public ResultVO<CampusIngestRunLog> runTask(@RequestParam Long taskId, HttpServletRequest request) {
        String params = "taskId=" + taskId;
        try {
            User user = userUtil.getuser(request);
            CampusIngestRunLog runLog = campusIngestService.runTask(taskId, user.getUser_id());
            campusAuditLogService.record(request, "数据接入", "手动运行任务", "campus_ingest_task",
                    String.valueOf(taskId), params, true, null);
            return ResultVO.success(runLog);
        } catch (Exception e) {
            campusAuditLogService.record(request, "数据接入", "手动运行任务", "campus_ingest_task",
                    String.valueOf(taskId), params, false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @GetMapping("/record/list")
    public ResultVO<PageInfo<CampusIngestRecord>> listRecords(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long sourceId,
            @RequestParam(required = false) Long taskId,
            @RequestParam(required = false) String normalizedStatus,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") Date startTime,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") Date endTime) {
        return ResultVO.success(campusIngestService.listRecords(pageNum, pageSize, keyword,
                sourceId, taskId, normalizedStatus, targetType, startTime, endTime));
    }

    @PostMapping("/record/submit")
    public ResultVO<CampusIngestRecord> submitRecord(@RequestBody CampusIngestRecord record,
                                                     HttpServletRequest request) {
        try {
            User user = userUtil.getuser(request);
            CampusIngestRecord saved = campusIngestService.submitRecord(record, user.getUser_id());
            campusAuditLogService.record(request, "数据接入", "提交接入记录", "campus_ingest_record",
                    String.valueOf(saved.getRecordId()), JSON.toJSONString(record), true, null);
            return ResultVO.success(saved);
        } catch (Exception e) {
            campusAuditLogService.record(request, "数据接入", "提交接入记录", "campus_ingest_record",
                    record == null ? null : String.valueOf(record.getRecordId()), JSON.toJSONString(record), false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @PostMapping("/record/convert-clue")
    public ResultVO<CampusClue> convertRecordToClue(@RequestParam Long recordId, HttpServletRequest request) {
        String params = "recordId=" + recordId;
        try {
            User user = userUtil.getuser(request);
            CampusClue saved = campusIngestService.convertRecordToClue(recordId, user.getUser_id(), user.getUsername());
            campusAuditLogService.record(request, "数据接入", "转换线索", "campus_ingest_record",
                    String.valueOf(recordId), params, true, null);
            return ResultVO.success(saved);
        } catch (Exception e) {
            campusAuditLogService.record(request, "数据接入", "转换线索", "campus_ingest_record",
                    String.valueOf(recordId), params, false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @PostMapping("/record/convert-account-content")
    public ResultVO<CampusAccountContent> convertRecordToAccountContent(@RequestParam Long recordId,
                                                                        @RequestParam(required = false) Long accountId,
                                                                        HttpServletRequest request) {
        String params = "recordId=" + recordId + "&accountId=" + accountId;
        try {
            User user = userUtil.getuser(request);
            CampusAccountContent saved = campusIngestService.convertRecordToAccountContent(recordId, accountId, user.getUser_id());
            campusAuditLogService.record(request, "数据接入", "转换账号动态", "campus_ingest_record",
                    String.valueOf(recordId), params, true, null);
            return ResultVO.success(saved);
        } catch (Exception e) {
            campusAuditLogService.record(request, "数据接入", "转换账号动态", "campus_ingest_record",
                    String.valueOf(recordId), params, false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @PostMapping("/run/start")
    public ResultVO<CampusIngestRunLog> startRun(@RequestParam Long taskId, HttpServletRequest request) {
        String params = "taskId=" + taskId;
        try {
            User user = userUtil.getuser(request);
            CampusIngestRunLog saved = campusIngestService.startRun(taskId, user.getUser_id());
            campusAuditLogService.record(request, "数据接入", "开始运行", "campus_ingest_run_log",
                    String.valueOf(saved.getRunId()), params, true, null);
            return ResultVO.success(saved);
        } catch (Exception e) {
            campusAuditLogService.record(request, "数据接入", "开始运行", "campus_ingest_run_log",
                    null, params, false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @PostMapping("/run/finish")
    public ResultVO<CampusIngestRunLog> finishRun(@RequestParam Long runId,
                                                  @RequestParam(required = false) String runStatus,
                                                  @RequestParam(required = false) Integer fetchedCount,
                                                  @RequestParam(required = false) Integer successCount,
                                                  @RequestParam(required = false) Integer failCount,
                                                  @RequestParam(required = false) String errorMessage,
                                                  HttpServletRequest request) {
        String params = "runId=" + runId + "&runStatus=" + runStatus;
        try {
            User user = userUtil.getuser(request);
            CampusIngestRunLog saved = campusIngestService.finishRun(runId, runStatus,
                    fetchedCount, successCount, failCount, errorMessage, user.getUser_id());
            campusAuditLogService.record(request, "数据接入", "结束运行", "campus_ingest_run_log",
                    String.valueOf(runId), params, true, null);
            return ResultVO.success(saved);
        } catch (Exception e) {
            campusAuditLogService.record(request, "数据接入", "结束运行", "campus_ingest_run_log",
                    String.valueOf(runId), params, false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @GetMapping("/run/list")
    public ResultVO<List<CampusIngestRunLog>> listRunLogs(@RequestParam Long taskId) {
        return ResultVO.success(campusIngestService.listRunLogs(taskId));
    }

    @GetMapping("/run/page")
    public ResultVO<PageInfo<CampusIngestRunLog>> listRunLogPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long taskId,
            @RequestParam(required = false) String runStatus,
            @RequestParam(required = false) String errorType,
            @RequestParam(required = false) String triggerType) {
        return ResultVO.success(campusIngestService.listRunLogPage(pageNum, pageSize,
                taskId, runStatus, errorType, triggerType));
    }

    @GetMapping("/api-call/list")
    public ResultVO<List<CampusIngestApiCallLog>> listApiCallLogs(
            @RequestParam(required = false) Long taskId,
            @RequestParam(required = false) Long runId,
            @RequestParam(required = false) String provider,
            @RequestParam(required = false) String callStatus) {
        return ResultVO.success(campusIngestService.listApiCallLogs(taskId, runId, provider, callStatus));
    }
}
