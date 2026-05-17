package com.stonedt.intelligence.controller.campus;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageInfo;
import com.stonedt.intelligence.entity.User;
import com.stonedt.intelligence.entity.campus.CampusPublicWebWhitelist;
import com.stonedt.intelligence.service.campus.CampusAuditLogService;
import com.stonedt.intelligence.service.campus.CampusPublicWebWhitelistService;
import com.stonedt.intelligence.util.UserUtil;
import com.stonedt.intelligence.vo.ResultVO;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/campus/ingest/public-web/whitelist")
public class CampusPublicWebWhitelistController {

    private final CampusPublicWebWhitelistService campusPublicWebWhitelistService;
    private final CampusAuditLogService campusAuditLogService;
    private final UserUtil userUtil;

    public CampusPublicWebWhitelistController(CampusPublicWebWhitelistService campusPublicWebWhitelistService,
                                              CampusAuditLogService campusAuditLogService,
                                              UserUtil userUtil) {
        this.campusPublicWebWhitelistService = campusPublicWebWhitelistService;
        this.campusAuditLogService = campusAuditLogService;
        this.userUtil = userUtil;
    }

    @GetMapping("/list")
    public ResultVO<PageInfo<CampusPublicWebWhitelist>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                                             @RequestParam(defaultValue = "10") Integer pageSize,
                                                             @RequestParam(required = false) String keyword,
                                                             @RequestParam(required = false) String siteDomain,
                                                             @RequestParam(required = false) Integer enabled) {
        return ResultVO.success(campusPublicWebWhitelistService.list(pageNum, pageSize, keyword, siteDomain, enabled));
    }

    @PostMapping("/save")
    public ResultVO<CampusPublicWebWhitelist> save(@RequestBody CampusPublicWebWhitelist whitelist,
                                                   HttpServletRequest request) {
        try {
            User user = userUtil.getuser(request);
            CampusPublicWebWhitelist saved = campusPublicWebWhitelistService.save(whitelist, user.getUser_id());
            campusAuditLogService.record(request, "公开网页白名单", "保存白名单", "campus_public_web_whitelist",
                    String.valueOf(saved.getWhitelistId()), JSON.toJSONString(whitelist), true, null);
            return ResultVO.success(saved);
        } catch (Exception e) {
            campusAuditLogService.record(request, "公开网页白名单", "保存白名单", "campus_public_web_whitelist",
                    whitelist == null ? null : String.valueOf(whitelist.getWhitelistId()),
                    JSON.toJSONString(whitelist), false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @PostMapping("/update-status")
    public ResultVO<CampusPublicWebWhitelist> updateStatus(@RequestParam Long whitelistId,
                                                           @RequestParam Integer enabled,
                                                           HttpServletRequest request) {
        String params = "whitelistId=" + whitelistId + "&enabled=" + enabled;
        try {
            User user = userUtil.getuser(request);
            CampusPublicWebWhitelist saved = campusPublicWebWhitelistService.updateStatus(whitelistId, enabled, user.getUser_id());
            campusAuditLogService.record(request, "公开网页白名单", "状态变更", "campus_public_web_whitelist",
                    String.valueOf(whitelistId), params, true, null);
            return ResultVO.success(saved);
        } catch (Exception e) {
            campusAuditLogService.record(request, "公开网页白名单", "状态变更", "campus_public_web_whitelist",
                    String.valueOf(whitelistId), params, false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @PostMapping("/delete")
    public ResultVO<Void> delete(@RequestParam Long whitelistId, HttpServletRequest request) {
        String params = "whitelistId=" + whitelistId;
        try {
            User user = userUtil.getuser(request);
            campusPublicWebWhitelistService.delete(whitelistId, user.getUser_id());
            campusAuditLogService.record(request, "公开网页白名单", "删除白名单", "campus_public_web_whitelist",
                    String.valueOf(whitelistId), params, true, null);
            return ResultVO.success();
        } catch (Exception e) {
            campusAuditLogService.record(request, "公开网页白名单", "删除白名单", "campus_public_web_whitelist",
                    String.valueOf(whitelistId), params, false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }
}
