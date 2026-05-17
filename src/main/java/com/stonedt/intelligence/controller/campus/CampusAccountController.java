package com.stonedt.intelligence.controller.campus;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageInfo;
import com.stonedt.intelligence.entity.User;
import com.stonedt.intelligence.entity.campus.CampusAccount;
import com.stonedt.intelligence.entity.campus.CampusAccountContent;
import com.stonedt.intelligence.entity.campus.CampusAccountTask;
import com.stonedt.intelligence.service.campus.CampusAccountService;
import com.stonedt.intelligence.service.campus.CampusAuditLogService;
import com.stonedt.intelligence.util.UserUtil;
import com.stonedt.intelligence.vo.ResultVO;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/campus/account")
public class CampusAccountController {

    private final CampusAccountService campusAccountService;
    private final CampusAuditLogService campusAuditLogService;
    private final UserUtil userUtil;

    public CampusAccountController(CampusAccountService campusAccountService,
                                   CampusAuditLogService campusAuditLogService,
                                   UserUtil userUtil) {
        this.campusAccountService = campusAccountService;
        this.campusAuditLogService = campusAuditLogService;
        this.userUtil = userUtil;
    }

    @GetMapping("/list")
    public ResultVO<PageInfo<CampusAccount>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                                  @RequestParam(defaultValue = "10") Integer pageSize,
                                                  @RequestParam(required = false) String keyword,
                                                  @RequestParam(required = false) String platform,
                                                  @RequestParam(required = false) String focusLevel,
                                                  @RequestParam(required = false) String auditStatus,
                                                  @RequestParam(required = false) String accountStatus,
                                                  @RequestParam(required = false)
                                                  @DateTimeFormat(pattern = "yyyy-MM-dd") Date focusEndBefore) {
        return ResultVO.success(campusAccountService.list(pageNum, pageSize, keyword, platform, focusLevel,
                auditStatus, accountStatus, focusEndBefore));
    }

    @GetMapping("/detail")
    public ResultVO<CampusAccount> detail(@RequestParam Long accountId) {
        return ResultVO.success(campusAccountService.detail(accountId));
    }

    @PostMapping("/save")
    public ResultVO<CampusAccount> save(@RequestBody CampusAccount account, HttpServletRequest request) {
        try {
            User user = userUtil.getuser(request);
            CampusAccount saved = campusAccountService.save(account, user.getUser_id());
            campusAuditLogService.record(request, "重点关注账号库", "保存", "campus_account",
                    String.valueOf(saved.getAccountId()), JSON.toJSONString(account), true, null);
            return ResultVO.success(saved);
        } catch (Exception e) {
            campusAuditLogService.record(request, "重点关注账号库", "保存", "campus_account",
                    account == null ? null : String.valueOf(account.getAccountId()),
                    JSON.toJSONString(account), false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @PostMapping("/audit")
    public ResultVO<CampusAccount> audit(@RequestParam Long accountId,
                                         @RequestParam String auditStatus,
                                         @RequestParam(required = false) String auditOpinion,
                                         HttpServletRequest request) {
        String params = "accountId=" + accountId + "&auditStatus=" + auditStatus;
        try {
            User user = userUtil.getuser(request);
            CampusAccount saved = campusAccountService.audit(accountId, auditStatus, auditOpinion, user.getUser_id());
            campusAuditLogService.record(request, "重点关注账号库", "审核", "campus_account",
                    String.valueOf(accountId), params, true, null);
            return ResultVO.success(saved);
        } catch (Exception e) {
            campusAuditLogService.record(request, "重点关注账号库", "审核", "campus_account",
                    String.valueOf(accountId), params, false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @PostMapping("/update-status")
    public ResultVO<CampusAccount> updateStatus(@RequestParam Long accountId,
                                                @RequestParam String accountStatus,
                                                HttpServletRequest request) {
        String params = "accountId=" + accountId + "&accountStatus=" + accountStatus;
        try {
            User user = userUtil.getuser(request);
            CampusAccount saved = campusAccountService.updateStatus(accountId, accountStatus, user.getUser_id());
            campusAuditLogService.record(request, "重点关注账号库", "状态变更", "campus_account",
                    String.valueOf(accountId), params, true, null);
            return ResultVO.success(saved);
        } catch (Exception e) {
            campusAuditLogService.record(request, "重点关注账号库", "状态变更", "campus_account",
                    String.valueOf(accountId), params, false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @PostMapping("/delete")
    public ResultVO<Void> delete(@RequestParam Long accountId, HttpServletRequest request) {
        String params = "accountId=" + accountId;
        try {
            User user = userUtil.getuser(request);
            campusAccountService.delete(accountId, user.getUser_id());
            campusAuditLogService.record(request, "重点关注账号库", "删除", "campus_account",
                    String.valueOf(accountId), params, true, null);
            return ResultVO.success();
        } catch (Exception e) {
            campusAuditLogService.record(request, "重点关注账号库", "删除", "campus_account",
                    String.valueOf(accountId), params, false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @PostMapping("/task/add")
    public ResultVO<CampusAccountTask> addTask(@RequestBody CampusAccountTask task, HttpServletRequest request) {
        try {
            User user = userUtil.getuser(request);
            CampusAccountTask saved = campusAccountService.addTask(task, user.getUser_id());
            campusAuditLogService.record(request, "重点关注账号库", "新增关注任务", "campus_account_task",
                    String.valueOf(saved.getTaskId()), JSON.toJSONString(task), true, null);
            return ResultVO.success(saved);
        } catch (Exception e) {
            campusAuditLogService.record(request, "重点关注账号库", "新增关注任务", "campus_account_task",
                    task == null ? null : String.valueOf(task.getTaskId()),
                    JSON.toJSONString(task), false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @GetMapping("/task/list")
    public ResultVO<List<CampusAccountTask>> listTasks(@RequestParam Long accountId) {
        return ResultVO.success(campusAccountService.listTasks(accountId));
    }

    @PostMapping("/content/add")
    public ResultVO<CampusAccountContent> addContent(@RequestBody CampusAccountContent content,
                                                     HttpServletRequest request) {
        try {
            User user = userUtil.getuser(request);
            CampusAccountContent saved = campusAccountService.addContent(content, user.getUser_id());
            campusAuditLogService.record(request, "重点关注账号库", "新增公开动态", "campus_account_content",
                    String.valueOf(saved.getContentId()), JSON.toJSONString(content), true, null);
            return ResultVO.success(saved);
        } catch (Exception e) {
            campusAuditLogService.record(request, "重点关注账号库", "新增公开动态", "campus_account_content",
                    content == null ? null : String.valueOf(content.getContentId()),
                    JSON.toJSONString(content), false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @GetMapping("/content/list")
    public ResultVO<PageInfo<CampusAccountContent>> listContents(@RequestParam(defaultValue = "1") Integer pageNum,
                                                                 @RequestParam(defaultValue = "10") Integer pageSize,
                                                                 @RequestParam(required = false) Long accountId,
                                                                 @RequestParam(required = false) Long taskId,
                                                                 @RequestParam(required = false) String riskLevel,
                                                                 @RequestParam(required = false) String keyword) {
        return ResultVO.success(campusAccountService.listContents(pageNum, pageSize, accountId,
                taskId, riskLevel, keyword));
    }
}
