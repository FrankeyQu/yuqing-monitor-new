package com.stonedt.intelligence.service.impl.campus;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.stonedt.intelligence.dao.campus.CampusPermissionDao;
import com.stonedt.intelligence.entity.User;
import com.stonedt.intelligence.entity.campus.CampusCurrentUser;
import com.stonedt.intelligence.entity.campus.CampusPermissionApi;
import com.stonedt.intelligence.entity.campus.CampusPermissionMenu;
import com.stonedt.intelligence.entity.campus.CampusPermissionRole;
import com.stonedt.intelligence.service.campus.CampusPermissionService;
import com.stonedt.intelligence.util.SnowflakeUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class CampusPermissionServiceImpl implements CampusPermissionService {

    private static final String ADMIN_ROLE_CODE = "campus_admin";

    private final CampusPermissionDao campusPermissionDao;

    public CampusPermissionServiceImpl(CampusPermissionDao campusPermissionDao) {
        this.campusPermissionDao = campusPermissionDao;
    }

    @Override
    public CampusCurrentUser currentUser(User user) {
        if (user == null || user.getUser_id() == null) {
            throw new IllegalArgumentException("当前用户不存在");
        }
        CampusCurrentUser current = new CampusCurrentUser();
        current.setUserId(user.getUser_id());
        current.setUsername(user.getUsername());
        current.setTelephone(user.getTelephone());
        current.setOrganizationId(user.getOrganization_id());
        List<CampusPermissionRole> roles = campusPermissionDao.listRolesByUserId(user.getUser_id());
        current.setRoles(roles);
        current.setMenus(menuTree(user.getUser_id()));
        current.setPermissions(permissionCodes(user.getUser_id(), roles));
        return current;
    }

    @Override
    public List<CampusPermissionMenu> menuTree(Long userId) {
        if (userId == null) {
            return new ArrayList<>();
        }
        return buildTree(campusPermissionDao.listMenusByUserId(userId, 1));
    }

    @Override
    public boolean hasApiPermission(Long userId, String method, String path) {
        if (userId == null || StringUtils.isBlank(path)) {
            return false;
        }
        List<CampusPermissionRole> roles = campusPermissionDao.listRolesByUserId(userId);
        for (CampusPermissionRole role : roles) {
            if (ADMIN_ROLE_CODE.equals(role.getRoleCode())) {
                return true;
            }
        }
        List<CampusPermissionApi> apis = campusPermissionDao.listApisByUserId(userId);
        for (CampusPermissionApi api : apis) {
            if (matchesMethod(api.getRequestMethod(), method) && matchesPath(api.getRequestPath(), path)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public PageInfo<CampusPermissionRole> listRoles(Integer pageNum, Integer pageSize,
                                                    String keyword, String roleType, Integer status) {
        PageHelper.startPage(defaultPageNum(pageNum), defaultPageSize(pageSize));
        return new PageInfo<>(campusPermissionDao.listRoles(keyword, roleType, status));
    }

    @Override
    public CampusPermissionRole saveRole(CampusPermissionRole role, Long operatorUserId) {
        validateRole(role);
        ensureRoleCodeUnique(role);
        if (role.getRoleId() == null) {
            role.setRoleId(SnowflakeUtil.getId());
            setRoleDefaults(role);
            role.setCreateUserId(operatorUserId);
            role.setUpdateUserId(operatorUserId);
            campusPermissionDao.insertRole(role);
            return role;
        }
        CampusPermissionRole old = requireRole(role.getRoleId());
        if (ADMIN_ROLE_CODE.equals(old.getRoleCode())) {
            role.setRoleCode(ADMIN_ROLE_CODE);
            role.setStatus(1);
        }
        role.setUpdateUserId(operatorUserId);
        setRoleDefaults(role);
        campusPermissionDao.updateRole(role);
        return campusPermissionDao.selectRoleById(role.getRoleId());
    }

    @Override
    public void deleteRole(Long roleId, Long operatorUserId) {
        CampusPermissionRole role = requireRole(roleId);
        if (ADMIN_ROLE_CODE.equals(role.getRoleCode())) {
            throw new IllegalArgumentException("默认管理员角色不能删除");
        }
        campusPermissionDao.logicalDeleteRole(roleId, operatorUserId);
    }

    @Override
    public List<CampusPermissionMenu> listMenus() {
        return buildTree(campusPermissionDao.listMenus(null, 1));
    }

    @Override
    public PageInfo<CampusPermissionApi> listApis(Integer pageNum, Integer pageSize,
                                                  String keyword, String moduleName, Integer status) {
        PageHelper.startPage(defaultPageNum(pageNum), defaultPageSize(pageSize));
        return new PageInfo<>(campusPermissionDao.listApis(keyword, moduleName, status));
    }

    @Override
    public List<Long> listRoleMenuIds(Long roleId) {
        requireRole(roleId);
        return campusPermissionDao.listRoleMenuIds(roleId);
    }

    @Override
    public List<Long> listRoleApiIds(Long roleId) {
        requireRole(roleId);
        return campusPermissionDao.listRoleApiIds(roleId);
    }

    @Override
    public void assignRoleMenus(Long roleId, List<Long> menuIds, Long operatorUserId) {
        requireRole(roleId);
        campusPermissionDao.deleteRoleMenus(roleId, operatorUserId);
        if (menuIds == null) {
            return;
        }
        Set<Long> uniqueIds = new HashSet<>(menuIds);
        for (Long menuId : uniqueIds) {
            if (menuId != null) {
                campusPermissionDao.insertRoleMenu(SnowflakeUtil.getId(), roleId, menuId, operatorUserId);
            }
        }
    }

    @Override
    public void assignRoleApis(Long roleId, List<Long> apiIds, Long operatorUserId) {
        requireRole(roleId);
        campusPermissionDao.deleteRoleApis(roleId, operatorUserId);
        if (apiIds == null) {
            return;
        }
        Set<Long> uniqueIds = new HashSet<>(apiIds);
        for (Long apiId : uniqueIds) {
            if (apiId != null) {
                campusPermissionDao.insertRoleApi(SnowflakeUtil.getId(), roleId, apiId, operatorUserId);
            }
        }
    }

    private List<String> permissionCodes(Long userId, List<CampusPermissionRole> roles) {
        List<String> permissions = new ArrayList<>();
        for (CampusPermissionRole role : roles) {
            permissions.add("role:" + role.getRoleCode());
        }
        for (CampusPermissionMenu menu : campusPermissionDao.listMenusByUserId(userId, null)) {
            if (StringUtils.isNotBlank(menu.getPermissionCode())) {
                permissions.add(menu.getPermissionCode());
            }
        }
        for (CampusPermissionApi api : campusPermissionDao.listApisByUserId(userId)) {
            if (StringUtils.isNotBlank(api.getApiCode())) {
                permissions.add(api.getApiCode());
            }
        }
        return permissions;
    }

    private List<CampusPermissionMenu> buildTree(List<CampusPermissionMenu> menus) {
        Map<Long, CampusPermissionMenu> menuMap = new HashMap<>();
        List<CampusPermissionMenu> roots = new ArrayList<>();
        for (CampusPermissionMenu menu : menus) {
            menu.getChildren().clear();
            menuMap.put(menu.getMenuId(), menu);
        }
        for (CampusPermissionMenu menu : menus) {
            Long parentId = menu.getParentId();
            if (parentId == null || parentId == 0 || !menuMap.containsKey(parentId)) {
                roots.add(menu);
            } else {
                menuMap.get(parentId).getChildren().add(menu);
            }
        }
        return roots;
    }

    private boolean matchesMethod(String allowed, String method) {
        return StringUtils.isBlank(allowed)
                || "ALL".equalsIgnoreCase(allowed)
                || allowed.equalsIgnoreCase(method);
    }

    private boolean matchesPath(String pattern, String path) {
        if (StringUtils.isBlank(pattern)) {
            return false;
        }
        if (pattern.equals(path)) {
            return true;
        }
        if (pattern.endsWith("/**")) {
            String prefix = pattern.substring(0, pattern.length() - 3);
            return path.equals(prefix) || path.startsWith(prefix + "/");
        }
        if (pattern.endsWith("*")) {
            String prefix = pattern.substring(0, pattern.length() - 1);
            return path.startsWith(prefix);
        }
        return false;
    }

    private void validateRole(CampusPermissionRole role) {
        if (role == null) {
            throw new IllegalArgumentException("角色不能为空");
        }
        if (StringUtils.isBlank(role.getRoleCode())) {
            throw new IllegalArgumentException("角色编码不能为空");
        }
        if (StringUtils.isBlank(role.getRoleName())) {
            throw new IllegalArgumentException("角色名称不能为空");
        }
    }

    private void setRoleDefaults(CampusPermissionRole role) {
        role.setRoleType(StringUtils.defaultIfBlank(role.getRoleType(), "business"));
        role.setDataScope(StringUtils.defaultIfBlank(role.getDataScope(), "school"));
        role.setStatus(role.getStatus() == null ? 1 : role.getStatus());
        role.setDeleted(0);
    }

    private void ensureRoleCodeUnique(CampusPermissionRole role) {
        int count = campusPermissionDao.countRoleCode(role.getRoleCode(), role.getRoleId());
        if (count > 0) {
            throw new IllegalArgumentException("角色编码已存在");
        }
    }

    private CampusPermissionRole requireRole(Long roleId) {
        if (roleId == null) {
            throw new IllegalArgumentException("角色ID不能为空");
        }
        CampusPermissionRole role = campusPermissionDao.selectRoleById(roleId);
        if (role == null) {
            throw new IllegalArgumentException("角色不存在");
        }
        return role;
    }

    private int defaultPageNum(Integer pageNum) {
        return pageNum == null || pageNum < 1 ? 1 : pageNum;
    }

    private int defaultPageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return 10;
        }
        return Math.min(pageSize, 100);
    }
}
