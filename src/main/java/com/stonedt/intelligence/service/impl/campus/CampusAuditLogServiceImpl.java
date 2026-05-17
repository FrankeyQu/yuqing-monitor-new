package com.stonedt.intelligence.service.impl.campus;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.stonedt.intelligence.dao.campus.CampusAuditLogDao;
import com.stonedt.intelligence.entity.User;
import com.stonedt.intelligence.entity.campus.CampusAuditLog;
import com.stonedt.intelligence.service.campus.CampusAuditLogService;
import com.stonedt.intelligence.service.campus.ingest.security.CampusIngestAuditSanitizer;
import com.stonedt.intelligence.util.SnowflakeUtil;
import com.stonedt.intelligence.util.UserUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Service
@Slf4j
public class CampusAuditLogServiceImpl implements CampusAuditLogService {

    private final CampusAuditLogDao campusAuditLogDao;
    private final UserUtil userUtil;

    public CampusAuditLogServiceImpl(CampusAuditLogDao campusAuditLogDao, UserUtil userUtil) {
        this.campusAuditLogDao = campusAuditLogDao;
        this.userUtil = userUtil;
    }

    @Override
    public void record(HttpServletRequest request,
                       String moduleName,
                       String operationType,
                       String objectType,
                       String objectId,
                       String requestParams,
                       boolean success,
                       String failureReason) {
        CampusAuditLog auditLog = new CampusAuditLog();
        auditLog.setAuditId(SnowflakeUtil.getId());
        auditLog.setModuleName(moduleName);
        auditLog.setOperationType(operationType);
        auditLog.setObjectType(objectType);
        auditLog.setObjectId(objectId);
        auditLog.setRequestParams(limit(CampusIngestAuditSanitizer.sanitize(requestParams), 4000));
        auditLog.setOperationResult(success ? 1 : 0);
        auditLog.setFailureReason(limit(CampusIngestAuditSanitizer.sanitize(failureReason), 512));
        if (request != null) {
            auditLog.setRequestMethod(request.getMethod());
            auditLog.setRequestUri(request.getRequestURI());
            auditLog.setRequestIp(getIpAddr(request));
            try {
                User user = userUtil.getuser(request);
                auditLog.setOperatorUserId(user.getUser_id());
                auditLog.setOperatorName(user.getUsername());
                if (user.getOrganization_id() != null && user.getOrganization_id().matches("\\d+")) {
                    auditLog.setOperatorDepartmentId(Long.valueOf(user.getOrganization_id()));
                }
            } catch (Exception ignored) {
                // Audit logging must not interrupt the business operation.
            }
        }
        try {
            campusAuditLogDao.insert(auditLog);
        } catch (Exception e) {
            log.warn("校园业务审计日志写入失败", e);
        }
    }

    @Override
    public PageInfo<CampusAuditLog> list(Integer pageNum,
                                         Integer pageSize,
                                         String moduleName,
                                         String operationType,
                                         String objectType,
                                         String objectId,
                                         String operatorName) {
        PageHelper.startPage(defaultPageNum(pageNum), defaultPageSize(pageSize));
        return new PageInfo<>(campusAuditLogDao.list(moduleName, operationType, objectType, objectId, operatorName));
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

    private String limit(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private String getIpAddr(HttpServletRequest request) {
        String ipAddress = request.getHeader("x-forwarded-for");
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        if (ipAddress != null && ipAddress.indexOf(",") > 0) {
            ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
        }
        return ipAddress;
    }
}
