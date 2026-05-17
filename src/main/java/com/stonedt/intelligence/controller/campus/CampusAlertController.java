package com.stonedt.intelligence.controller.campus;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageInfo;
import com.stonedt.intelligence.entity.User;
import com.stonedt.intelligence.entity.campus.CampusAlert;
import com.stonedt.intelligence.entity.campus.CampusAlertRule;
import com.stonedt.intelligence.entity.campus.CampusSensitiveWord;
import com.stonedt.intelligence.service.campus.CampusAlertService;
import com.stonedt.intelligence.service.campus.CampusAuditLogService;
import com.stonedt.intelligence.util.UserUtil;
import com.stonedt.intelligence.vo.ResultVO;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/campus/alert")
public class CampusAlertController {

    private final CampusAlertService campusAlertService;
    private final CampusAuditLogService campusAuditLogService;
    private final UserUtil userUtil;

    public CampusAlertController(CampusAlertService campusAlertService,
                                 CampusAuditLogService campusAuditLogService,
                                 UserUtil userUtil) {
        this.campusAlertService = campusAlertService;
        this.campusAuditLogService = campusAuditLogService;
        this.userUtil = userUtil;
    }

    @GetMapping("/sensitive-word/list")
    public ResultVO<PageInfo<CampusSensitiveWord>> listSensitiveWords(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String wordCategory,
            @RequestParam(required = false) String riskLevel,
            @RequestParam(required = false) Integer status) {
        return ResultVO.success(campusAlertService.listSensitiveWords(pageNum, pageSize,
                keyword, wordCategory, riskLevel, status));
    }

    @PostMapping("/sensitive-word/save")
    public ResultVO<CampusSensitiveWord> saveSensitiveWord(@RequestBody CampusSensitiveWord word,
                                                           HttpServletRequest request) {
        try {
            User user = userUtil.getuser(request);
            CampusSensitiveWord saved = campusAlertService.saveSensitiveWord(word, user.getUser_id());
            campusAuditLogService.record(request, "预警中心", "保存敏感词", "campus_sensitive_word",
                    String.valueOf(saved.getWordId()), JSON.toJSONString(word), true, null);
            return ResultVO.success(saved);
        } catch (Exception e) {
            campusAuditLogService.record(request, "预警中心", "保存敏感词", "campus_sensitive_word",
                    word == null ? null : String.valueOf(word.getWordId()), JSON.toJSONString(word), false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @PostMapping("/sensitive-word/delete")
    public ResultVO<Void> deleteSensitiveWord(@RequestParam Long wordId, HttpServletRequest request) {
        String params = "wordId=" + wordId;
        try {
            User user = userUtil.getuser(request);
            campusAlertService.deleteSensitiveWord(wordId, user.getUser_id());
            campusAuditLogService.record(request, "预警中心", "删除敏感词", "campus_sensitive_word",
                    String.valueOf(wordId), params, true, null);
            return ResultVO.success();
        } catch (Exception e) {
            campusAuditLogService.record(request, "预警中心", "删除敏感词", "campus_sensitive_word",
                    String.valueOf(wordId), params, false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @GetMapping("/rule/list")
    public ResultVO<PageInfo<CampusAlertRule>> listRules(@RequestParam(defaultValue = "1") Integer pageNum,
                                                         @RequestParam(defaultValue = "10") Integer pageSize,
                                                         @RequestParam(required = false) String keyword,
                                                         @RequestParam(required = false) String ruleType,
                                                         @RequestParam(required = false) Integer enabled) {
        return ResultVO.success(campusAlertService.listRules(pageNum, pageSize, keyword, ruleType, enabled));
    }

    @PostMapping("/rule/save")
    public ResultVO<CampusAlertRule> saveRule(@RequestBody CampusAlertRule rule,
                                              HttpServletRequest request) {
        try {
            User user = userUtil.getuser(request);
            CampusAlertRule saved = campusAlertService.saveRule(rule, user.getUser_id());
            campusAuditLogService.record(request, "预警中心", "保存规则", "campus_alert_rule",
                    String.valueOf(saved.getRuleId()), JSON.toJSONString(rule), true, null);
            return ResultVO.success(saved);
        } catch (Exception e) {
            campusAuditLogService.record(request, "预警中心", "保存规则", "campus_alert_rule",
                    rule == null ? null : String.valueOf(rule.getRuleId()), JSON.toJSONString(rule), false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @PostMapping("/rule/delete")
    public ResultVO<Void> deleteRule(@RequestParam Long ruleId, HttpServletRequest request) {
        String params = "ruleId=" + ruleId;
        try {
            User user = userUtil.getuser(request);
            campusAlertService.deleteRule(ruleId, user.getUser_id());
            campusAuditLogService.record(request, "预警中心", "删除规则", "campus_alert_rule",
                    String.valueOf(ruleId), params, true, null);
            return ResultVO.success();
        } catch (Exception e) {
            campusAuditLogService.record(request, "预警中心", "删除规则", "campus_alert_rule",
                    String.valueOf(ruleId), params, false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @GetMapping("/list")
    public ResultVO<PageInfo<CampusAlert>> listAlerts(@RequestParam(defaultValue = "1") Integer pageNum,
                                                      @RequestParam(defaultValue = "10") Integer pageSize,
                                                      @RequestParam(required = false) String keyword,
                                                      @RequestParam(required = false) String alertSource,
                                                      @RequestParam(required = false) String riskLevel,
                                                      @RequestParam(required = false) String alertStatus) {
        return ResultVO.success(campusAlertService.listAlerts(pageNum, pageSize,
                keyword, alertSource, riskLevel, alertStatus));
    }

    @PostMapping("/create")
    public ResultVO<CampusAlert> createAlert(@RequestBody CampusAlert alert, HttpServletRequest request) {
        try {
            User user = userUtil.getuser(request);
            CampusAlert saved = campusAlertService.createAlert(alert, user.getUser_id());
            campusAuditLogService.record(request, "预警中心", "人工创建预警", "campus_alert",
                    String.valueOf(saved.getAlertId()), JSON.toJSONString(alert), true, null);
            return ResultVO.success(saved);
        } catch (Exception e) {
            campusAuditLogService.record(request, "预警中心", "人工创建预警", "campus_alert",
                    alert == null ? null : String.valueOf(alert.getAlertId()), JSON.toJSONString(alert), false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @PostMapping("/handle")
    public ResultVO<CampusAlert> handleAlert(@RequestParam Long alertId,
                                             @RequestParam String alertStatus,
                                             @RequestParam(required = false) String handleOpinion,
                                             HttpServletRequest request) {
        String params = "alertId=" + alertId + "&alertStatus=" + alertStatus;
        try {
            User user = userUtil.getuser(request);
            CampusAlert saved = campusAlertService.handleAlert(alertId, alertStatus, handleOpinion, user.getUser_id());
            campusAuditLogService.record(request, "预警中心", "处理预警", "campus_alert",
                    String.valueOf(alertId), params, true, null);
            return ResultVO.success(saved);
        } catch (Exception e) {
            campusAuditLogService.record(request, "预警中心", "处理预警", "campus_alert",
                    String.valueOf(alertId), params, false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @PostMapping("/evaluate-clue")
    public ResultVO<List<CampusAlert>> evaluateClue(@RequestParam Long clueId, HttpServletRequest request) {
        String params = "clueId=" + clueId;
        try {
            User user = userUtil.getuser(request);
            List<CampusAlert> alerts = campusAlertService.evaluateClue(clueId, user.getUser_id());
            campusAuditLogService.record(request, "预警中心", "线索预警评估", "campus_clue",
                    String.valueOf(clueId), params, true, null);
            return ResultVO.success(alerts);
        } catch (Exception e) {
            campusAuditLogService.record(request, "预警中心", "线索预警评估", "campus_clue",
                    String.valueOf(clueId), params, false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @PostMapping("/create-from-clue")
    public ResultVO<List<CampusAlert>> createFromClue(@RequestParam Long clueId, HttpServletRequest request) {
        String params = "clueId=" + clueId;
        try {
            User user = userUtil.getuser(request);
            List<CampusAlert> alerts = campusAlertService.evaluateClue(clueId, user.getUser_id());
            campusAuditLogService.record(request, "预警中心", "从线索生成预警", "campus_clue",
                    String.valueOf(clueId), params, true, null);
            return ResultVO.success(alerts);
        } catch (Exception e) {
            campusAuditLogService.record(request, "预警中心", "从线索生成预警", "campus_clue",
                    String.valueOf(clueId), params, false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @PostMapping("/evaluate-account-content")
    public ResultVO<List<CampusAlert>> evaluateAccountContent(@RequestParam Long contentId,
                                                              HttpServletRequest request) {
        String params = "contentId=" + contentId;
        try {
            User user = userUtil.getuser(request);
            List<CampusAlert> alerts = campusAlertService.evaluateAccountContent(contentId, user.getUser_id());
            campusAuditLogService.record(request, "预警中心", "账号内容预警评估", "campus_account_content",
                    String.valueOf(contentId), params, true, null);
            return ResultVO.success(alerts);
        } catch (Exception e) {
            campusAuditLogService.record(request, "预警中心", "账号内容预警评估", "campus_account_content",
                    String.valueOf(contentId), params, false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }
}
