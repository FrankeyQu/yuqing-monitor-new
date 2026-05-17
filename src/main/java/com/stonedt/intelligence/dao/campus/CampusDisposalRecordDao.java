package com.stonedt.intelligence.dao.campus;

import com.stonedt.intelligence.entity.campus.CampusDisposalRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CampusDisposalRecordDao {

    int insert(CampusDisposalRecord record);

    List<CampusDisposalRecord> listByTaskId(@Param("disposalTaskId") Long disposalTaskId);
}
