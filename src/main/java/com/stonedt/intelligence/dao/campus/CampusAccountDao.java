package com.stonedt.intelligence.dao.campus;

import com.stonedt.intelligence.entity.campus.CampusAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface CampusAccountDao {

    int insert(CampusAccount account);

    int update(CampusAccount account);

    int audit(@Param("accountId") Long accountId,
              @Param("auditStatus") String auditStatus,
              @Param("auditOpinion") String auditOpinion,
              @Param("accountStatus") String accountStatus,
              @Param("auditUserId") Long auditUserId,
              @Param("updateUserId") Long updateUserId);

    int updateStatus(@Param("accountId") Long accountId,
                     @Param("accountStatus") String accountStatus,
                     @Param("updateUserId") Long updateUserId);

    int logicalDelete(@Param("accountId") Long accountId, @Param("updateUserId") Long updateUserId);

    CampusAccount selectByAccountId(@Param("accountId") Long accountId);

    List<CampusAccount> list(@Param("keyword") String keyword,
                             @Param("platform") String platform,
                             @Param("focusLevel") String focusLevel,
                             @Param("auditStatus") String auditStatus,
                             @Param("accountStatus") String accountStatus,
                             @Param("focusEndBefore") Date focusEndBefore);

    int countPlatformAccount(@Param("platform") String platform,
                             @Param("accountUid") String accountUid,
                             @Param("excludeAccountId") Long excludeAccountId);
}
