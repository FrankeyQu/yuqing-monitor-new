package com.stonedt.intelligence.service.impl.campus;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.stonedt.intelligence.dao.campus.CampusDepartmentDao;
import com.stonedt.intelligence.entity.campus.CampusDepartment;
import com.stonedt.intelligence.service.campus.CampusDepartmentService;
import com.stonedt.intelligence.util.SnowflakeUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CampusDepartmentServiceImpl implements CampusDepartmentService {

    private final CampusDepartmentDao campusDepartmentDao;

    public CampusDepartmentServiceImpl(CampusDepartmentDao campusDepartmentDao) {
        this.campusDepartmentDao = campusDepartmentDao;
    }

    @Override
    public CampusDepartment save(CampusDepartment department, Long operatorUserId) {
        validate(department);
        if (department.getDepartmentId() == null) {
            department.setDepartmentId(SnowflakeUtil.getId());
            department.setCreateUserId(operatorUserId);
            department.setUpdateUserId(operatorUserId);
            setDefaults(department);
            ensureCodeUnique(department);
            campusDepartmentDao.insert(department);
            return department;
        }
        CampusDepartment old = campusDepartmentDao.selectByDepartmentId(department.getDepartmentId());
        if (old == null) {
            throw new IllegalArgumentException("部门不存在");
        }
        department.setUpdateUserId(operatorUserId);
        setDefaults(department);
        ensureCodeUnique(department);
        campusDepartmentDao.update(department);
        return campusDepartmentDao.selectByDepartmentId(department.getDepartmentId());
    }

    @Override
    public void delete(Long departmentId, Long operatorUserId) {
        if (departmentId == null) {
            throw new IllegalArgumentException("部门ID不能为空");
        }
        campusDepartmentDao.logicalDelete(departmentId, operatorUserId);
    }

    @Override
    public CampusDepartment detail(Long departmentId) {
        if (departmentId == null) {
            throw new IllegalArgumentException("部门ID不能为空");
        }
        return campusDepartmentDao.selectByDepartmentId(departmentId);
    }

    @Override
    public PageInfo<CampusDepartment> list(Integer pageNum,
                                           Integer pageSize,
                                           String departmentName,
                                           Long parentId,
                                           Integer status) {
        PageHelper.startPage(defaultPageNum(pageNum), defaultPageSize(pageSize));
        return new PageInfo<>(campusDepartmentDao.list(departmentName, parentId, status));
    }

    @Override
    public List<CampusDepartment> tree() {
        return campusDepartmentDao.list(null, null, 1);
    }

    private void validate(CampusDepartment department) {
        if (department == null) {
            throw new IllegalArgumentException("部门信息不能为空");
        }
        if (StringUtils.isBlank(department.getDepartmentName())) {
            throw new IllegalArgumentException("部门名称不能为空");
        }
    }

    private void setDefaults(CampusDepartment department) {
        if (department.getParentId() == null) {
            department.setParentId(0L);
        }
        if (department.getSortNo() == null) {
            department.setSortNo(0);
        }
        if (department.getStatus() == null) {
            department.setStatus(1);
        }
        department.setDeleted(0);
    }

    private void ensureCodeUnique(CampusDepartment department) {
        if (StringUtils.isBlank(department.getDepartmentCode())) {
            return;
        }
        int count = campusDepartmentDao.countByCode(department.getDepartmentCode(), department.getDepartmentId());
        if (count > 0) {
            throw new IllegalArgumentException("部门编码已存在");
        }
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
