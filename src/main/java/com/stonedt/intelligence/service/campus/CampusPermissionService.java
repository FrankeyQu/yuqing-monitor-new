package com.stonedt.intelligence.service.campus;

import com.github.pagehelper.PageInfo;
import com.stonedt.intelligence.entity.User;
import com.stonedt.intelligence.entity.campus.CampusCurrentUser;
import com.stonedt.intelligence.entity.campus.CampusPermissionApi;
import com.stonedt.intelligence.entity.campus.CampusPermissionMenu;
import com.stonedt.intelligence.entity.campus.CampusPermissionRole;

import java.util.List;

public interface CampusPermissionService {

    CampusCurrentUser currentUser(User user);

    List<CampusPermissionMenu> menuTree(Long userId);

    boolean hasApiPermission(Long userId, String method, String path);

    PageInfo<CampusPermissionRole> listRoles(Integer pageNum, Integer pageSize,
                                             String keyword, String roleType, Integer status);

    CampusPermissionRole saveRole(CampusPermissionRole role, Long operatorUserId);

    void deleteRole(Long roleId, Long operatorUserId);

    List<CampusPermissionMenu> listMenus();

    PageInfo<CampusPermissionApi> listApis(Integer pageNum, Integer pageSize,
                                           String keyword, String moduleName, Integer status);

    List<Long> listRoleMenuIds(Long roleId);

    List<Long> listRoleApiIds(Long roleId);

    void assignRoleMenus(Long roleId, List<Long> menuIds, Long operatorUserId);

    void assignRoleApis(Long roleId, List<Long> apiIds, Long operatorUserId);
}
