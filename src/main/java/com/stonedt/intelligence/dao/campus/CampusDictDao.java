package com.stonedt.intelligence.dao.campus;

import com.stonedt.intelligence.entity.campus.CampusDictItem;
import com.stonedt.intelligence.entity.campus.CampusDictType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CampusDictDao {

    int insertType(CampusDictType dictType);

    int updateType(CampusDictType dictType);

    int logicalDeleteType(@Param("dictType") String dictType, @Param("updateUserId") Long updateUserId);

    CampusDictType selectType(@Param("dictType") String dictType);

    List<CampusDictType> listTypes(@Param("keyword") String keyword, @Param("status") Integer status);

    int insertItem(CampusDictItem dictItem);

    int updateItem(CampusDictItem dictItem);

    int logicalDeleteItem(@Param("dictType") String dictType,
                          @Param("itemCode") String itemCode,
                          @Param("updateUserId") Long updateUserId);

    CampusDictItem selectItem(@Param("dictType") String dictType, @Param("itemCode") String itemCode);

    List<CampusDictItem> listItems(@Param("dictType") String dictType,
                                   @Param("keyword") String keyword,
                                   @Param("status") Integer status);

    List<CampusDictItem> enabledItems(@Param("dictType") String dictType);
}
