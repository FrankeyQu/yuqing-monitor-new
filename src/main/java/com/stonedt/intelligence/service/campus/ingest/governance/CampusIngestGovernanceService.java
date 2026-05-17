package com.stonedt.intelligence.service.campus.ingest.governance;

import com.stonedt.intelligence.dao.campus.CampusIngestTaskDao;
import com.stonedt.intelligence.entity.campus.CampusIngestTask;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;

@Service
public class CampusIngestGovernanceService {

    private static final Long SYSTEM_USER_ID = 0L;
    private static final String ADAPTER_THIRD_PARTY_API = "third_party_api";
    private static final String ADAPTER_BAIDU_SEARCH = "baidu_search";
    private static final String ADAPTER_PUBLIC_WEB_PULL = "public_web_pull";

    private final CampusIngestTaskDao campusIngestTaskDao;

    public CampusIngestGovernanceService(CampusIngestTaskDao campusIngestTaskDao) {
        this.campusIngestTaskDao = campusIngestTaskDao;
    }

    public void ensureQuotaAvailable(CampusIngestTask task, Long operatorUserId) {
        if (!isQuotaLimitedExternalTask(task)) {
            return;
        }
        Date today = today();
        int used = currentUsedCount(task, today, operatorUserId);
        int limit = task.getDailyQuotaLimit();
        if (used >= limit) {
            throw new IllegalStateException("接入任务今日API额度已用尽");
        }
    }

    public void recordQuotaUsage(Long taskId, Integer costUnits) {
        if (taskId == null || costUnits == null || costUnits <= 0) {
            return;
        }
        campusIngestTaskDao.increaseDailyQuotaUsed(taskId, today(), costUnits, SYSTEM_USER_ID);
    }

    private boolean isQuotaLimitedExternalTask(CampusIngestTask task) {
        return task != null
                && isExternalAdapter(task.getAdapterType())
                && task.getDailyQuotaLimit() != null
                && task.getDailyQuotaLimit() > 0;
    }

    private boolean isExternalAdapter(String adapterType) {
        return ADAPTER_THIRD_PARTY_API.equals(adapterType)
                || ADAPTER_BAIDU_SEARCH.equals(adapterType)
                || ADAPTER_PUBLIC_WEB_PULL.equals(adapterType);
    }

    private int currentUsedCount(CampusIngestTask task, Date today, Long operatorUserId) {
        if (!isSameDay(task.getQuotaStatDate(), today)) {
            campusIngestTaskDao.resetDailyQuota(task.getTaskId(), today, operatorUserId);
            task.setDailyQuotaUsed(0);
            task.setQuotaStatDate(today);
            return 0;
        }
        return task.getDailyQuotaUsed() == null ? 0 : Math.max(task.getDailyQuotaUsed(), 0);
    }

    private Date today() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private boolean isSameDay(Date left, Date right) {
        if (left == null || right == null) {
            return false;
        }
        Calendar leftCalendar = Calendar.getInstance();
        leftCalendar.setTime(left);
        Calendar rightCalendar = Calendar.getInstance();
        rightCalendar.setTime(right);
        return leftCalendar.get(Calendar.YEAR) == rightCalendar.get(Calendar.YEAR)
                && leftCalendar.get(Calendar.DAY_OF_YEAR) == rightCalendar.get(Calendar.DAY_OF_YEAR);
    }
}
