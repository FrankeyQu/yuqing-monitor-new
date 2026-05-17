package com.stonedt.intelligence.controller.campus;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageInfo;
import com.stonedt.intelligence.entity.User;
import com.stonedt.intelligence.entity.campus.CampusDetectionHit;
import com.stonedt.intelligence.entity.campus.CampusDetectionRule;
import com.stonedt.intelligence.entity.campus.CampusDetectionRunLog;
import com.stonedt.intelligence.entity.campus.CampusDetectionTask;
import com.stonedt.intelligence.entity.campus.CampusDetectionTopic;
import com.stonedt.intelligence.service.campus.CampusAuditLogService;
import com.stonedt.intelligence.service.campus.CampusDetectionService;
import com.stonedt.intelligence.util.UserUtil;
import com.stonedt.intelligence.vo.ResultVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/campus/detection")
public class CampusDetectionController {

    private final CampusDetectionService campusDetectionService;
    private final CampusAuditLogService campusAuditLogService;
    private final UserUtil userUtil;

    public CampusDetectionController(CampusDetectionService campusDetectionService,
                                     CampusAuditLogService campusAuditLogService,
                                     UserUtil userUtil) {
        this.campusDetectionService = campusDetectionService;
        this.campusAuditLogService = campusAuditLogService;
        this.userUtil = userUtil;
    }

    @GetMapping("/topic/list")
    public ResultVO<PageInfo<CampusDetectionTopic>> listTopics(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String topicCategory,
            @RequestParam(required = false) Integer enabled) {
        return ResultVO.success(campusDetectionService.listTopics(pageNum, pageSize, keyword, topicCategory, enabled));
    }

    @PostMapping("/topic/save")
    public ResultVO<CampusDetectionTopic> saveTopic(@RequestBody CampusDetectionTopic topic,
                                                    HttpServletRequest request) {
        try {
            User user = userUtil.getuser(request);
            CampusDetectionTopic saved = campusDetectionService.saveTopic(topic, user.getUser_id());
            campusAuditLogService.record(request, "检测引擎", "保存检测主题", "campus_detection_topic",
                    String.valueOf(saved.getTopicId()), JSON.toJSONString(topic), true, null);
            return ResultVO.success(saved);
        } catch (Exception e) {
            campusAuditLogService.record(request, "检测引擎", "保存检测主题", "campus_detection_topic",
                    topic == null ? null : String.valueOf(topic.getTopicId()), JSON.toJSONString(topic), false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @PostMapping("/topic/delete")
    public ResultVO<Void> deleteTopic(@RequestParam Long topicId, HttpServletRequest request) {
        String params = "topicId=" + topicId;
        try {
            User user = userUtil.getuser(request);
            campusDetectionService.deleteTopic(topicId, user.getUser_id());
            campusAuditLogService.record(request, "检测引擎", "删除检测主题", "campus_detection_topic",
                    String.valueOf(topicId), params, true, null);
            return ResultVO.success();
        } catch (Exception e) {
            campusAuditLogService.record(request, "检测引擎", "删除检测主题", "campus_detection_topic",
                    String.valueOf(topicId), params, false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @GetMapping("/rule/list")
    public ResultVO<PageInfo<CampusDetectionRule>> listRules(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long topicId,
            @RequestParam(required = false) String ruleType,
            @RequestParam(required = false) Integer enabled) {
        return ResultVO.success(campusDetectionService.listRules(pageNum, pageSize, topicId, ruleType, enabled));
    }

    @PostMapping("/rule/save")
    public ResultVO<CampusDetectionRule> saveRule(@RequestBody CampusDetectionRule rule,
                                                  HttpServletRequest request) {
        try {
            User user = userUtil.getuser(request);
            CampusDetectionRule saved = campusDetectionService.saveRule(rule, user.getUser_id());
            campusAuditLogService.record(request, "检测引擎", "保存检测规则", "campus_detection_rule",
                    String.valueOf(saved.getRuleId()), JSON.toJSONString(rule), true, null);
            return ResultVO.success(saved);
        } catch (Exception e) {
            campusAuditLogService.record(request, "检测引擎", "保存检测规则", "campus_detection_rule",
                    rule == null ? null : String.valueOf(rule.getRuleId()), JSON.toJSONString(rule), false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @PostMapping("/rule/delete")
    public ResultVO<Void> deleteRule(@RequestParam Long ruleId, HttpServletRequest request) {
        String params = "ruleId=" + ruleId;
        try {
            User user = userUtil.getuser(request);
            campusDetectionService.deleteRule(ruleId, user.getUser_id());
            campusAuditLogService.record(request, "检测引擎", "删除检测规则", "campus_detection_rule",
                    String.valueOf(ruleId), params, true, null);
            return ResultVO.success();
        } catch (Exception e) {
            campusAuditLogService.record(request, "检测引擎", "删除检测规则", "campus_detection_rule",
                    String.valueOf(ruleId), params, false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @GetMapping("/task/list")
    public ResultVO<PageInfo<CampusDetectionTask>> listTasks(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long topicId,
            @RequestParam(required = false) String taskStatus) {
        return ResultVO.success(campusDetectionService.listTasks(pageNum, pageSize, keyword, topicId, taskStatus));
    }

    @PostMapping("/task/save")
    public ResultVO<CampusDetectionTask> saveTask(@RequestBody CampusDetectionTask task,
                                                  HttpServletRequest request) {
        try {
            User user = userUtil.getuser(request);
            CampusDetectionTask saved = campusDetectionService.saveTask(task, user.getUser_id());
            campusAuditLogService.record(request, "检测引擎", "保存检测任务", "campus_detection_task",
                    String.valueOf(saved.getDetectionTaskId()), JSON.toJSONString(task), true, null);
            return ResultVO.success(saved);
        } catch (Exception e) {
            campusAuditLogService.record(request, "检测引擎", "保存检测任务", "campus_detection_task",
                    task == null ? null : String.valueOf(task.getDetectionTaskId()), JSON.toJSONString(task), false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @PostMapping("/task/update-status")
    public ResultVO<CampusDetectionTask> updateTaskStatus(@RequestParam Long detectionTaskId,
                                                          @RequestParam String taskStatus,
                                                          HttpServletRequest request) {
        String params = "detectionTaskId=" + detectionTaskId + "&taskStatus=" + taskStatus;
        try {
            User user = userUtil.getuser(request);
            CampusDetectionTask saved = campusDetectionService.updateTaskStatus(detectionTaskId, taskStatus, user.getUser_id());
            campusAuditLogService.record(request, "检测引擎", "更新检测任务状态", "campus_detection_task",
                    String.valueOf(detectionTaskId), params, true, null);
            return ResultVO.success(saved);
        } catch (Exception e) {
            campusAuditLogService.record(request, "检测引擎", "更新检测任务状态", "campus_detection_task",
                    String.valueOf(detectionTaskId), params, false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @PostMapping("/task/delete")
    public ResultVO<Void> deleteTask(@RequestParam Long detectionTaskId, HttpServletRequest request) {
        String params = "detectionTaskId=" + detectionTaskId;
        try {
            User user = userUtil.getuser(request);
            campusDetectionService.deleteTask(detectionTaskId, user.getUser_id());
            campusAuditLogService.record(request, "检测引擎", "删除检测任务", "campus_detection_task",
                    String.valueOf(detectionTaskId), params, true, null);
            return ResultVO.success();
        } catch (Exception e) {
            campusAuditLogService.record(request, "检测引擎", "删除检测任务", "campus_detection_task",
                    String.valueOf(detectionTaskId), params, false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @PostMapping("/task/run")
    public ResultVO<CampusDetectionRunLog> runTask(@RequestParam Long detectionTaskId,
                                                   HttpServletRequest request) {
        String params = "detectionTaskId=" + detectionTaskId;
        try {
            User user = userUtil.getuser(request);
            CampusDetectionRunLog runLog = campusDetectionService.runTask(detectionTaskId, user.getUser_id());
            campusAuditLogService.record(request, "检测引擎", "运行检测任务", "campus_detection_task",
                    String.valueOf(detectionTaskId), params, true, null);
            return ResultVO.success(runLog);
        } catch (Exception e) {
            campusAuditLogService.record(request, "检测引擎", "运行检测任务", "campus_detection_task",
                    String.valueOf(detectionTaskId), params, false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @GetMapping("/hit/list")
    public ResultVO<PageInfo<CampusDetectionHit>> listHits(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long detectionTaskId,
            @RequestParam(required = false) Long topicId,
            @RequestParam(required = false) String objectType,
            @RequestParam(required = false) String hitStatus,
            @RequestParam(required = false) String riskLevel,
            @RequestParam(required = false) String keyword) {
        return ResultVO.success(campusDetectionService.listHits(pageNum, pageSize, detectionTaskId,
                topicId, objectType, hitStatus, riskLevel, keyword));
    }

    @PostMapping("/hit/alert")
    public ResultVO<CampusDetectionHit> alertHit(@RequestParam Long hitId, HttpServletRequest request) {
        String params = "hitId=" + hitId;
        try {
            User user = userUtil.getuser(request);
            CampusDetectionHit hit = campusDetectionService.alertHit(hitId, user.getUser_id());
            campusAuditLogService.record(request, "检测引擎", "检测命中转预警", "campus_detection_hit",
                    String.valueOf(hitId), params, true, null);
            return ResultVO.success(hit);
        } catch (Exception e) {
            campusAuditLogService.record(request, "检测引擎", "检测命中转预警", "campus_detection_hit",
                    String.valueOf(hitId), params, false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @PostMapping("/hit/ignore")
    public ResultVO<CampusDetectionHit> ignoreHit(@RequestParam Long hitId, HttpServletRequest request) {
        String params = "hitId=" + hitId;
        try {
            User user = userUtil.getuser(request);
            CampusDetectionHit hit = campusDetectionService.ignoreHit(hitId, user.getUser_id());
            campusAuditLogService.record(request, "检测引擎", "忽略检测命中", "campus_detection_hit",
                    String.valueOf(hitId), params, true, null);
            return ResultVO.success(hit);
        } catch (Exception e) {
            campusAuditLogService.record(request, "检测引擎", "忽略检测命中", "campus_detection_hit",
                    String.valueOf(hitId), params, false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @GetMapping("/run-log/list")
    public ResultVO<PageInfo<CampusDetectionRunLog>> listRunLogs(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam Long detectionTaskId) {
        return ResultVO.success(campusDetectionService.listRunLogs(pageNum, pageSize, detectionTaskId));
    }
}
