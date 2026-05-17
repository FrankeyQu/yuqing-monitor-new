package com.stonedt.intelligence.service.impl.campus;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.stonedt.intelligence.dao.campus.CampusAccountContentDao;
import com.stonedt.intelligence.dao.campus.CampusAccountDao;
import com.stonedt.intelligence.dao.campus.CampusAccountTaskDao;
import com.stonedt.intelligence.entity.campus.CampusAccount;
import com.stonedt.intelligence.entity.campus.CampusAccountContent;
import com.stonedt.intelligence.entity.campus.CampusAccountTask;
import com.stonedt.intelligence.service.campus.CampusAccountService;
import com.stonedt.intelligence.service.campus.support.CampusRiskLevel;
import com.stonedt.intelligence.util.SnowflakeUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class CampusAccountServiceImpl implements CampusAccountService {

    private static final String AUDIT_PENDING = "pending";
    private static final String AUDIT_APPROVED = "approved";
    private static final String AUDIT_REJECTED = "rejected";
    private static final String STATUS_PENDING = "pending";
    private static final String STATUS_ACTIVE = "active";
    private static final String STATUS_REJECTED = "rejected";
    private static final String TASK_ACTIVE = "active";
    private static final String RISK_NORMAL = CampusRiskLevel.normalCode();
    private static final String FOCUS_NORMAL = "normal";

    private final CampusAccountDao campusAccountDao;
    private final CampusAccountTaskDao campusAccountTaskDao;
    private final CampusAccountContentDao campusAccountContentDao;

    public CampusAccountServiceImpl(CampusAccountDao campusAccountDao,
                                    CampusAccountTaskDao campusAccountTaskDao,
                                    CampusAccountContentDao campusAccountContentDao) {
        this.campusAccountDao = campusAccountDao;
        this.campusAccountTaskDao = campusAccountTaskDao;
        this.campusAccountContentDao = campusAccountContentDao;
    }

    @Override
    public CampusAccount save(CampusAccount account, Long operatorUserId) {
        validateAccount(account);
        if (account.getAccountId() == null) {
            account.setAccountId(SnowflakeUtil.getId());
            account.setCreateUserId(operatorUserId);
            account.setUpdateUserId(operatorUserId);
            setAccountDefaults(account);
            ensureUniquePlatformAccount(account);
            campusAccountDao.insert(account);
            createInitialTask(account, operatorUserId);
            return campusAccountDao.selectByAccountId(account.getAccountId());
        }
        requireAccount(account.getAccountId());
        account.setUpdateUserId(operatorUserId);
        setAccountUpdateDefaults(account);
        ensureUniquePlatformAccount(account);
        campusAccountDao.update(account);
        return campusAccountDao.selectByAccountId(account.getAccountId());
    }

    @Override
    public CampusAccount detail(Long accountId) {
        return requireAccount(accountId);
    }

    @Override
    public PageInfo<CampusAccount> list(Integer pageNum,
                                        Integer pageSize,
                                        String keyword,
                                        String platform,
                                        String focusLevel,
                                        String auditStatus,
                                        String accountStatus,
                                        Date focusEndBefore) {
        PageHelper.startPage(defaultPageNum(pageNum), defaultPageSize(pageSize));
        return new PageInfo<>(campusAccountDao.list(keyword, platform, focusLevel, auditStatus,
                accountStatus, focusEndBefore));
    }

    @Override
    public CampusAccount audit(Long accountId,
                               String auditStatus,
                               String auditOpinion,
                               Long operatorUserId) {
        requireAccount(accountId);
        if (!AUDIT_APPROVED.equals(auditStatus) && !AUDIT_REJECTED.equals(auditStatus)) {
            throw new IllegalArgumentException("审核状态只能为 approved 或 rejected");
        }
        String accountStatus = AUDIT_APPROVED.equals(auditStatus) ? STATUS_ACTIVE : STATUS_REJECTED;
        campusAccountDao.audit(accountId, auditStatus, auditOpinion, accountStatus, operatorUserId, operatorUserId);
        return campusAccountDao.selectByAccountId(accountId);
    }

    @Override
    public CampusAccount updateStatus(Long accountId, String accountStatus, Long operatorUserId) {
        requireAccount(accountId);
        if (StringUtils.isBlank(accountStatus)) {
            throw new IllegalArgumentException("账号状态不能为空");
        }
        campusAccountDao.updateStatus(accountId, accountStatus, operatorUserId);
        return campusAccountDao.selectByAccountId(accountId);
    }

    @Override
    public void delete(Long accountId, Long operatorUserId) {
        requireAccount(accountId);
        campusAccountDao.logicalDelete(accountId, operatorUserId);
    }

    @Override
    public CampusAccountTask addTask(CampusAccountTask task, Long operatorUserId) {
        validateTask(task);
        requireAccount(task.getAccountId());
        task.setTaskId(SnowflakeUtil.getId());
        task.setTaskStatus(StringUtils.defaultIfBlank(task.getTaskStatus(), TASK_ACTIVE));
        task.setDeleted(0);
        task.setCreateUserId(operatorUserId);
        task.setUpdateUserId(operatorUserId);
        campusAccountTaskDao.insert(task);
        return campusAccountTaskDao.selectByTaskId(task.getTaskId());
    }

    @Override
    public List<CampusAccountTask> listTasks(Long accountId) {
        requireAccount(accountId);
        return campusAccountTaskDao.listByAccountId(accountId);
    }

    @Override
    public CampusAccountContent addContent(CampusAccountContent content, Long operatorUserId) {
        validateContent(content);
        CampusAccount account = requireAccount(content.getAccountId());
        content.setContentId(SnowflakeUtil.getId());
        if (StringUtils.isBlank(content.getPlatform())) {
            content.setPlatform(account.getPlatform());
        }
        if (content.getCaptureTime() == null) {
            content.setCaptureTime(new Date());
        }
        if (StringUtils.isBlank(content.getRiskLevel())) {
            content.setRiskLevel(RISK_NORMAL);
        } else {
            content.setRiskLevel(CampusRiskLevel.requireValid(content.getRiskLevel()));
        }
        content.setDeleted(0);
        content.setCreateUserId(operatorUserId);
        content.setUpdateUserId(operatorUserId);
        campusAccountContentDao.insert(content);
        return campusAccountContentDao.selectByContentId(content.getContentId());
    }

    @Override
    public PageInfo<CampusAccountContent> listContents(Integer pageNum,
                                                       Integer pageSize,
                                                       Long accountId,
                                                       Long taskId,
                                                       String riskLevel,
                                                       String keyword) {
        PageHelper.startPage(defaultPageNum(pageNum), defaultPageSize(pageSize));
        return new PageInfo<>(campusAccountContentDao.list(accountId, taskId,
                CampusRiskLevel.normalizeForQuery(riskLevel), keyword));
    }

    private void validateAccount(CampusAccount account) {
        if (account == null) {
            throw new IllegalArgumentException("账号信息不能为空");
        }
        if (StringUtils.isBlank(account.getPlatform())) {
            throw new IllegalArgumentException("平台不能为空");
        }
        if (StringUtils.isBlank(account.getAccountName())) {
            throw new IllegalArgumentException("账号名称不能为空");
        }
        if (StringUtils.isBlank(account.getSourceBasis())) {
            throw new IllegalArgumentException("来源依据不能为空");
        }
        if (StringUtils.isBlank(account.getTaskNo())) {
            throw new IllegalArgumentException("任务编号不能为空");
        }
        if (StringUtils.isBlank(account.getAuthorizationScope())) {
            throw new IllegalArgumentException("授权范围不能为空");
        }
        validateFocusTime(account.getFocusStartTime(), account.getFocusEndTime());
    }

    private void validateTask(CampusAccountTask task) {
        if (task == null) {
            throw new IllegalArgumentException("关注任务不能为空");
        }
        if (task.getAccountId() == null) {
            throw new IllegalArgumentException("账号ID不能为空");
        }
        if (StringUtils.isBlank(task.getTaskNo())) {
            throw new IllegalArgumentException("任务编号不能为空");
        }
        if (StringUtils.isBlank(task.getSourceBasis())) {
            throw new IllegalArgumentException("来源依据不能为空");
        }
        if (StringUtils.isBlank(task.getAuthorizationScope())) {
            throw new IllegalArgumentException("授权范围不能为空");
        }
        validateFocusTime(task.getFocusStartTime(), task.getFocusEndTime());
    }

    private void validateContent(CampusAccountContent content) {
        if (content == null) {
            throw new IllegalArgumentException("账号动态不能为空");
        }
        if (content.getAccountId() == null) {
            throw new IllegalArgumentException("账号ID不能为空");
        }
        if (StringUtils.isBlank(content.getContentText()) && StringUtils.isBlank(content.getContentTitle())) {
            throw new IllegalArgumentException("公开动态标题和内容不能同时为空");
        }
    }

    private void validateFocusTime(Date startTime, Date endTime) {
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("关注开始时间和结束时间不能为空");
        }
        if (!endTime.after(startTime)) {
            throw new IllegalArgumentException("关注结束时间必须晚于开始时间");
        }
    }

    private CampusAccount requireAccount(Long accountId) {
        if (accountId == null) {
            throw new IllegalArgumentException("账号ID不能为空");
        }
        CampusAccount account = campusAccountDao.selectByAccountId(accountId);
        if (account == null) {
            throw new IllegalArgumentException("账号不存在");
        }
        return account;
    }

    private void setAccountDefaults(CampusAccount account) {
        if (StringUtils.isBlank(account.getFocusLevel())) {
            account.setFocusLevel(FOCUS_NORMAL);
        }
        account.setAuditStatus(AUDIT_PENDING);
        account.setAccountStatus(STATUS_PENDING);
        account.setDeleted(0);
    }

    private void setAccountUpdateDefaults(CampusAccount account) {
        if (StringUtils.isBlank(account.getFocusLevel())) {
            account.setFocusLevel(null);
        }
    }

    private void ensureUniquePlatformAccount(CampusAccount account) {
        if (StringUtils.isBlank(account.getAccountUid())) {
            return;
        }
        int count = campusAccountDao.countPlatformAccount(account.getPlatform(), account.getAccountUid(), account.getAccountId());
        if (count > 0) {
            throw new IllegalArgumentException("该平台账号已存在");
        }
    }

    private void createInitialTask(CampusAccount account, Long operatorUserId) {
        CampusAccountTask task = new CampusAccountTask();
        task.setTaskId(SnowflakeUtil.getId());
        task.setAccountId(account.getAccountId());
        task.setTaskNo(account.getTaskNo());
        task.setTaskName("账号初始关注任务");
        task.setSourceBasis(account.getSourceBasis());
        task.setAuthorizationScope(account.getAuthorizationScope());
        task.setFocusStartTime(account.getFocusStartTime());
        task.setFocusEndTime(account.getFocusEndTime());
        task.setTaskStatus(TASK_ACTIVE);
        task.setResponsibleDepartmentId(account.getResponsibleDepartmentId());
        task.setResponsibleUserId(account.getResponsibleUserId());
        task.setDeleted(0);
        task.setCreateUserId(operatorUserId);
        task.setUpdateUserId(operatorUserId);
        campusAccountTaskDao.insert(task);
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
