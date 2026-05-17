package com.stonedt.intelligence.service.campus;

import com.github.pagehelper.PageInfo;
import com.stonedt.intelligence.entity.campus.CampusDepartment;

import java.util.List;

public interface CampusDepartmentService {

    CampusDepartment save(CampusDepartment department, Long operatorUserId);

    void delete(Long departmentId, Long operatorUserId);

    CampusDepartment detail(Long departmentId);

    PageInfo<CampusDepartment> list(Integer pageNum,
                                    Integer pageSize,
                                    String departmentName,
                                    Long parentId,
                                    Integer status);

    List<CampusDepartment> tree();
}
