package com.stonedt.intelligence.service.campus.ingest.governance;

import com.stonedt.intelligence.dao.campus.CampusIngestTaskDao;
import com.stonedt.intelligence.entity.campus.CampusIngestTask;
import org.junit.Assert;
import org.junit.Test;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class CampusIngestGovernanceServiceTest {

    @Test
    public void zeroDailyQuotaMeansUnlimited() {
        StubTaskDao taskDao = new StubTaskDao();
        CampusIngestGovernanceService service = new CampusIngestGovernanceService(taskDao);

        service.ensureQuotaAvailable(task(0, 99, today()), 7L);

        Assert.assertEquals(0, taskDao.resetCount);
    }

    @Test(expected = IllegalStateException.class)
    public void sameDayUsedQuotaBlocksExternalCall() {
        CampusIngestGovernanceService service = new CampusIngestGovernanceService(new StubTaskDao());

        service.ensureQuotaAvailable(task(1, 1, today()), 7L);
    }

    @Test
    public void expiredQuotaDateResetsBeforeCheck() {
        StubTaskDao taskDao = new StubTaskDao();
        CampusIngestGovernanceService service = new CampusIngestGovernanceService(taskDao);

        service.ensureQuotaAvailable(task(1, 5, yesterday()), 7L);

        Assert.assertEquals(1, taskDao.resetCount);
        Assert.assertEquals(7L, taskDao.updateUserId.longValue());
    }

    @Test
    public void recordQuotaUsageIncrementsPositiveCostUnits() {
        StubTaskDao taskDao = new StubTaskDao();
        CampusIngestGovernanceService service = new CampusIngestGovernanceService(taskDao);

        service.recordQuotaUsage(1001L, 2);

        Assert.assertEquals(1001L, taskDao.incrementTaskId.longValue());
        Assert.assertEquals(2, taskDao.incrementCostUnits.intValue());
    }

    private CampusIngestTask task(int limit, int used, Date statDate) {
        CampusIngestTask task = new CampusIngestTask();
        task.setTaskId(1001L);
        task.setAdapterType("third_party_api");
        task.setDailyQuotaLimit(limit);
        task.setDailyQuotaUsed(used);
        task.setQuotaStatDate(statDate);
        return task;
    }

    private Date today() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private Date yesterday() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today());
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        return calendar.getTime();
    }

    private static class StubTaskDao implements CampusIngestTaskDao {

        private int resetCount;
        private Long updateUserId;
        private Long incrementTaskId;
        private Integer incrementCostUnits;

        @Override
        public int insert(CampusIngestTask task) {
            return 0;
        }

        @Override
        public int update(CampusIngestTask task) {
            return 0;
        }

        @Override
        public int updateStatus(Long taskId, String taskStatus, Long updateUserId) {
            return 0;
        }

        @Override
        public int updateLastRunTime(Long taskId, Long updateUserId) {
            return 0;
        }

        @Override
        public int updateNextRunTime(Long taskId, Date nextRunTime, Long updateUserId) {
            return 0;
        }

        @Override
        public List<CampusIngestTask> listDueTasks(Date now, Integer limit) {
            return Collections.emptyList();
        }

        @Override
        public int acquireScheduleLock(Long taskId, Date now, Date lockUntil) {
            return 0;
        }

        @Override
        public int acquireExecutionLock(Long taskId, Date now, Date lockUntil, Long updateUserId) {
            return 0;
        }

        @Override
        public int releaseScheduleLock(Long taskId) {
            return 0;
        }

        @Override
        public int releaseScheduleLockBefore(Long taskId, Date lockUntil) {
            return 0;
        }

        @Override
        public int markScheduleSuccess(Long taskId, Date nextRunTime, Date lockUntil, Long updateUserId) {
            return 0;
        }

        @Override
        public int markScheduleFailure(Long taskId,
                                       Date nextRunTime,
                                       String errorType,
                                       Integer currentRetryCount,
                                       String taskStatus,
                                       Date lockUntil,
                                       Long updateUserId) {
            return 0;
        }

        @Override
        public int resetDailyQuota(Long taskId, Date quotaStatDate, Long updateUserId) {
            this.resetCount++;
            this.updateUserId = updateUserId;
            return 1;
        }

        @Override
        public int increaseDailyQuotaUsed(Long taskId, Date quotaStatDate, Integer costUnits, Long updateUserId) {
            this.incrementTaskId = taskId;
            this.incrementCostUnits = costUnits;
            return 1;
        }

        @Override
        public int logicalDelete(Long taskId, Long updateUserId) {
            return 0;
        }

        @Override
        public CampusIngestTask selectByTaskId(Long taskId) {
            return null;
        }

        @Override
        public List<CampusIngestTask> list(String keyword, Long sourceId, String targetType, String taskStatus) {
            return Collections.emptyList();
        }
    }
}
