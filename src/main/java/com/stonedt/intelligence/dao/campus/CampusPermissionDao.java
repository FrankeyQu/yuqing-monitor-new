package com.stonedt.intelligence.dao.campus;

import com.stonedt.intelligence.entity.campus.CampusPermissionApi;
import com.stonedt.intelligence.entity.campus.CampusPermissionMenu;
import com.stonedt.intelligence.entity.campus.CampusPermissionRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CampusPermissionDao {

    int insertRole(CampusPermissionRole role);

    int updateRole(CampusPermissionRole role);

    int logicalDeleteRole(@Param("roleId") Long roleId, @Param("updateUserId") Long updateUserId);

    CampusPermissionRole selectRoleById(@Param("roleId") Long roleId);

    int countRoleCode(@Param("roleCode") String roleCode, @Param("excludeRoleId") Long excludeRoleId);

    List<CampusPermissionRole> listRoles(@Param("keyword") String keyword,
                                         @Param("roleType") String roleType,
                                         @Param("status") Integer status);

    List<CampusPermissionRole> listRolesByUserId(@Param("userId") Long userId);

    List<CampusPermissionMenu> listMenus(@Param("visible") Integer visible,
                                         @Param("status") Integer status);

    List<CampusPermissionMenu> listMenusByUserId(@Param("userId") Long userId,
                                                 @Param("visible") Integer visible);

    List<CampusPermissionApi> listApis(@Param("keyword") String keyword,
                                       @Param("moduleName") String moduleName,
                                       @Param("status") Integer status);

    List<CampusPermissionApi> listApisByUserId(@Param("userId") Long userId);

    List<Long> listRoleMenuIds(@Param("roleId") Long roleId);

    List<Long> listRoleApiIds(@Param("roleId") Long roleId);

    int deleteRoleMenus(@Param("roleId") Long roleId, @Param("updateUserId") Long updateUserId);

    int deleteRoleApis(@Param("roleId") Long roleId, @Param("updateUserId") Long updateUserId);

    int insertRoleMenu(@Param("relationId") Long relationId,
                       @Param("roleId") Long roleId,
                       @Param("menuId") Long menuId,
                       @Param("operatorUserId") Long operatorUserId);

    int insertRoleApi(@Param("relationId") Long relationId,
                      @Param("roleId") Long roleId,
                      @Param("apiId") Long apiId,
                      @Param("operatorUserId") Long operatorUserId);
}
