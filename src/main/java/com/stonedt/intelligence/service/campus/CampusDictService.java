package com.stonedt.intelligence.service.campus;

import com.github.pagehelper.PageInfo;
import com.stonedt.intelligence.entity.campus.CampusDictItem;
import com.stonedt.intelligence.entity.campus.CampusDictType;

import java.util.List;

public interface CampusDictService {

    CampusDictType saveType(CampusDictType dictType, Long operatorUserId);

    void deleteType(String dictType, Long operatorUserId);

    PageInfo<CampusDictType> listTypes(Integer pageNum, Integer pageSize, String keyword, Integer status);

    CampusDictItem saveItem(CampusDictItem dictItem, Long operatorUserId);

    void deleteItem(String dictType, String itemCode, Long operatorUserId);

    PageInfo<CampusDictItem> listItems(Integer pageNum, Integer pageSize, String dictType, String keyword, Integer status);

    List<CampusDictItem> enabledItems(String dictType);
}
