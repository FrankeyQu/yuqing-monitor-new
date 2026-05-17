package com.stonedt.intelligence.controller.campus;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageInfo;
import com.stonedt.intelligence.dto.campus.CampusMonitorAiAnalyzeRequest;
import com.stonedt.intelligence.dto.campus.CampusMonitorAiAnalyzeResponse;
import com.stonedt.intelligence.dto.campus.CampusMonitorTaskAiDiagnosis;
import com.stonedt.intelligence.entity.User;
import com.stonedt.intelligence.entity.campus.CampusAlert;
import com.stonedt.intelligence.entity.campus.CampusClue;
import com.stonedt.intelligence.entity.campus.CampusMonitorInformation;
import com.stonedt.intelligence.entity.campus.CampusMonitorResult;
import com.stonedt.intelligence.entity.campus.CampusMonitorRunLog;
import com.stonedt.intelligence.entity.campus.CampusMonitorTask;
import com.stonedt.intelligence.entity.campus.CampusMonitorWatchTarget;
import com.stonedt.intelligence.service.campus.CampusAuditLogService;
import com.stonedt.intelligence.service.campus.CampusMonitorService;
import com.stonedt.intelligence.util.UserUtil;
import com.stonedt.intelligence.vo.ResultVO;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/campus/monitor")
public class CampusMonitorController {

    private final CampusMonitorService campusMonitorService;
    private final CampusAuditLogService campusAuditLogService;
    private final UserUtil userUtil;

    public CampusMonitorController(CampusMonitorService campusMonitorService,
                                   CampusAuditLogService campusAuditLogService,
                                   UserUtil userUtil) {
        this.campusMonitorService = campusMonitorService;
        this.campusAuditLogService = campusAuditLogService;
        this.userUtil = userUtil;
    }

    @GetMapping("/overview")
    public ResultVO<Map<String, Integer>> overview() {
        return ResultVO.success(campusMonitorService.overview());
    }

    @GetMapping("/task/list")
    public ResultVO<PageInfo<CampusMonitorTask>> listTasks(@RequestParam(defaultValue = "1") Integer pageNum,
                                                           @RequestParam(defaultValue = "10") Integer pageSize,
                                                           @RequestParam(required = false) String keyword,
                                                           @RequestParam(required = false) String taskStatus,
                                                           @RequestParam(required = false) String platform) {
        return ResultVO.success(campusMonitorService.listTasks(pageNum, pageSize, keyword, taskStatus, platform));
    }

    @PostMapping("/task/save")
    public ResultVO<CampusMonitorTask> saveTask(@RequestBody CampusMonitorTask task, HttpServletRequest request) {
        try {
            User user = userUtil.getuser(request);
            CampusMonitorTask saved = campusMonitorService.saveTask(task, user.getUser_id());
            campusAuditLogService.record(request, "监测任务", "保存监测任务", "campus_monitor_task",
                    String.valueOf(saved.getMonitorTaskId()), JSON.toJSONString(task), true, null);
            return ResultVO.success(saved);
        } catch (Exception e) {
            campusAuditLogService.record(request, "监测任务", "保存监测任务", "campus_monitor_task",
                    task == null ? null : String.valueOf(task.getMonitorTaskId()), JSON.toJSONString(task), false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @PostMapping("/task/update-status")
    public ResultVO<CampusMonitorTask> updateTaskStatus(@RequestParam Long monitorTaskId,
                                                        @RequestParam String taskStatus,
                                                        HttpServletRequest request) {
        String params = "monitorTaskId=" + monitorTaskId + "&taskStatus=" + taskStatus;
        try {
            User user = userUtil.getuser(request);
            CampusMonitorTask saved = campusMonitorService.updateTaskStatus(monitorTaskId, taskStatus, user.getUser_id());
            campusAuditLogService.record(request, "监测任务", "更新监测任务状态", "campus_monitor_task",
                    String.valueOf(monitorTaskId), params, true, null);
            return ResultVO.success(saved);
        } catch (Exception e) {
            campusAuditLogService.record(request, "监测任务", "更新监测任务状态", "campus_monitor_task",
                    String.valueOf(monitorTaskId), params, false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @PostMapping("/task/update-display")
    public ResultVO<CampusMonitorTask> updateTaskDisplay(@RequestParam Long monitorTaskId,
                                                         @RequestParam Integer displayEnabled,
                                                         HttpServletRequest request) {
        String params = "monitorTaskId=" + monitorTaskId + "&displayEnabled=" + displayEnabled;
        try {
            User user = userUtil.getuser(request);
            CampusMonitorTask saved = campusMonitorService.updateTaskDisplay(monitorTaskId, displayEnabled, user.getUser_id());
            campusAuditLogService.record(request, "监测任务", "更新监测任务前台展示", "campus_monitor_task",
                    String.valueOf(monitorTaskId), params, true, null);
            return ResultVO.success(saved);
        } catch (Exception e) {
            campusAuditLogService.record(request, "监测任务", "更新监测任务前台展示", "campus_monitor_task",
                    String.valueOf(monitorTaskId), params, false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @PostMapping("/task/delete")
    public ResultVO<Void> deleteTask(@RequestParam Long monitorTaskId, HttpServletRequest request) {
        String params = "monitorTaskId=" + monitorTaskId;
        try {
            User user = userUtil.getuser(request);
            campusMonitorService.deleteTask(monitorTaskId, user.getUser_id());
            campusAuditLogService.record(request, "监测任务", "删除监测任务", "campus_monitor_task",
                    String.valueOf(monitorTaskId), params, true, null);
            return ResultVO.success();
        } catch (Exception e) {
            campusAuditLogService.record(request, "监测任务", "删除监测任务", "campus_monitor_task",
                    String.valueOf(monitorTaskId), params, false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @PostMapping("/task/run")
    public ResultVO<CampusMonitorRunLog> runTask(@RequestParam Long monitorTaskId, HttpServletRequest request) {
        String params = "monitorTaskId=" + monitorTaskId;
        try {
            User user = userUtil.getuser(request);
            CampusMonitorRunLog runLog = campusMonitorService.runTask(monitorTaskId, user.getUser_id());
            campusAuditLogService.record(request, "监测任务", "运行监测任务", "campus_monitor_task",
                    String.valueOf(monitorTaskId), params, true, null);
            return ResultVO.success(runLog);
        } catch (Exception e) {
            campusAuditLogService.record(request, "监测任务", "运行监测任务", "campus_monitor_task",
                    String.valueOf(monitorTaskId), params, false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @GetMapping("/task/run-log/list")
    public ResultVO<PageInfo<CampusMonitorRunLog>> listRunLogs(@RequestParam(defaultValue = "1") Integer pageNum,
                                                               @RequestParam(defaultValue = "10") Integer pageSize,
                                                               @RequestParam Long monitorTaskId) {
        return ResultVO.success(campusMonitorService.listRunLogs(pageNum, pageSize, monitorTaskId));
    }

    @GetMapping("/result/list")
    public ResultVO<PageInfo<CampusMonitorResult>> listResults(@RequestParam(defaultValue = "1") Integer pageNum,
                                                               @RequestParam(defaultValue = "10") Integer pageSize,
                                                               @RequestParam(required = false) Long monitorTaskId,
                                                               @RequestParam(required = false) String keyword,
                                                               @RequestParam(required = false) String riskLevel,
                                                               @RequestParam(required = false) String resultStatus,
                                                               @RequestParam(required = false) String platform,
                                                               @RequestParam(required = false) String language,
                                                               @RequestParam(required = false) Boolean converted) {
        return ResultVO.success(campusMonitorService.listResults(pageNum, pageSize, monitorTaskId,
                keyword, riskLevel, resultStatus, platform, language, converted));
    }

    @GetMapping("/information/list")
    public ResultVO<PageInfo<CampusMonitorInformation>> listInformation(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long monitorTaskId,
            @RequestParam(required = false) String sourcePlatform,
            @RequestParam(required = false) String sourceSubPlatform,
            @RequestParam(required = false) String riskLevel,
            @RequestParam(required = false) String clueStatus,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String sentiment,
            @RequestParam(required = false) String resultStatus,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") Date publishTimeStart,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") Date publishTimeEnd,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") Date collectTimeStart,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") Date collectTimeEnd,
            @RequestParam(required = false) String matchScope,
            @RequestParam(required = false) Boolean similarDedup,
            @RequestParam(required = false) String hitScope,
            @RequestParam(required = false) String sortBy) {
        return ResultVO.success(campusMonitorService.listInformation(pageNum, pageSize, keyword,
                monitorTaskId, sourcePlatform, sourceSubPlatform, riskLevel, clueStatus, language, sentiment, resultStatus,
                publishTimeStart, publishTimeEnd, collectTimeStart, collectTimeEnd, matchScope, similarDedup, hitScope, sortBy));
    }

    @GetMapping("/information/count-by-platform")
    public ResultVO<List<Map<String, Object>>> countInformationByPlatform(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long monitorTaskId,
            @RequestParam(required = false) String sourcePlatform,
            @RequestParam(required = false) String sourceSubPlatform,
            @RequestParam(required = false) String riskLevel,
            @RequestParam(required = false) String clueStatus,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String sentiment,
            @RequestParam(required = false) String resultStatus,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") Date publishTimeStart,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") Date publishTimeEnd,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") Date collectTimeStart,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") Date collectTimeEnd,
            @RequestParam(required = false) String matchScope,
            @RequestParam(required = false) Boolean similarDedup,
            @RequestParam(required = false) String hitScope) {
        return ResultVO.success(campusMonitorService.countInformationByPlatform(keyword,
                monitorTaskId, sourcePlatform, sourceSubPlatform, riskLevel, clueStatus, language, sentiment, resultStatus,
                publishTimeStart, publishTimeEnd, collectTimeStart, collectTimeEnd, matchScope, similarDedup, hitScope));
    }

    @GetMapping("/information/count-by-sub-platform")
    public ResultVO<List<Map<String, Object>>> countInformationBySubPlatform(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long monitorTaskId,
            @RequestParam(required = false) String sourcePlatform,
            @RequestParam(required = false) String sourceSubPlatform,
            @RequestParam(required = false) String riskLevel,
            @RequestParam(required = false) String clueStatus,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String sentiment,
            @RequestParam(required = false) String resultStatus,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") Date publishTimeStart,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") Date publishTimeEnd,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") Date collectTimeStart,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") Date collectTimeEnd,
            @RequestParam(required = false) String matchScope,
            @RequestParam(required = false) Boolean similarDedup,
            @RequestParam(required = false) String hitScope) {
        return ResultVO.success(campusMonitorService.countInformationBySubPlatform(keyword,
                monitorTaskId, sourcePlatform, sourceSubPlatform, riskLevel, clueStatus, language, sentiment, resultStatus,
                publishTimeStart, publishTimeEnd, collectTimeStart, collectTimeEnd, matchScope, similarDedup, hitScope));
    }

    @PostMapping("/result/alert")
    public ResultVO<CampusMonitorResult> alertResult(@RequestParam Long monitorResultId, HttpServletRequest request) {
        String params = "monitorResultId=" + monitorResultId;
        try {
            User user = userUtil.getuser(request);
            CampusMonitorResult saved = campusMonitorService.alertResult(monitorResultId, user.getUser_id());
            campusAuditLogService.record(request, "监测任务", "监测结果转预警", "campus_monitor_result",
                    String.valueOf(monitorResultId), params, true, null);
            return ResultVO.success(saved);
        } catch (Exception e) {
            campusAuditLogService.record(request, "监测任务", "监测结果转预警", "campus_monitor_result",
                    String.valueOf(monitorResultId), params, false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @PostMapping("/result/ignore")
    public ResultVO<CampusMonitorResult> ignoreResult(@RequestParam Long monitorResultId, HttpServletRequest request) {
        String params = "monitorResultId=" + monitorResultId;
        try {
            User user = userUtil.getuser(request);
            CampusMonitorResult saved = campusMonitorService.ignoreResult(monitorResultId, user.getUser_id());
            campusAuditLogService.record(request, "监测任务", "取消监测预警", "campus_monitor_result",
                    String.valueOf(monitorResultId), params, true, null);
            return ResultVO.success(saved);
        } catch (Exception e) {
            campusAuditLogService.record(request, "监测任务", "取消监测预警", "campus_monitor_result",
                    String.valueOf(monitorResultId), params, false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @PostMapping("/task/ai-diagnose")
    public ResultVO<CampusMonitorTaskAiDiagnosis> diagnoseTask(@RequestParam Long monitorTaskId,
                                                               HttpServletRequest request) {
        String params = "monitorTaskId=" + monitorTaskId;
        try {
            User user = userUtil.getuser(request);
            CampusMonitorTaskAiDiagnosis diagnosis = campusMonitorService.diagnoseTask(monitorTaskId, user.getUser_id());
            campusAuditLogService.record(request, "监测任务", "AI体检监测任务", "campus_monitor_task",
                    String.valueOf(monitorTaskId), params, true, null);
            return ResultVO.success(diagnosis);
        } catch (Exception e) {
            campusAuditLogService.record(request, "监测任务", "AI体检监测任务", "campus_monitor_task",
                    String.valueOf(monitorTaskId), params, false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @PostMapping("/result/sentiment")
    public ResultVO<CampusMonitorResult> updateResultSentiment(@RequestParam Long monitorResultId,
                                                               @RequestParam String sentiment,
                                                               HttpServletRequest request) {
        String params = "monitorResultId=" + monitorResultId + "&sentiment=" + sentiment;
        try {
            User user = userUtil.getuser(request);
            CampusMonitorResult saved = campusMonitorService.updateResultSentiment(monitorResultId,
                    sentiment, user.getUser_id(), user.getUsername());
            campusAuditLogService.record(request, "监测任务", "修改监测结果情感", "campus_monitor_result",
                    String.valueOf(monitorResultId), params, true, null);
            return ResultVO.success(saved);
        } catch (Exception e) {
            campusAuditLogService.record(request, "监测任务", "修改监测结果情感", "campus_monitor_result",
                    String.valueOf(monitorResultId), params, false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @PostMapping("/result/ai-analyze")
    public ResultVO<CampusMonitorAiAnalyzeResponse> analyzeResults(@RequestBody CampusMonitorAiAnalyzeRequest analyzeRequest,
                                                                   HttpServletRequest request) {
        String params = JSON.toJSONString(analyzeRequest);
        try {
            User user = userUtil.getuser(request);
            CampusMonitorAiAnalyzeResponse result = campusMonitorService.analyzeResults(analyzeRequest,
                    user.getUser_id(), user.getUsername());
            campusAuditLogService.record(request, "监测任务", "AI分析监测命中", "campus_monitor_result",
                    null, params, true, null);
            return ResultVO.success(result);
        } catch (Exception e) {
            campusAuditLogService.record(request, "监测任务", "AI分析监测命中", "campus_monitor_result",
                    null, params, false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @PostMapping("/result/convert-clue")
    public ResultVO<CampusClue> convertResultToClue(@RequestParam Long monitorResultId, HttpServletRequest request) {
        String params = "monitorResultId=" + monitorResultId;
        try {
            User user = userUtil.getuser(request);
            CampusClue saved = campusMonitorService.convertResultToClue(monitorResultId,
                    user.getUser_id(), user.getUsername());
            campusAuditLogService.record(request, "监测任务", "监测结果转线索", "campus_monitor_result",
                    String.valueOf(monitorResultId), params, true, null);
            return ResultVO.success(saved);
        } catch (Exception e) {
            campusAuditLogService.record(request, "监测任务", "监测结果转线索", "campus_monitor_result",
                    String.valueOf(monitorResultId), params, false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @GetMapping("/watch-target/list")
    public ResultVO<PageInfo<CampusMonitorWatchTarget>> listWatchTargets(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long monitorTaskId,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) String platform,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String targetStatus) {
        return ResultVO.success(campusMonitorService.listWatchTargets(pageNum, pageSize, monitorTaskId,
                targetType, platform, keyword, targetStatus));
    }

    @PostMapping("/watch-target/save")
    public ResultVO<CampusMonitorWatchTarget> saveWatchTarget(@RequestBody CampusMonitorWatchTarget target,
                                                             HttpServletRequest request) {
        try {
            User user = userUtil.getuser(request);
            CampusMonitorWatchTarget saved = campusMonitorService.saveWatchTarget(target, user.getUser_id());
            campusAuditLogService.record(request, "监测任务", "保存重点监控目标", "campus_monitor_watch_target",
                    String.valueOf(saved.getTargetId()), JSON.toJSONString(target), true, null);
            return ResultVO.success(saved);
        } catch (Exception e) {
            campusAuditLogService.record(request, "监测任务", "保存重点监控目标", "campus_monitor_watch_target",
                    target == null ? null : String.valueOf(target.getTargetId()), JSON.toJSONString(target), false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @PostMapping("/watch-target/create-from-result")
    public ResultVO<CampusMonitorWatchTarget> createWatchTargetFromResult(
            @RequestParam Long monitorResultId,
            @RequestParam Long monitorTaskId,
            @RequestParam(defaultValue = "account") String targetType,
            HttpServletRequest request) {
        String params = "monitorResultId=" + monitorResultId + "&monitorTaskId=" + monitorTaskId + "&targetType=" + targetType;
        try {
            User user = userUtil.getuser(request);
            CampusMonitorWatchTarget saved = campusMonitorService.createWatchTargetFromResult(monitorResultId,
                    monitorTaskId, targetType, user.getUser_id());
            campusAuditLogService.record(request, "监测任务", "结果加入重点监控目标", "campus_monitor_watch_target",
                    String.valueOf(saved.getTargetId()), params, true, null);
            return ResultVO.success(saved);
        } catch (Exception e) {
            campusAuditLogService.record(request, "监测任务", "结果加入重点监控目标", "campus_monitor_watch_target",
                    String.valueOf(monitorResultId), params, false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @PostMapping("/watch-target/delete")
    public ResultVO<Void> deleteWatchTarget(@RequestParam Long targetId, HttpServletRequest request) {
        String params = "targetId=" + targetId;
        try {
            User user = userUtil.getuser(request);
            campusMonitorService.deleteWatchTarget(targetId, user.getUser_id());
            campusAuditLogService.record(request, "监测任务", "删除重点监控目标", "campus_monitor_watch_target",
                    String.valueOf(targetId), params, true, null);
            return ResultVO.success();
        } catch (Exception e) {
            campusAuditLogService.record(request, "监测任务", "删除重点监控目标", "campus_monitor_watch_target",
                    String.valueOf(targetId), params, false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @GetMapping("/alert/list")
    public ResultVO<PageInfo<CampusAlert>> listAlerts(@RequestParam(defaultValue = "1") Integer pageNum,
                                                      @RequestParam(defaultValue = "10") Integer pageSize,
                                                      @RequestParam(required = false) Long monitorTaskId,
                                                      @RequestParam(required = false) String keyword,
                                                      @RequestParam(required = false) String riskLevel,
                                                      @RequestParam(required = false) String alertStatus) {
        return ResultVO.success(campusMonitorService.listAlerts(pageNum, pageSize, monitorTaskId,
                keyword, riskLevel, alertStatus));
    }

    @PostMapping("/alert/handle")
    public ResultVO<CampusAlert> handleAlert(@RequestParam Long alertId,
                                             @RequestParam String alertStatus,
                                             @RequestParam(required = false) String handleOpinion,
                                             HttpServletRequest request) {
        String params = "alertId=" + alertId + "&alertStatus=" + alertStatus;
        try {
            User user = userUtil.getuser(request);
            CampusAlert saved = campusMonitorService.handleAlert(alertId, alertStatus, handleOpinion, user.getUser_id());
            campusAuditLogService.record(request, "监测任务", "处理监测告警", "campus_alert",
                    String.valueOf(alertId), params, true, null);
            return ResultVO.success(saved);
        } catch (Exception e) {
            campusAuditLogService.record(request, "监测任务", "处理监测告警", "campus_alert",
                    String.valueOf(alertId), params, false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }
}
