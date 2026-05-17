package com.stonedt.intelligence.controller.campus;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageInfo;
import com.stonedt.intelligence.entity.User;
import com.stonedt.intelligence.entity.campus.CampusCurrentUser;
import com.stonedt.intelligence.entity.campus.CampusPermissionApi;
import com.stonedt.intelligence.entity.campus.CampusPermissionMenu;
import com.stonedt.intelligence.entity.campus.CampusPermissionRole;
import com.stonedt.intelligence.service.campus.CampusAuditLogService;
import com.stonedt.intelligence.service.campus.CampusPermissionService;
import com.stonedt.intelligence.util.UserUtil;
import com.stonedt.intelligence.vo.ResultVO;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/campus/system")
public class CampusSystemController {

    private final CampusPermissionService campusPermissionService;
    private final CampusAuditLogService campusAuditLogService;
    private final UserUtil userUtil;

    public CampusSystemController(CampusPermissionService campusPermissionService,
                                  CampusAuditLogService campusAuditLogService,
                                  UserUtil userUtil) {
        this.campusPermissionService = campusPermissionService;
        this.campusAuditLogService = campusAuditLogService;
        this.userUtil = userUtil;
    }

    @GetMapping("/current-user")
    public ResultVO<CampusCurrentUser> currentUser(HttpServletRequest request) {
        User user = userUtil.getuser(request);
        return ResultVO.success(campusPermissionService.currentUser(user));
    }

    @GetMapping("/menu-tree")
    public ResultVO<List<CampusPermissionMenu>> menuTree(HttpServletRequest request) {
        User user = userUtil.getuser(request);
        return ResultVO.success(campusPermissionService.menuTree(user.getUser_id()));
    }

    @GetMapping("/role/list")
    public ResultVO<PageInfo<CampusPermissionRole>> listRoles(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String roleType,
            @RequestParam(required = false) Integer status) {
        return ResultVO.success(campusPermissionService.listRoles(pageNum, pageSize, keyword, roleType, status));
    }

    @PostMapping("/role/save")
    public ResultVO<CampusPermissionRole> saveRole(@RequestBody CampusPermissionRole role,
                                                   HttpServletRequest request) {
        try {
            User user = userUtil.getuser(request);
            CampusPermissionRole saved = campusPermissionService.saveRole(role, user.getUser_id());
            campusAuditLogService.record(request, "权限管理", "保存角色", "campus_permission_role",
                    String.valueOf(saved.getRoleId()), JSON.toJSONString(role), true, null);
            return ResultVO.success(saved);
        } catch (Exception e) {
            campusAuditLogService.record(request, "权限管理", "保存角色", "campus_permission_role",
                    role == null ? null : String.valueOf(role.getRoleId()), JSON.toJSONString(role), false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @PostMapping("/role/delete")
    public ResultVO<Void> deleteRole(@RequestParam Long roleId, HttpServletRequest request) {
        String params = "roleId=" + roleId;
        try {
            User user = userUtil.getuser(request);
            campusPermissionService.deleteRole(roleId, user.getUser_id());
            campusAuditLogService.record(request, "权限管理", "删除角色", "campus_permission_role",
                    String.valueOf(roleId), params, true, null);
            return ResultVO.success();
        } catch (Exception e) {
            campusAuditLogService.record(request, "权限管理", "删除角色", "campus_permission_role",
                    String.valueOf(roleId), params, false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @GetMapping("/menu/list")
    public ResultVO<List<CampusPermissionMenu>> listMenus() {
        return ResultVO.success(campusPermissionService.listMenus());
    }

    @GetMapping("/api/list")
    public ResultVO<PageInfo<CampusPermissionApi>> listApis(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String moduleName,
            @RequestParam(required = false) Integer status) {
        return ResultVO.success(campusPermissionService.listApis(pageNum, pageSize, keyword, moduleName, status));
    }

    @GetMapping("/role/menu-ids")
    public ResultVO<List<Long>> listRoleMenuIds(@RequestParam Long roleId) {
        return ResultVO.success(campusPermissionService.listRoleMenuIds(roleId));
    }

    @GetMapping("/role/api-ids")
    public ResultVO<List<Long>> listRoleApiIds(@RequestParam Long roleId) {
        return ResultVO.success(campusPermissionService.listRoleApiIds(roleId));
    }

    @PostMapping("/role/assign-menus")
    public ResultVO<Void> assignRoleMenus(@RequestParam Long roleId,
                                          @RequestBody(required = false) List<Long> menuIds,
                                          HttpServletRequest request) {
        String params = "roleId=" + roleId + "&menuIds=" + JSON.toJSONString(menuIds);
        try {
            User user = userUtil.getuser(request);
            campusPermissionService.assignRoleMenus(roleId, menuIds, user.getUser_id());
            campusAuditLogService.record(request, "权限管理", "分配菜单", "campus_role_menu",
                    String.valueOf(roleId), params, true, null);
            return ResultVO.success();
        } catch (Exception e) {
            campusAuditLogService.record(request, "权限管理", "分配菜单", "campus_role_menu",
                    String.valueOf(roleId), params, false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @PostMapping("/role/assign-apis")
    public ResultVO<Void> assignRoleApis(@RequestParam Long roleId,
                                         @RequestBody(required = false) List<Long> apiIds,
                                         HttpServletRequest request) {
        String params = "roleId=" + roleId + "&apiIds=" + JSON.toJSONString(apiIds);
        try {
            User user = userUtil.getuser(request);
            campusPermissionService.assignRoleApis(roleId, apiIds, user.getUser_id());
            campusAuditLogService.record(request, "权限管理", "分配接口", "campus_role_api",
                    String.valueOf(roleId), params, true, null);
            return ResultVO.success();
        } catch (Exception e) {
            campusAuditLogService.record(request, "权限管理", "分配接口", "campus_role_api",
                    String.valueOf(roleId), params, false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }
}
