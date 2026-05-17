package com.stonedt.intelligence.service.campus;

import com.github.pagehelper.PageInfo;
import com.stonedt.intelligence.entity.campus.*;

import java.util.Date;
import java.util.List;

public interface CampusEventService {

    CampusEvent save(CampusEvent event, Long operatorUserId);

    CampusEvent createFromClue(Long clueId, CampusEvent event, Long operatorUserId);

    CampusEvent addClueToEvent(Long eventId, Long clueId, Long operatorUserId);

    CampusEvent detail(Long eventId);

    PageInfo<CampusEvent> list(Integer pageNum,
                               Integer pageSize,
                               String keyword,
                               String riskLevel,
                               String eventStatus,
                               Date startTime,
                               Date endTime);

    CampusEvent rate(Long eventId, String riskLevel, String disposalRequirement, Long operatorUserId);

    CampusEventAccount addAccount(Long eventId, Long accountId, Long operatorUserId);

    CampusDisposalTask assign(CampusDisposalTask task, Long operatorUserId);

    CampusDisposalRecord feedback(Long disposalTaskId,
                                  String recordContent,
                                  String attachmentDesc,
                                  Long operatorUserId,
                                  String operatorName);

    CampusDisposalRecord returnTask(Long disposalTaskId,
                                    String recordContent,
                                    Long operatorUserId,
                                    String operatorName);

    CampusDisposalRecord confirm(Long disposalTaskId,
                                 String recordContent,
                                 Long operatorUserId,
                                 String operatorName);

    CampusDisposalRecord recordOfflineDisposal(Long eventId,
                                               String recordContent,
                                               String attachmentDesc,
                                               Long operatorUserId,
                                               String operatorName);

    CampusEvent archive(Long eventId, String archiveConclusion, Long operatorUserId);

    List<CampusEventClue> listClues(Long eventId);

    List<CampusClue> suggestSimilarClues(Long eventId, Integer limit);

    List<CampusEventAccount> listAccounts(Long eventId);

    List<CampusDisposalTask> listTasks(Long eventId);

    List<CampusDisposalRecord> listRecords(Long disposalTaskId);

    List<CampusDisposalRecord> listRecordsByEvent(Long eventId);
}
