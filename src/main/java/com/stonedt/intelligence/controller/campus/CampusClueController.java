package com.stonedt.intelligence.controller.campus;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageInfo;
import com.stonedt.intelligence.dao.campus.CampusClueDao;
import com.stonedt.intelligence.entity.User;
import com.stonedt.intelligence.entity.campus.CampusClue;
import com.stonedt.intelligence.entity.campus.CampusClueOperationLog;
import com.stonedt.intelligence.service.campus.CampusAuditLogService;
import com.stonedt.intelligence.service.campus.CampusClueService;
import com.stonedt.intelligence.util.UserUtil;
import com.stonedt.intelligence.vo.ResultVO;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/campus/clue")
public class CampusClueController {

    private final CampusClueService campusClueService;
    private final CampusAuditLogService campusAuditLogService;
    private final CampusClueDao campusClueDao;
    private final UserUtil userUtil;

    public CampusClueController(CampusClueService campusClueService,
                                CampusAuditLogService campusAuditLogService,
                                CampusClueDao campusClueDao,
                                UserUtil userUtil) {
        this.campusClueService = campusClueService;
        this.campusAuditLogService = campusAuditLogService;
        this.campusClueDao = campusClueDao;
        this.userUtil = userUtil;
    }

    @GetMapping("/list")
    public ResultVO<PageInfo<CampusClue>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String clueSource,
            @RequestParam(required = false) String sourcePlatform,
            @RequestParam(required = false) String sourceSubPlatform,
            @RequestParam(required = false) String riskLevel,
            @RequestParam(required = false) String clueStatus,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String sentiment,
            @RequestParam(required = false) String articleStatus,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") Date startTime,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") Date endTime,
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
            @RequestParam(required = false) String sortBy) {
        return ResultVO.success(campusClueService.list(pageNum, pageSize, keyword, clueSource,
                sourcePlatform, sourceSubPlatform, riskLevel, clueStatus, language, sentiment, articleStatus,
                startTime, endTime, publishTimeStart, publishTimeEnd,
                collectTimeStart, collectTimeEnd, matchScope, similarDedup, sortBy));
    }

    @GetMapping("/detail")
    public ResultVO<CampusClue> detail(@RequestParam Long clueId) {
        return ResultVO.success(campusClueService.detail(clueId));
    }

    @GetMapping("/operation-logs")
    public ResultVO<List<CampusClueOperationLog>> operationLogs(@RequestParam Long clueId) {
        return ResultVO.success(campusClueService.operationLogs(clueId));
    }

    @GetMapping("/count-by-media-type")
    public ResultVO<List<Map<String, Object>>> countByMediaType(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String clueSource,
            @RequestParam(required = false) String sourcePlatform,
            @RequestParam(required = false) String sourceSubPlatform,
            @RequestParam(required = false) String riskLevel,
            @RequestParam(required = false) String clueStatus,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String sentiment,
            @RequestParam(required = false) String articleStatus,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") Date startTime,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") Date endTime,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") Date publishTimeStart,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") Date publishTimeEnd,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") Date collectTimeStart,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") Date collectTimeEnd,
            @RequestParam(required = false) String matchScope,
            @RequestParam(required = false) Boolean similarDedup) {
        return ResultVO.success(campusClueService.countByMediaType(keyword, clueSource,
                sourcePlatform, sourceSubPlatform, riskLevel, clueStatus, language, sentiment, articleStatus,
                startTime, endTime, publishTimeStart, publishTimeEnd,
                collectTimeStart, collectTimeEnd, matchScope, similarDedup));
    }

    @GetMapping("/count-by-sub-platform")
    public ResultVO<List<Map<String, Object>>> countBySubPlatform(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String clueSource,
            @RequestParam(required = false) String sourcePlatform,
            @RequestParam(required = false) String sourceSubPlatform,
            @RequestParam(required = false) String riskLevel,
            @RequestParam(required = false) String clueStatus,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String sentiment,
            @RequestParam(required = false) String articleStatus,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") Date startTime,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") Date endTime,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") Date publishTimeStart,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") Date publishTimeEnd,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") Date collectTimeStart,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") Date collectTimeEnd,
            @RequestParam(required = false) String matchScope,
            @RequestParam(required = false) Boolean similarDedup) {
        return ResultVO.success(campusClueService.countBySubPlatform(keyword, clueSource,
                sourcePlatform, sourceSubPlatform, riskLevel, clueStatus, language, sentiment, articleStatus,
                startTime, endTime, publishTimeStart, publishTimeEnd,
                collectTimeStart, collectTimeEnd, matchScope, similarDedup));
    }

    @PostMapping("/save")
    public ResultVO<CampusClue> save(@RequestBody CampusClue clue, HttpServletRequest request) {
        try {
            User user = userUtil.getuser(request);
            CampusClue saved = campusClueService.save(clue, user.getUser_id(), user.getUsername());
            campusAuditLogService.record(request, "线索库", "保存", "campus_clue",
                    String.valueOf(saved.getClueId()), JSON.toJSONString(clue), true, null);
            return ResultVO.success(saved);
        } catch (Exception e) {
            campusAuditLogService.record(request, "线索库", "保存", "campus_clue",
                    clue == null ? null : String.valueOf(clue.getClueId()), JSON.toJSONString(clue), false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @PostMapping("/judge")
    public ResultVO<CampusClue> judge(@RequestParam Long clueId,
                                      @RequestParam String riskLevel,
                                      @RequestParam(required = false) String judgeOpinion,
                                      HttpServletRequest request) {
        String params = "clueId=" + clueId + "&riskLevel=" + riskLevel;
        try {
            User user = userUtil.getuser(request);
            CampusClue saved = campusClueService.judge(clueId, riskLevel, judgeOpinion,
                    user.getUser_id(), user.getUsername());
            campusAuditLogService.record(request, "线索库", "研判", "campus_clue",
                    String.valueOf(clueId), params, true, null);
            return ResultVO.success(saved);
        } catch (Exception e) {
            campusAuditLogService.record(request, "线索库", "研判", "campus_clue",
                    String.valueOf(clueId), params, false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @PostMapping("/archive")
    public ResultVO<CampusClue> archive(@RequestParam Long clueId,
                                        @RequestParam(required = false) String archiveReason,
                                        HttpServletRequest request) {
        String params = "clueId=" + clueId;
        try {
            User user = userUtil.getuser(request);
            CampusClue saved = campusClueService.archive(clueId, archiveReason,
                    user.getUser_id(), user.getUsername());
            campusAuditLogService.record(request, "线索库", "归档", "campus_clue",
                    String.valueOf(clueId), params, true, null);
            return ResultVO.success(saved);
        } catch (Exception e) {
            campusAuditLogService.record(request, "线索库", "归档", "campus_clue",
                    String.valueOf(clueId), params, false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @PostMapping("/delete")
    public ResultVO<Void> delete(@RequestParam Long clueId, HttpServletRequest request) {
        String params = "clueId=" + clueId;
        try {
            User user = userUtil.getuser(request);
            campusClueService.delete(clueId, user.getUser_id(), user.getUsername());
            campusAuditLogService.record(request, "线索库", "删除", "campus_clue",
                    String.valueOf(clueId), params, true, null);
            return ResultVO.success();
        } catch (Exception e) {
            campusAuditLogService.record(request, "线索库", "删除", "campus_clue",
                    String.valueOf(clueId), params, false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @GetMapping("/suggest")
    public ResultVO<List<String>> suggest(@RequestParam String keyword) {
        return ResultVO.success(campusClueDao.suggestKeywords(keyword));
    }
}
