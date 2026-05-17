package com.stonedt.intelligence.service.campus;

import com.github.pagehelper.PageInfo;
import com.stonedt.intelligence.entity.campus.CampusAuditLog;

import javax.servlet.http.HttpServletRequest;

public interface CampusAuditLogService {

    void record(HttpServletRequest request,
                String moduleName,
                String operationType,
                String objectType,
                String objectId,
                String requestParams,
                boolean success,
                String failureReason);

    PageInfo<CampusAuditLog> list(Integer pageNum,
                                  Integer pageSize,
                                  String moduleName,
                                  String operationType,
                                  String objectType,
                                  String objectId,
                                  String operatorName);
}
