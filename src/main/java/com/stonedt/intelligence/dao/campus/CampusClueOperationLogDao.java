package com.stonedt.intelligence.dao.campus;

import com.stonedt.intelligence.entity.campus.CampusClueOperationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CampusClueOperationLogDao {

    int insert(CampusClueOperationLog operationLog);

    List<CampusClueOperationLog> listByClueId(@Param("clueId") Long clueId);
}
