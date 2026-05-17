package com.stonedt.intelligence.service.campus.ingest.governance;

import com.stonedt.intelligence.dao.campus.CampusIngestApiCallLogDao;
import com.stonedt.intelligence.entity.campus.CampusIngestApiCallLog;
import com.stonedt.intelligence.entity.campus.CampusIngestSource;
import com.stonedt.intelligence.entity.campus.CampusIngestTask;
import com.stonedt.intelligence.service.campus.ingest.CampusIngestFetchRequest;
import com.stonedt.intelligence.service.campus.ingest.security.CampusIngestAuditSanitizer;
import com.stonedt.intelligence.service.campus.ingest.tikhub.TikhubEndpointDefinition;
import com.stonedt.intelligence.service.campus.ingest.tikhub.TikhubFetchConfig;
import com.stonedt.intelligence.util.SnowflakeUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@Slf4j
public class CampusIngestApiCallLogger {

    public static final String STATUS_SUCCESS = "success";
    public static final String STATUS_FAILED = "failed";

    private final CampusIngestApiCallLogDao campusIngestApiCallLogDao;
    private final CampusIngestGovernanceService campusIngestGovernanceService;

    public CampusIngestApiCallLogger(CampusIngestApiCallLogDao campusIngestApiCallLogDao,
                                     CampusIngestGovernanceService campusIngestGovernanceService) {
        this.campusIngestApiCallLogDao = campusIngestApiCallLogDao;
        this.campusIngestGovernanceService = campusIngestGovernanceService;
    }

    public void recordTikhubCall(CampusIngestFetchRequest request,
                                 TikhubEndpointDefinition endpointDefinition,
                                 TikhubFetchConfig fetchConfig,
                                 Date requestTime,
                                 Long durationMs,
                                 String callStatus,
                                 Integer httpStatus,
                                 String errorType,
                                 String errorMessage,
                                 Integer costUnits,
                                 boolean consumeQuota) {
        try {
            CampusIngestApiCallLog callLog = new CampusIngestApiCallLog();
            callLog.setCallId(SnowflakeUtil.getId());
            callLog.setRunId(request == null ? null : request.getRunId());
            CampusIngestTask task = request == null ? null : request.getTask();
            CampusIngestSource source = request == null ? null : request.getSource();
            callLog.setTaskId(task == null ? null : task.getTaskId());
            callLog.setSourceId(source == null ? null : source.getSourceId());
            callLog.setProvider(TikhubFetchConfig.PROVIDER);
            callLog.setEndpointKey(endpointDefinition == null ? null : endpointDefinition.getEndpointKey());
            callLog.setCredentialRef(fetchConfig == null ? null : fetchConfig.getCredentialRef());
            callLog.setRequestTime(requestTime);
            callLog.setDurationMs(durationMs);
            callLog.setCallStatus(StringUtils.defaultIfBlank(callStatus, STATUS_FAILED));
            callLog.setHttpStatus(httpStatus);
            callLog.setErrorType(errorType);
            callLog.setErrorMessage(StringUtils.left(CampusIngestAuditSanitizer.sanitize(errorMessage), 2048));
            callLog.setCostUnits(costUnits == null ? 0 : Math.max(costUnits, 0));
            campusIngestApiCallLogDao.insert(callLog);
            if (consumeQuota && callLog.getCostUnits() > 0 && callLog.getTaskId() != null) {
                campusIngestGovernanceService.recordQuotaUsage(callLog.getTaskId(), callLog.getCostUnits());
            }
        } catch (Exception ex) {
            log.warn("第三方API调用日志写入失败", ex);
        }
    }

    public void recordExternalCall(CampusIngestFetchRequest request,
                                   String provider,
                                   String endpointKey,
                                   String credentialRef,
                                   Date requestTime,
                                   Long durationMs,
                                   String callStatus,
                                   Integer httpStatus,
                                   String errorType,
                                   String errorMessage,
                                   Integer costUnits,
                                   boolean consumeQuota) {
        try {
            CampusIngestApiCallLog callLog = new CampusIngestApiCallLog();
            callLog.setCallId(SnowflakeUtil.getId());
            callLog.setRunId(request == null ? null : request.getRunId());
            CampusIngestTask task = request == null ? null : request.getTask();
            CampusIngestSource source = request == null ? null : request.getSource();
            callLog.setTaskId(task == null ? null : task.getTaskId());
            callLog.setSourceId(source == null ? null : source.getSourceId());
            callLog.setProvider(StringUtils.defaultIfBlank(provider, "external"));
            callLog.setEndpointKey(endpointKey);
            callLog.setCredentialRef(credentialRef);
            callLog.setRequestTime(requestTime);
            callLog.setDurationMs(durationMs);
            callLog.setCallStatus(StringUtils.defaultIfBlank(callStatus, STATUS_FAILED));
            callLog.setHttpStatus(httpStatus);
            callLog.setErrorType(errorType);
            callLog.setErrorMessage(StringUtils.left(CampusIngestAuditSanitizer.sanitize(errorMessage), 2048));
            callLog.setCostUnits(costUnits == null ? 0 : Math.max(costUnits, 0));
            campusIngestApiCallLogDao.insert(callLog);
            if (consumeQuota && callLog.getCostUnits() > 0 && callLog.getTaskId() != null) {
                campusIngestGovernanceService.recordQuotaUsage(callLog.getTaskId(), callLog.getCostUnits());
            }
        } catch (Exception ex) {
            log.warn("外部API调用日志写入失败", ex);
        }
    }
}
