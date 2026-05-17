package com.stonedt.intelligence.controller.campus;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageInfo;
import com.stonedt.intelligence.entity.User;
import com.stonedt.intelligence.entity.campus.CampusAnalysisResult;
import com.stonedt.intelligence.entity.campus.CampusAnalysisTask;
import com.stonedt.intelligence.service.campus.CampusAnalysisService;
import com.stonedt.intelligence.service.campus.CampusAuditLogService;
import com.stonedt.intelligence.util.UserUtil;
import com.stonedt.intelligence.vo.ResultVO;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/campus/analysis")
public class CampusAnalysisController {

    private final CampusAnalysisService campusAnalysisService;
    private final CampusAuditLogService campusAuditLogService;
    private final UserUtil userUtil;

    public CampusAnalysisController(CampusAnalysisService campusAnalysisService,
                                    CampusAuditLogService campusAuditLogService,
                                    UserUtil userUtil) {
        this.campusAnalysisService = campusAnalysisService;
        this.campusAuditLogService = campusAuditLogService;
        this.userUtil = userUtil;
    }

    @PostMapping("/task/create")
    public ResultVO<CampusAnalysisTask> createTask(@RequestBody CampusAnalysisTask task,
                                                   HttpServletRequest request) {
        try {
            User user = userUtil.getuser(request);
            CampusAnalysisTask saved = campusAnalysisService.createTask(task, user.getUser_id());
            campusAuditLogService.record(request, "辅助研判", "创建任务", "campus_analysis_task",
                    String.valueOf(saved.getAnalysisTaskId()), JSON.toJSONString(task), true, null);
            return ResultVO.success(saved);
        } catch (Exception e) {
            campusAuditLogService.record(request, "辅助研判", "创建任务", "campus_analysis_task",
                    task == null ? null : String.valueOf(task.getAnalysisTaskId()), JSON.toJSONString(task), false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @GetMapping("/task/list")
    public ResultVO<PageInfo<CampusAnalysisTask>> listTasks(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String objectType,
            @RequestParam(required = false) Long objectId,
            @RequestParam(required = false) String analysisType,
            @RequestParam(required = false) String taskStatus) {
        return ResultVO.success(campusAnalysisService.listTasks(pageNum, pageSize,
                objectType, objectId, analysisType, taskStatus));
    }

    @PostMapping("/task/run")
    public ResultVO<CampusAnalysisResult> runTask(@RequestParam Long analysisTaskId,
                                                  HttpServletRequest request) {
        String params = "analysisTaskId=" + analysisTaskId;
        try {
            User user = userUtil.getuser(request);
            CampusAnalysisResult result = campusAnalysisService.runTask(analysisTaskId, user.getUser_id());
            campusAuditLogService.record(request, "辅助研判", "运行任务", "campus_analysis_task",
                    String.valueOf(analysisTaskId), params, true, null);
            return ResultVO.success(result);
        } catch (Exception e) {
            campusAuditLogService.record(request, "辅助研判", "运行任务", "campus_analysis_task",
                    String.valueOf(analysisTaskId), params, false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @GetMapping("/result/list")
    public ResultVO<PageInfo<CampusAnalysisResult>> listResults(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long analysisTaskId,
            @RequestParam(required = false) String objectType,
            @RequestParam(required = false) Long objectId,
            @RequestParam(required = false) String analysisType,
            @RequestParam(required = false) String adoptionStatus) {
        return ResultVO.success(campusAnalysisService.listResults(pageNum, pageSize,
                analysisTaskId, objectType, objectId, analysisType, adoptionStatus));
    }

    @PostMapping("/result/review")
    public ResultVO<CampusAnalysisResult> reviewResult(@RequestParam Long analysisResultId,
                                                       @RequestParam String adoptionStatus,
                                                       @RequestParam(required = false) String reviewOpinion,
                                                       HttpServletRequest request) {
        String params = "analysisResultId=" + analysisResultId + "&adoptionStatus=" + adoptionStatus;
        try {
            User user = userUtil.getuser(request);
            CampusAnalysisResult result = campusAnalysisService.reviewResult(analysisResultId,
                    adoptionStatus, reviewOpinion, user.getUser_id());
            campusAuditLogService.record(request, "辅助研判", "复核结果", "campus_analysis_result",
                    String.valueOf(analysisResultId), params, true, null);
            return ResultVO.success(result);
        } catch (Exception e) {
            campusAuditLogService.record(request, "辅助研判", "复核结果", "campus_analysis_result",
                    String.valueOf(analysisResultId), params, false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }
}
