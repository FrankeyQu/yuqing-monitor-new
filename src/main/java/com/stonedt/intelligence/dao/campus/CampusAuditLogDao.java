package com.stonedt.intelligence.dao.campus;

import com.stonedt.intelligence.entity.campus.CampusAuditLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CampusAuditLogDao {

    int insert(CampusAuditLog auditLog);

    List<CampusAuditLog> list(@Param("moduleName") String moduleName,
                              @Param("operationType") String operationType,
                              @Param("objectType") String objectType,
                              @Param("objectId") String objectId,
                              @Param("operatorName") String operatorName);
}
