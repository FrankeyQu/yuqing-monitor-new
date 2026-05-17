package com.stonedt.intelligence.service.impl.campus;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.stonedt.intelligence.dao.campus.CampusDictDao;
import com.stonedt.intelligence.entity.campus.CampusDictItem;
import com.stonedt.intelligence.entity.campus.CampusDictType;
import com.stonedt.intelligence.service.campus.CampusDictService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CampusDictServiceImpl implements CampusDictService {

    private final CampusDictDao campusDictDao;

    public CampusDictServiceImpl(CampusDictDao campusDictDao) {
        this.campusDictDao = campusDictDao;
    }

    @Override
    public CampusDictType saveType(CampusDictType dictType, Long operatorUserId) {
        validateType(dictType);
        CampusDictType old = campusDictDao.selectType(dictType.getDictType());
        setTypeDefaults(dictType, operatorUserId);
        if (old == null) {
            campusDictDao.insertType(dictType);
        } else {
            campusDictDao.updateType(dictType);
        }
        return campusDictDao.selectType(dictType.getDictType());
    }

    @Override
    public void deleteType(String dictType, Long operatorUserId) {
        if (StringUtils.isBlank(dictType)) {
            throw new IllegalArgumentException("字典类型不能为空");
        }
        campusDictDao.logicalDeleteType(dictType, operatorUserId);
    }

    @Override
    public PageInfo<CampusDictType> listTypes(Integer pageNum, Integer pageSize, String keyword, Integer status) {
        PageHelper.startPage(defaultPageNum(pageNum), defaultPageSize(pageSize));
        return new PageInfo<>(campusDictDao.listTypes(keyword, status));
    }

    @Override
    public CampusDictItem saveItem(CampusDictItem dictItem, Long operatorUserId) {
        validateItem(dictItem);
        if (campusDictDao.selectType(dictItem.getDictType()) == null) {
            throw new IllegalArgumentException("字典类型不存在");
        }
        CampusDictItem old = campusDictDao.selectItem(dictItem.getDictType(), dictItem.getItemCode());
        setItemDefaults(dictItem, operatorUserId);
        if (old == null) {
            campusDictDao.insertItem(dictItem);
        } else {
            campusDictDao.updateItem(dictItem);
        }
        return campusDictDao.selectItem(dictItem.getDictType(), dictItem.getItemCode());
    }

    @Override
    public void deleteItem(String dictType, String itemCode, Long operatorUserId) {
        if (StringUtils.isBlank(dictType) || StringUtils.isBlank(itemCode)) {
            throw new IllegalArgumentException("字典类型和字典项编码不能为空");
        }
        campusDictDao.logicalDeleteItem(dictType, itemCode, operatorUserId);
    }

    @Override
    public PageInfo<CampusDictItem> listItems(Integer pageNum, Integer pageSize, String dictType, String keyword, Integer status) {
        PageHelper.startPage(defaultPageNum(pageNum), defaultPageSize(pageSize));
        return new PageInfo<>(campusDictDao.listItems(dictType, keyword, status));
    }

    @Override
    public List<CampusDictItem> enabledItems(String dictType) {
        if (StringUtils.isBlank(dictType)) {
            throw new IllegalArgumentException("字典类型不能为空");
        }
        return campusDictDao.listItems(dictType, null, 1);
    }

    private void validateType(CampusDictType dictType) {
        if (dictType == null) {
            throw new IllegalArgumentException("字典类型信息不能为空");
        }
        if (StringUtils.isBlank(dictType.getDictType())) {
            throw new IllegalArgumentException("字典类型不能为空");
        }
        if (StringUtils.isBlank(dictType.getDictName())) {
            throw new IllegalArgumentException("字典名称不能为空");
        }
    }

    private void validateItem(CampusDictItem dictItem) {
        if (dictItem == null) {
            throw new IllegalArgumentException("字典项信息不能为空");
        }
        if (StringUtils.isBlank(dictItem.getDictType())) {
            throw new IllegalArgumentException("字典类型不能为空");
        }
        if (StringUtils.isBlank(dictItem.getItemCode())) {
            throw new IllegalArgumentException("字典项编码不能为空");
        }
        if (StringUtils.isBlank(dictItem.getItemName())) {
            throw new IllegalArgumentException("字典项名称不能为空");
        }
    }

    private void setTypeDefaults(CampusDictType dictType, Long operatorUserId) {
        if (dictType.getSortNo() == null) {
            dictType.setSortNo(0);
        }
        if (dictType.getStatus() == null) {
            dictType.setStatus(1);
        }
        dictType.setDeleted(0);
        dictType.setCreateUserId(operatorUserId);
        dictType.setUpdateUserId(operatorUserId);
    }

    private void setItemDefaults(CampusDictItem dictItem, Long operatorUserId) {
        if (dictItem.getSortNo() == null) {
            dictItem.setSortNo(0);
        }
        if (dictItem.getStatus() == null) {
            dictItem.setStatus(1);
        }
        if (StringUtils.isBlank(dictItem.getItemValue())) {
            dictItem.setItemValue(dictItem.getItemCode());
        }
        dictItem.setDeleted(0);
        dictItem.setCreateUserId(operatorUserId);
        dictItem.setUpdateUserId(operatorUserId);
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
