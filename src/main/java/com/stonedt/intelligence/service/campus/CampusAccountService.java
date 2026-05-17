package com.stonedt.intelligence.service.campus;

import com.github.pagehelper.PageInfo;
import com.stonedt.intelligence.entity.campus.CampusAccount;
import com.stonedt.intelligence.entity.campus.CampusAccountContent;
import com.stonedt.intelligence.entity.campus.CampusAccountTask;

import java.util.Date;
import java.util.List;

public interface CampusAccountService {

    CampusAccount save(CampusAccount account, Long operatorUserId);

    CampusAccount detail(Long accountId);

    PageInfo<CampusAccount> list(Integer pageNum,
                                 Integer pageSize,
                                 String keyword,
                                 String platform,
                                 String focusLevel,
                                 String auditStatus,
                                 String accountStatus,
                                 Date focusEndBefore);

    CampusAccount audit(Long accountId,
                        String auditStatus,
                        String auditOpinion,
                        Long operatorUserId);

    CampusAccount updateStatus(Long accountId, String accountStatus, Long operatorUserId);

    void delete(Long accountId, Long operatorUserId);

    CampusAccountTask addTask(CampusAccountTask task, Long operatorUserId);

    List<CampusAccountTask> listTasks(Long accountId);

    CampusAccountContent addContent(CampusAccountContent content, Long operatorUserId);

    PageInfo<CampusAccountContent> listContents(Integer pageNum,
                                                Integer pageSize,
                                                Long accountId,
                                                Long taskId,
                                                String riskLevel,
                                                String keyword);
}
