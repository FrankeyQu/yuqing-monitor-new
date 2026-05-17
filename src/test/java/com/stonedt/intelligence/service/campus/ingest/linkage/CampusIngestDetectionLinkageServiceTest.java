package com.stonedt.intelligence.service.campus.ingest.linkage;

import com.github.pagehelper.PageInfo;
import com.stonedt.intelligence.dao.campus.CampusDetectionTaskDao;
import com.stonedt.intelligence.entity.campus.CampusDetectionHit;
import com.stonedt.intelligence.entity.campus.CampusDetectionRule;
import com.stonedt.intelligence.entity.campus.CampusDetectionRunLog;
import com.stonedt.intelligence.entity.campus.CampusDetectionTask;
import com.stonedt.intelligence.entity.campus.CampusDetectionTopic;
import com.stonedt.intelligence.entity.campus.CampusIngestRunLog;
import com.stonedt.intelligence.entity.campus.CampusIngestTask;
import com.stonedt.intelligence.service.campus.CampusDetectionService;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

public class CampusIngestDetectionLinkageServiceTest {

    @Test
    public void explicitDetectionTaskIdsTriggerLinkage() {
        StubDetectionService detectionService = new StubDetectionService();
        CampusIngestDetectionLinkageService service =
                new CampusIngestDetectionLinkageService(detectionService, new StubDetectionTaskDao("active", "ingest_record"));

        CampusIngestDetectionLinkageResult result = service.linkAfterIngestRun(ingestTask("1001"), runLog(), 1, 9L);

        Assert.assertEquals(1, detectionService.runCount);
        Assert.assertEquals(1, result.getTriggerCount());
        Assert.assertEquals(2, result.getHitCount());
        Assert.assertEquals(1, result.getAlertCount());
        Assert.assertNull(result.getErrorMessage());
    }

    @Test
    public void blankDetectionTaskIdsDoNotTrigger() {
        StubDetectionService detectionService = new StubDetectionService();
        CampusIngestDetectionLinkageService service =
                new CampusIngestDetectionLinkageService(detectionService, new StubDetectionTaskDao("active", "ingest_record"));

        CampusIngestDetectionLinkageResult result = service.linkAfterIngestRun(ingestTask(" "), runLog(), 1, 9L);

        Assert.assertEquals(0, detectionService.runCount);
        Assert.assertFalse(result.hasResult());
    }

    @Test
    public void inactiveDetectionTaskIsReportedButDoesNotRun() {
        StubDetectionService detectionService = new StubDetectionService();
        CampusIngestDetectionLinkageService service =
                new CampusIngestDetectionLinkageService(detectionService, new StubDetectionTaskDao("paused", "ingest_record"));

        CampusIngestDetectionLinkageResult result = service.linkAfterIngestRun(ingestTask("1001"), runLog(), 1, 9L);

        Assert.assertEquals(0, detectionService.runCount);
        Assert.assertTrue(result.getErrorMessage().contains("不可用于接入联动"));
    }

    private CampusIngestTask ingestTask(String detectionTaskIds) {
        CampusIngestTask task = new CampusIngestTask();
        task.setAutoDetectEnabled(1);
        task.setDetectionTaskIds(detectionTaskIds);
        return task;
    }

    private CampusIngestRunLog runLog() {
        CampusIngestRunLog runLog = new CampusIngestRunLog();
        runLog.setRunId(2001L);
        return runLog;
    }

    private static class StubDetectionTaskDao implements CampusDetectionTaskDao {

        private final String taskStatus;
        private final String objectTypes;

        private StubDetectionTaskDao(String taskStatus, String objectTypes) {
            this.taskStatus = taskStatus;
            this.objectTypes = objectTypes;
        }

        @Override
        public int insert(CampusDetectionTask task) {
            return 0;
        }

        @Override
        public int update(CampusDetectionTask task) {
            return 0;
        }

        @Override
        public int updateStatus(Long detectionTaskId, String taskStatus, Long updateUserId) {
            return 0;
        }

        @Override
        public int updateLastRunTime(Long detectionTaskId, Long updateUserId) {
            return 0;
        }

        @Override
        public int logicalDelete(Long detectionTaskId, Long updateUserId) {
            return 0;
        }

        @Override
        public CampusDetectionTask selectByTaskId(Long detectionTaskId) {
            CampusDetectionTask task = new CampusDetectionTask();
            task.setDetectionTaskId(detectionTaskId);
            task.setTaskStatus(taskStatus);
            task.setObjectTypes(objectTypes);
            return task;
        }

        @Override
        public List<CampusDetectionTask> list(String keyword, Long topicId, String taskStatus) {
            return Collections.emptyList();
        }

        @Override
        public List<CampusDetectionTask> listActiveTasks() {
            return Collections.emptyList();
        }
    }

    private static class StubDetectionService implements CampusDetectionService {

        private int runCount;

        @Override
        public CampusDetectionRunLog runIngestRecordTask(Long detectionTaskId, Long ingestRunId, Long operatorUserId) {
            runCount++;
            CampusDetectionRunLog runLog = new CampusDetectionRunLog();
            runLog.setHitCount(2);
            runLog.setAlertCount(1);
            return runLog;
        }

        @Override
        public CampusDetectionTopic saveTopic(CampusDetectionTopic topic, Long operatorUserId) {
            return null;
        }

        @Override
        public void deleteTopic(Long topicId, Long operatorUserId) {
        }

        @Override
        public PageInfo<CampusDetectionTopic> listTopics(Integer pageNum, Integer pageSize, String keyword, String topicCategory, Integer enabled) {
            return null;
        }

        @Override
        public CampusDetectionRule saveRule(CampusDetectionRule rule, Long operatorUserId) {
            return null;
        }

        @Override
        public void deleteRule(Long ruleId, Long operatorUserId) {
        }

        @Override
        public PageInfo<CampusDetectionRule> listRules(Integer pageNum, Integer pageSize, Long topicId, String ruleType, Integer enabled) {
            return null;
        }

        @Override
        public CampusDetectionTask saveTask(CampusDetectionTask task, Long operatorUserId) {
            return null;
        }

        @Override
        public CampusDetectionTask updateTaskStatus(Long detectionTaskId, String taskStatus, Long operatorUserId) {
            return null;
        }

        @Override
        public void deleteTask(Long detectionTaskId, Long operatorUserId) {
        }

        @Override
        public PageInfo<CampusDetectionTask> listTasks(Integer pageNum, Integer pageSize, String keyword, Long topicId, String taskStatus) {
            return null;
        }

        @Override
        public CampusDetectionRunLog runTask(Long detectionTaskId, Long operatorUserId) {
            return null;
        }

        @Override
        public CampusDetectionHit alertHit(Long hitId, Long operatorUserId) {
            return null;
        }

        @Override
        public CampusDetectionHit ignoreHit(Long hitId, Long operatorUserId) {
            return null;
        }

        @Override
        public PageInfo<CampusDetectionHit> listHits(Integer pageNum, Integer pageSize, Long detectionTaskId, Long topicId, String objectType, String hitStatus, String riskLevel, String keyword) {
            return null;
        }

        @Override
        public PageInfo<CampusDetectionRunLog> listRunLogs(Integer pageNum, Integer pageSize, Long detectionTaskId) {
            return null;
        }
    }
}
