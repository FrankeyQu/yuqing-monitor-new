package com.stonedt.intelligence.service.impl.campus;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.stonedt.intelligence.dao.campus.*;
import com.stonedt.intelligence.entity.campus.*;
import com.stonedt.intelligence.service.campus.CampusEventService;
import com.stonedt.intelligence.service.campus.support.CampusRiskLevel;
import com.stonedt.intelligence.util.SnowflakeUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class CampusEventServiceImpl implements CampusEventService {

    private static final String STATUS_PENDING_JUDGE = "pending_judge";
    private static final String STATUS_ASSIGNED = "assigned";
    private static final String STATUS_PROCESSING = "processing";
    private static final String STATUS_FEEDBACK = "feedback";
    private static final String STATUS_REVIEWED = "reviewed";
    private static final String STATUS_RATED = "rated";
    private static final String STATUS_ARCHIVED = "archived";
    private static final String CLUE_STATUS_ARCHIVED = "archived";
    private static final String CLUE_STATUS_CONVERTED = "converted";
    private static final String TASK_PENDING = "pending";
    private static final String TASK_COMPLETED = "completed";
    private static final String TASK_RETURNED = "returned";
    private static final String TASK_CONFIRMED = "confirmed";

    private final CampusEventDao campusEventDao;
    private final CampusEventClueDao campusEventClueDao;
    private final CampusEventAccountDao campusEventAccountDao;
    private final CampusDisposalTaskDao campusDisposalTaskDao;
    private final CampusDisposalRecordDao campusDisposalRecordDao;
    private final CampusClueDao campusClueDao;
    private final CampusAccountDao campusAccountDao;

    public CampusEventServiceImpl(CampusEventDao campusEventDao,
                                  CampusEventClueDao campusEventClueDao,
                                  CampusEventAccountDao campusEventAccountDao,
                                  CampusDisposalTaskDao campusDisposalTaskDao,
                                  CampusDisposalRecordDao campusDisposalRecordDao,
                                  CampusClueDao campusClueDao,
                                  CampusAccountDao campusAccountDao) {
        this.campusEventDao = campusEventDao;
        this.campusEventClueDao = campusEventClueDao;
        this.campusEventAccountDao = campusEventAccountDao;
        this.campusDisposalTaskDao = campusDisposalTaskDao;
        this.campusDisposalRecordDao = campusDisposalRecordDao;
        this.campusClueDao = campusClueDao;
        this.campusAccountDao = campusAccountDao;
    }

    @Override
    @Transactional
    public CampusEvent save(CampusEvent event, Long operatorUserId) {
        validateEvent(event);
        if (event.getEventId() == null) {
            event.setEventId(SnowflakeUtil.getId());
            event.setCreateUserId(operatorUserId);
            event.setUpdateUserId(operatorUserId);
            setEventDefaults(event);
            campusEventDao.insert(event);
            return campusEventDao.selectByEventId(event.getEventId());
        }
        CampusEvent old = requireEvent(event.getEventId());
        ensureNotArchived(old);
        event.setUpdateUserId(operatorUserId);
        normalizeRiskForUpdate(event);
        campusEventDao.update(event);
        return campusEventDao.selectByEventId(event.getEventId());
    }

    @Override
    @Transactional
    public CampusEvent createFromClue(Long clueId, CampusEvent event, Long operatorUserId) {
        CampusClue clue = campusClueDao.selectByClueId(clueId);
        if (clue == null) {
            throw new IllegalArgumentException("线索不存在");
        }
        ensureClueCanConvert(clue);
        if (event == null) {
            event = new CampusEvent();
        }
        if (StringUtils.isBlank(event.getEventTitle())) {
            event.setEventTitle(clue.getClueTitle());
        }
        if (StringUtils.isBlank(event.getEventSummary())) {
            event.setEventSummary(clue.getClueContent());
        }
        if (event.getFirstPublishTime() == null) {
            event.setFirstPublishTime(clue.getPublishTime());
        }
        if (event.getDiscoverTime() == null) {
            event.setDiscoverTime(clue.getDiscoverTime());
        }
        if (StringUtils.isBlank(event.getRiskLevel())) {
            event.setRiskLevel(CampusRiskLevel.normalizeOrDefault(clue.getRiskLevel()));
        }
        if (StringUtils.isBlank(event.getEventType())) {
            event.setEventType(clue.getTopicCategory());
        }
        if (event.getInvolvedDepartmentId() == null) {
            event.setInvolvedDepartmentId(clue.getInvolvedDepartmentId());
        }
        CampusEvent saved = save(event, operatorUserId);
        addEventClue(saved.getEventId(), clueId, operatorUserId);
        int converted = campusClueDao.markConverted(clueId, saved.getEventId(), operatorUserId);
        if (converted != 1) {
            throw new IllegalArgumentException("线索状态已变化，不能重复转事件");
        }
        return saved;
    }

    @Override
    public CampusEvent detail(Long eventId) {
        return requireEvent(eventId);
    }

    @Override
    public PageInfo<CampusEvent> list(Integer pageNum,
                                      Integer pageSize,
                                      String keyword,
                                      String riskLevel,
                                      String eventStatus,
                                      Date startTime,
                                      Date endTime) {
        PageHelper.startPage(defaultPageNum(pageNum), defaultPageSize(pageSize));
        return new PageInfo<>(campusEventDao.list(keyword, CampusRiskLevel.normalizeForQuery(riskLevel),
                eventStatus, startTime, endTime));
    }

    @Override
    @Transactional
    public CampusEvent rate(Long eventId, String riskLevel, String disposalRequirement, Long operatorUserId) {
        CampusEvent event = requireEvent(eventId);
        ensureStatus(event, "风险定级", STATUS_PENDING_JUDGE, STATUS_RATED);
        if (StringUtils.isBlank(riskLevel)) {
            throw new IllegalArgumentException("风险等级不能为空");
        }
        campusEventDao.rate(eventId, CampusRiskLevel.requireValid(riskLevel), disposalRequirement, operatorUserId);
        return campusEventDao.selectByEventId(eventId);
    }

    @Override
    @Transactional
    public CampusEventAccount addAccount(Long eventId, Long accountId, Long operatorUserId) {
        CampusEvent event = requireEvent(eventId);
        ensureNotArchived(event);
        if (accountId == null || campusAccountDao.selectByAccountId(accountId) == null) {
            throw new IllegalArgumentException("账号不存在");
        }
        CampusEventAccount relation = new CampusEventAccount();
        relation.setRelationId(SnowflakeUtil.getId());
        relation.setEventId(eventId);
        relation.setAccountId(accountId);
        relation.setDeleted(0);
        relation.setCreateUserId(operatorUserId);
        campusEventAccountDao.insert(relation);
        return relation;
    }

    @Override
    @Transactional
    public CampusDisposalTask assign(CampusDisposalTask task, Long operatorUserId) {
        validateTask(task);
        CampusEvent event = requireEvent(task.getEventId());
        ensureStatus(event, "分派处置", STATUS_RATED, STATUS_ASSIGNED, STATUS_PROCESSING);
        task.setDisposalTaskId(SnowflakeUtil.getId());
        task.setTaskStatus(TASK_PENDING);
        if (task.getDueTime() == null) {
            task.setDueTime(defaultDueTime(event.getRiskLevel()));
        }
        task.setDeleted(0);
        task.setCreateUserId(operatorUserId);
        task.setUpdateUserId(operatorUserId);
        campusDisposalTaskDao.insert(task);
        campusEventDao.updateStatus(task.getEventId(), STATUS_ASSIGNED, operatorUserId);
        CampusDisposalTask saved = campusDisposalTaskDao.selectByTaskId(task.getDisposalTaskId());
        addRecord(saved, "assign", "任务已分派，办理要求：" + StringUtils.defaultString(saved.getDisposalRequirement(), "-"),
                null, operatorUserId, null);
        return saved;
    }

    @Override
    @Transactional
    public CampusDisposalRecord feedback(Long disposalTaskId,
                                         String recordContent,
                                         String attachmentDesc,
                                         Long operatorUserId,
                                         String operatorName) {
        CampusDisposalTask task = requireTask(disposalTaskId);
        ensureTaskStatus(task, "处置反馈", TASK_PENDING, TASK_RETURNED);
        CampusEvent event = requireEvent(task.getEventId());
        ensureStatus(event, "处置反馈", STATUS_ASSIGNED, STATUS_PROCESSING);
        CampusDisposalRecord record = addRecord(task, "feedback", recordContent, attachmentDesc, operatorUserId, operatorName);
        campusDisposalTaskDao.updateStatus(disposalTaskId, TASK_COMPLETED, summary(recordContent), operatorUserId);
        campusEventDao.updateStatus(task.getEventId(), STATUS_FEEDBACK, operatorUserId);
        return record;
    }

    @Override
    @Transactional
    public CampusDisposalRecord returnTask(Long disposalTaskId,
                                           String recordContent,
                                           Long operatorUserId,
                                           String operatorName) {
        CampusDisposalTask task = requireTask(disposalTaskId);
        ensureTaskStatus(task, "退回重办", TASK_COMPLETED);
        CampusEvent event = requireEvent(task.getEventId());
        ensureStatus(event, "退回重办", STATUS_FEEDBACK, STATUS_REVIEWED);
        CampusDisposalRecord record = addRecord(task, "return", recordContent, null, operatorUserId, operatorName);
        campusDisposalTaskDao.updateStatus(disposalTaskId, TASK_RETURNED, summary(recordContent), operatorUserId);
        campusEventDao.updateStatus(task.getEventId(), STATUS_PROCESSING, operatorUserId);
        return record;
    }

    @Override
    @Transactional
    public CampusDisposalRecord confirm(Long disposalTaskId,
                                        String recordContent,
                                        Long operatorUserId,
                                        String operatorName) {
        CampusDisposalTask task = requireTask(disposalTaskId);
        ensureTaskStatus(task, "复核确认", TASK_COMPLETED);
        CampusEvent event = requireEvent(task.getEventId());
        ensureStatus(event, "复核确认", STATUS_FEEDBACK);
        CampusDisposalRecord record = addRecord(task, "confirm", recordContent, null, operatorUserId, operatorName);
        campusDisposalTaskDao.updateStatus(disposalTaskId, TASK_CONFIRMED, summary(recordContent), operatorUserId);
        campusEventDao.updateStatus(task.getEventId(), STATUS_REVIEWED, operatorUserId);
        return record;
    }

    @Override
    @Transactional
    public CampusEvent archive(Long eventId, String archiveConclusion, Long operatorUserId) {
        CampusEvent event = requireEvent(eventId);
        ensureStatus(event, "归档", STATUS_REVIEWED);
        if (StringUtils.isBlank(archiveConclusion)) {
            throw new IllegalArgumentException("归档结论不能为空");
        }
        campusEventDao.archive(eventId, archiveConclusion, operatorUserId);
        return campusEventDao.selectByEventId(eventId);
    }

    @Override
    public List<CampusEventClue> listClues(Long eventId) {
        requireEvent(eventId);
        return campusEventClueDao.listByEventId(eventId);
    }

    @Override
    public List<CampusClue> suggestSimilarClues(Long eventId, Integer limit) {
        CampusEvent event = requireEvent(eventId);
        List<CampusEventClue> relations = campusEventClueDao.listByEventId(eventId);
        List<Long> excludeClueIds = new ArrayList<>();
        String topicCategory = event.getEventType();
        if (relations != null) {
            for (CampusEventClue relation : relations) {
                if (relation == null || relation.getClueId() == null) {
                    continue;
                }
                excludeClueIds.add(relation.getClueId());
                if (StringUtils.isBlank(topicCategory)) {
                    CampusClue clue = campusClueDao.selectByClueId(relation.getClueId());
                    if (clue != null && StringUtils.isNotBlank(clue.getTopicCategory())) {
                        topicCategory = clue.getTopicCategory();
                    }
                }
            }
        }
        String keyword = StringUtils.isBlank(topicCategory) ? event.getEventTitle() : null;
        return campusClueDao.listSimilarForEvent(eventId, excludeClueIds, topicCategory,
                CampusRiskLevel.normalizeForQuery(event.getRiskLevel()), keyword, safeSimilarLimit(limit));
    }

    @Override
    public List<CampusEventAccount> listAccounts(Long eventId) {
        requireEvent(eventId);
        return campusEventAccountDao.listByEventId(eventId);
    }

    @Override
    public List<CampusDisposalTask> listTasks(Long eventId) {
        requireEvent(eventId);
        return campusDisposalTaskDao.listByEventId(eventId);
    }

    @Override
    public List<CampusDisposalRecord> listRecords(Long disposalTaskId) {
        requireTask(disposalTaskId);
        return campusDisposalRecordDao.listByTaskId(disposalTaskId);
    }

    private void addEventClue(Long eventId, Long clueId, Long operatorUserId) {
        CampusEventClue relation = new CampusEventClue();
        relation.setRelationId(SnowflakeUtil.getId());
        relation.setEventId(eventId);
        relation.setClueId(clueId);
        relation.setDeleted(0);
        relation.setCreateUserId(operatorUserId);
        campusEventClueDao.insert(relation);
    }

    private CampusDisposalRecord addRecord(CampusDisposalTask task,
                                           String recordType,
                                           String recordContent,
                                           String attachmentDesc,
                                           Long operatorUserId,
                                           String operatorName) {
        if (StringUtils.isBlank(recordContent)) {
            throw new IllegalArgumentException("处置记录内容不能为空");
        }
        CampusDisposalRecord record = new CampusDisposalRecord();
        record.setRecordId(SnowflakeUtil.getId());
        record.setDisposalTaskId(task.getDisposalTaskId());
        record.setEventId(task.getEventId());
        record.setRecordType(recordType);
        record.setRecordContent(recordContent);
        record.setAttachmentDesc(attachmentDesc);
        record.setHandlerUserId(operatorUserId);
        record.setHandlerName(operatorName);
        record.setHandleTime(new Date());
        campusDisposalRecordDao.insert(record);
        return record;
    }

    private void ensureNotArchived(CampusEvent event) {
        if (event != null && STATUS_ARCHIVED.equals(event.getEventStatus())) {
            throw new IllegalArgumentException("已归档事件不能继续编辑或流转");
        }
    }

    private void ensureClueCanConvert(CampusClue clue) {
        if (clue == null) {
            throw new IllegalArgumentException("线索不存在");
        }
        if (CLUE_STATUS_ARCHIVED.equals(clue.getClueStatus())) {
            throw new IllegalArgumentException("已归档线索不能转事件");
        }
        if (CLUE_STATUS_CONVERTED.equals(clue.getClueStatus()) || clue.getEventId() != null) {
            throw new IllegalArgumentException("线索已转事件，不能重复转事件");
        }
    }

    private void ensureStatus(CampusEvent event, String operation, String... allowedStatuses) {
        if (event == null) {
            throw new IllegalArgumentException("事件不存在");
        }
        String status = StringUtils.defaultString(event.getEventStatus());
        for (String allowedStatus : allowedStatuses) {
            if (StringUtils.equals(status, allowedStatus)) {
                return;
            }
        }
        throw new IllegalArgumentException(operation + "不允许在当前事件状态执行");
    }

    private void ensureTaskStatus(CampusDisposalTask task, String operation, String... allowedStatuses) {
        if (task == null) {
            throw new IllegalArgumentException("处置任务不存在");
        }
        String status = StringUtils.defaultString(task.getTaskStatus());
        for (String allowedStatus : allowedStatuses) {
            if (StringUtils.equals(status, allowedStatus)) {
                return;
            }
        }
        throw new IllegalArgumentException(operation + "不允许在当前任务状态执行");
    }

    private Date defaultDueTime(String riskLevel) {
        Calendar calendar = Calendar.getInstance();
        String normalized = CampusRiskLevel.normalizeOrDefault(riskLevel);
        if ("urgent".equals(normalized)) {
            calendar.add(Calendar.MINUTE, 30);
        } else if ("major".equals(normalized)) {
            calendar.add(Calendar.HOUR, 2);
        } else if ("concern".equals(normalized)) {
            calendar.add(Calendar.HOUR, 8);
        } else {
            calendar.add(Calendar.DATE, 1);
        }
        return calendar.getTime();
    }

    private int safeSimilarLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return 10;
        }
        return Math.min(limit, 50);
    }

    private void validateEvent(CampusEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("事件信息不能为空");
        }
        if (StringUtils.isBlank(event.getEventTitle())) {
            throw new IllegalArgumentException("事件标题不能为空");
        }
    }

    private void validateTask(CampusDisposalTask task) {
        if (task == null) {
            throw new IllegalArgumentException("处置任务不能为空");
        }
        if (task.getEventId() == null) {
            throw new IllegalArgumentException("事件ID不能为空");
        }
        if (StringUtils.isBlank(task.getTaskTitle())) {
            throw new IllegalArgumentException("任务标题不能为空");
        }
        if (task.getAssignedDepartmentId() == null) {
            throw new IllegalArgumentException("承办部门不能为空");
        }
    }

    private CampusEvent requireEvent(Long eventId) {
        if (eventId == null) {
            throw new IllegalArgumentException("事件ID不能为空");
        }
        CampusEvent event = campusEventDao.selectByEventId(eventId);
        if (event == null) {
            throw new IllegalArgumentException("事件不存在");
        }
        return event;
    }

    private CampusDisposalTask requireTask(Long disposalTaskId) {
        if (disposalTaskId == null) {
            throw new IllegalArgumentException("处置任务ID不能为空");
        }
        CampusDisposalTask task = campusDisposalTaskDao.selectByTaskId(disposalTaskId);
        if (task == null) {
            throw new IllegalArgumentException("处置任务不存在");
        }
        return task;
    }

    private void setEventDefaults(CampusEvent event) {
        if (event.getDiscoverTime() == null) {
            event.setDiscoverTime(new Date());
        }
        if (StringUtils.isBlank(event.getRiskLevel())) {
            event.setRiskLevel(CampusRiskLevel.normalCode());
        } else {
            event.setRiskLevel(CampusRiskLevel.requireValid(event.getRiskLevel()));
        }
        if (StringUtils.isBlank(event.getEventStatus())) {
            event.setEventStatus(STATUS_PENDING_JUDGE);
        }
        if (event.getCurrentHeat() == null) {
            event.setCurrentHeat(0);
        }
        event.setDeleted(0);
    }

    private void normalizeRiskForUpdate(CampusEvent event) {
        if (StringUtils.isBlank(event.getRiskLevel())) {
            event.setRiskLevel(null);
        } else {
            event.setRiskLevel(CampusRiskLevel.requireValid(event.getRiskLevel()));
        }
    }

    private String summary(String content) {
        if (content == null) {
            return null;
        }
        return content.length() > 500 ? content.substring(0, 500) : content;
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
