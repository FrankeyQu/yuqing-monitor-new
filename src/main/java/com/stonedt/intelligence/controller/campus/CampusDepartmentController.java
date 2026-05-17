package com.stonedt.intelligence.controller.campus;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageInfo;
import com.stonedt.intelligence.entity.User;
import com.stonedt.intelligence.entity.campus.CampusDepartment;
import com.stonedt.intelligence.service.campus.CampusAuditLogService;
import com.stonedt.intelligence.service.campus.CampusDepartmentService;
import com.stonedt.intelligence.util.UserUtil;
import com.stonedt.intelligence.vo.ResultVO;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/campus/department")
public class CampusDepartmentController {

    private final CampusDepartmentService campusDepartmentService;
    private final CampusAuditLogService campusAuditLogService;
    private final UserUtil userUtil;

    public CampusDepartmentController(CampusDepartmentService campusDepartmentService,
                                      CampusAuditLogService campusAuditLogService,
                                      UserUtil userUtil) {
        this.campusDepartmentService = campusDepartmentService;
        this.campusAuditLogService = campusAuditLogService;
        this.userUtil = userUtil;
    }

    @GetMapping("/list")
    public ResultVO<PageInfo<CampusDepartment>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                                     @RequestParam(defaultValue = "10") Integer pageSize,
                                                     @RequestParam(required = false) String departmentName,
                                                     @RequestParam(required = false) Long parentId,
                                                     @RequestParam(required = false) Integer status) {
        return ResultVO.success(campusDepartmentService.list(pageNum, pageSize, departmentName, parentId, status));
    }

    @GetMapping("/tree")
    public ResultVO<List<CampusDepartment>> tree() {
        return ResultVO.success(campusDepartmentService.tree());
    }

    @GetMapping("/detail")
    public ResultVO<CampusDepartment> detail(@RequestParam Long departmentId) {
        return ResultVO.success(campusDepartmentService.detail(departmentId));
    }

    @PostMapping("/save")
    public ResultVO<CampusDepartment> save(@RequestBody CampusDepartment department,
                                           HttpServletRequest request) {
        try {
            User user = userUtil.getuser(request);
            CampusDepartment saved = campusDepartmentService.save(department, user.getUser_id());
            campusAuditLogService.record(request, "组织机构", "保存", "campus_department",
                    String.valueOf(saved.getDepartmentId()), JSON.toJSONString(department), true, null);
            return ResultVO.success(saved);
        } catch (Exception e) {
            campusAuditLogService.record(request, "组织机构", "保存", "campus_department",
                    department == null ? null : String.valueOf(department.getDepartmentId()),
                    JSON.toJSONString(department), false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @PostMapping("/delete")
    public ResultVO<Void> delete(@RequestParam Long departmentId, HttpServletRequest request) {
        try {
            User user = userUtil.getuser(request);
            campusDepartmentService.delete(departmentId, user.getUser_id());
            campusAuditLogService.record(request, "组织机构", "删除", "campus_department",
                    String.valueOf(departmentId), "departmentId=" + departmentId, true, null);
            return ResultVO.success();
        } catch (Exception e) {
            campusAuditLogService.record(request, "组织机构", "删除", "campus_department",
                    String.valueOf(departmentId), "departmentId=" + departmentId, false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }
}
