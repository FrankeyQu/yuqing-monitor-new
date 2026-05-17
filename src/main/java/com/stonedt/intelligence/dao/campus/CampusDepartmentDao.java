package com.stonedt.intelligence.dao.campus;

import com.stonedt.intelligence.entity.campus.CampusDepartment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CampusDepartmentDao {

    int insert(CampusDepartment department);

    int update(CampusDepartment department);

    int logicalDelete(@Param("departmentId") Long departmentId, @Param("updateUserId") Long updateUserId);

    CampusDepartment selectByDepartmentId(@Param("departmentId") Long departmentId);

    List<CampusDepartment> list(@Param("departmentName") String departmentName,
                                @Param("parentId") Long parentId,
                                @Param("status") Integer status);

    int countByCode(@Param("departmentCode") String departmentCode,
                    @Param("excludeDepartmentId") Long excludeDepartmentId);
}
