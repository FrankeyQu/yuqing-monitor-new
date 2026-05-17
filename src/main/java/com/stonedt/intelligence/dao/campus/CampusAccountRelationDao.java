package com.stonedt.intelligence.dao.campus;

import com.stonedt.intelligence.entity.campus.CampusAccountRelation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CampusAccountRelationDao {

    int insert(CampusAccountRelation relation);

    List<CampusAccountRelation> listByAccountId(@Param("accountId") Long accountId);
}
