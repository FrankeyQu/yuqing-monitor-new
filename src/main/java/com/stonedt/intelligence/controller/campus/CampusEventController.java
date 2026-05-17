package com.stonedt.intelligence.controller.campus;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageInfo;
import com.stonedt.intelligence.entity.User;
import com.stonedt.intelligence.entity.campus.*;
import com.stonedt.intelligence.service.campus.CampusAuditLogService;
import com.stonedt.intelligence.service.campus.CampusEventService;
import com.stonedt.intelligence.util.UserUtil;
import com.stonedt.intelligence.vo.ResultVO;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/campus/event")
public class CampusEventController {

    private final CampusEventService campusEventService;
    private final CampusAuditLogService campusAuditLogService;
    private final UserUtil userUtil;

    public CampusEventController(CampusEventService campusEventService,
                                 CampusAuditLogService campusAuditLogService,
                                 UserUtil userUtil) {
        this.campusEventService = campusEventService;
        this.campusAuditLogService = campusAuditLogService;
        this.userUtil = userUtil;
    }

    @GetMapping("/list")
    public ResultVO<PageInfo<CampusEvent>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                                @RequestParam(defaultValue = "10") Integer pageSize,
                                                @RequestParam(required = false) String keyword,
                                                @RequestParam(required = false) String riskLevel,
                                                @RequestParam(required = false) String eventStatus,
                                                @RequestParam(required = false)
                                                @DateTimeFormat(pattern = "yyyy-MM-dd") Date startTime,
                                                @RequestParam(required = false)
                                                @DateTimeFormat(pattern = "yyyy-MM-dd") Date endTime) {
        return ResultVO.success(campusEventService.list(pageNum, pageSize, keyword, riskLevel,
                eventStatus, startTime, endTime));
    }

    @GetMapping("/detail")
    public ResultVO<CampusEvent> detail(@RequestParam Long eventId) {
        return ResultVO.success(campusEventService.detail(eventId));
    }

    @PostMapping("/save")
    public ResultVO<CampusEvent> save(@RequestBody CampusEvent event, HttpServletRequest request) {
        try {
            User user = userUtil.getuser(request);
            CampusEvent saved = campusEventService.save(event, user.getUser_id());
            campusAuditLogService.record(request, "舆情事件库", "保存", "campus_event",
                    String.valueOf(saved.getEventId()), JSON.toJSONString(event), true, null);
            return ResultVO.success(saved);
        } catch (Exception e) {
            campusAuditLogService.record(request, "舆情事件库", "保存", "campus_event",
                    event == null ? null : String.valueOf(event.getEventId()),
                    JSON.toJSONString(event), false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @PostMapping("/create-from-clue")
    public ResultVO<CampusEvent> createFromClue(@RequestParam Long clueId,
                                                @RequestBody(required = false) CampusEvent event,
                                                HttpServletRequest request) {
        String params = "clueId=" + clueId;
        try {
            User user = userUtil.getuser(request);
            CampusEvent saved = campusEventService.createFromClue(clueId, event, user.getUser_id());
            campusAuditLogService.record(request, "舆情事件库", "线索转事件", "campus_event",
                    String.valueOf(saved.getEventId()), params, true, null);
            return ResultVO.success(saved);
        } catch (Exception e) {
            campusAuditLogService.record(request, "舆情事件库", "线索转事件", "campus_event",
                    null, params, false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @PostMapping("/rate")
    public ResultVO<CampusEvent> rate(@RequestParam Long eventId,
                                      @RequestParam String riskLevel,
                                      @RequestParam(required = false) String disposalRequirement,
                                      HttpServletRequest request) {
        String params = "eventId=" + eventId + "&riskLevel=" + riskLevel;
        try {
            User user = userUtil.getuser(request);
            CampusEvent saved = campusEventService.rate(eventId, riskLevel, disposalRequirement, user.getUser_id());
            campusAuditLogService.record(request, "舆情事件库", "风险定级", "campus_event",
                    String.valueOf(eventId), params, true, null);
            return ResultVO.success(saved);
        } catch (Exception e) {
            campusAuditLogService.record(request, "舆情事件库", "风险定级", "campus_event",
                    String.valueOf(eventId), params, false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @PostMapping("/account/add")
    public ResultVO<CampusEventAccount> addAccount(@RequestParam Long eventId,
                                                   @RequestParam Long accountId,
                                                   HttpServletRequest request) {
        String params = "eventId=" + eventId + "&accountId=" + accountId;
        try {
            User user = userUtil.getuser(request);
            CampusEventAccount relation = campusEventService.addAccount(eventId, accountId, user.getUser_id());
            campusAuditLogService.record(request, "舆情事件库", "关联账号", "campus_event_account",
                    String.valueOf(relation.getRelationId()), params, true, null);
            return ResultVO.success(relation);
        } catch (Exception e) {
            campusAuditLogService.record(request, "舆情事件库", "关联账号", "campus_event_account",
                    null, params, false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @PostMapping("/assign")
    public ResultVO<CampusDisposalTask> assign(@RequestBody CampusDisposalTask task, HttpServletRequest request) {
        try {
            User user = userUtil.getuser(request);
            CampusDisposalTask saved = campusEventService.assign(task, user.getUser_id());
            campusAuditLogService.record(request, "处置流转", "分派", "campus_disposal_task",
                    String.valueOf(saved.getDisposalTaskId()), JSON.toJSONString(task), true, null);
            return ResultVO.success(saved);
        } catch (Exception e) {
            campusAuditLogService.record(request, "处置流转", "分派", "campus_disposal_task",
                    task == null ? null : String.valueOf(task.getDisposalTaskId()),
                    JSON.toJSONString(task), false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @PostMapping("/feedback")
    public ResultVO<CampusDisposalRecord> feedback(@RequestParam Long disposalTaskId,
                                                   @RequestParam String recordContent,
                                                   @RequestParam(required = false) String attachmentDesc,
                                                   HttpServletRequest request) {
        return handleDisposalRecord("处置反馈", disposalTaskId, recordContent, attachmentDesc, request);
    }

    @PostMapping("/return")
    public ResultVO<CampusDisposalRecord> returnTask(@RequestParam Long disposalTaskId,
                                                     @RequestParam String recordContent,
                                                     HttpServletRequest request) {
        return handleDisposalRecord("退回重办", disposalTaskId, recordContent, null, request);
    }

    @PostMapping("/confirm")
    public ResultVO<CampusDisposalRecord> confirm(@RequestParam Long disposalTaskId,
                                                  @RequestParam String recordContent,
                                                  HttpServletRequest request) {
        return handleDisposalRecord("复核确认", disposalTaskId, recordContent, null, request);
    }

    @PostMapping("/archive")
    public ResultVO<CampusEvent> archive(@RequestParam Long eventId,
                                         @RequestParam String archiveConclusion,
                                         HttpServletRequest request) {
        String params = "eventId=" + eventId;
        try {
            User user = userUtil.getuser(request);
            CampusEvent saved = campusEventService.archive(eventId, archiveConclusion, user.getUser_id());
            campusAuditLogService.record(request, "舆情事件库", "归档", "campus_event",
                    String.valueOf(eventId), params, true, null);
            return ResultVO.success(saved);
        } catch (Exception e) {
            campusAuditLogService.record(request, "舆情事件库", "归档", "campus_event",
                    String.valueOf(eventId), params, false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @GetMapping("/clue/list")
    public ResultVO<List<CampusEventClue>> listClues(@RequestParam Long eventId) {
        return ResultVO.success(campusEventService.listClues(eventId));
    }

    @GetMapping("/clue/suggest")
    public ResultVO<List<CampusClue>> suggestSimilarClues(@RequestParam Long eventId,
                                                          @RequestParam(required = false, defaultValue = "10") Integer limit) {
        return ResultVO.success(campusEventService.suggestSimilarClues(eventId, limit));
    }

    @GetMapping("/account/list")
    public ResultVO<List<CampusEventAccount>> listAccounts(@RequestParam Long eventId) {
        return ResultVO.success(campusEventService.listAccounts(eventId));
    }

    @GetMapping("/task/list")
    public ResultVO<List<CampusDisposalTask>> listTasks(@RequestParam Long eventId) {
        return ResultVO.success(campusEventService.listTasks(eventId));
    }

    @GetMapping("/record/list")
    public ResultVO<List<CampusDisposalRecord>> listRecords(@RequestParam Long disposalTaskId) {
        return ResultVO.success(campusEventService.listRecords(disposalTaskId));
    }

    private ResultVO<CampusDisposalRecord> handleDisposalRecord(String operation,
                                                                Long disposalTaskId,
                                                                String recordContent,
                                                                String attachmentDesc,
                                                                HttpServletRequest request) {
        String params = "disposalTaskId=" + disposalTaskId;
        try {
            User user = userUtil.getuser(request);
            CampusDisposalRecord record;
            if ("处置反馈".equals(operation)) {
                record = campusEventService.feedback(disposalTaskId, recordContent, attachmentDesc,
                        user.getUser_id(), user.getUsername());
            } else if ("退回重办".equals(operation)) {
                record = campusEventService.returnTask(disposalTaskId, recordContent,
                        user.getUser_id(), user.getUsername());
            } else {
                record = campusEventService.confirm(disposalTaskId, recordContent,
                        user.getUser_id(), user.getUsername());
            }
            campusAuditLogService.record(request, "处置流转", operation, "campus_disposal_record",
                    String.valueOf(record.getRecordId()), params, true, null);
            return ResultVO.success(record);
        } catch (Exception e) {
            campusAuditLogService.record(request, "处置流转", operation, "campus_disposal_record",
                    null, params, false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }
}
