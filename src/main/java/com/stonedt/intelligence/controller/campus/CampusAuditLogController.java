package com.stonedt.intelligence.controller.campus;

import com.github.pagehelper.PageInfo;
import com.stonedt.intelligence.entity.campus.CampusAuditLog;
import com.stonedt.intelligence.service.campus.CampusAuditLogService;
import com.stonedt.intelligence.vo.ResultVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/campus/audit")
public class CampusAuditLogController {

    private final CampusAuditLogService campusAuditLogService;

    public CampusAuditLogController(CampusAuditLogService campusAuditLogService) {
        this.campusAuditLogService = campusAuditLogService;
    }

    @GetMapping("/list")
    public ResultVO<PageInfo<CampusAuditLog>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                                   @RequestParam(defaultValue = "10") Integer pageSize,
                                                   @RequestParam(required = false) String moduleName,
                                                   @RequestParam(required = false) String operationType,
                                                   @RequestParam(required = false) String objectType,
                                                   @RequestParam(required = false) String objectId,
                                                   @RequestParam(required = false) String operatorName) {
        return ResultVO.success(campusAuditLogService.list(pageNum, pageSize, moduleName, operationType,
                objectType, objectId, operatorName));
    }
}
