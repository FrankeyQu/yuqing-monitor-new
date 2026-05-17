package com.stonedt.intelligence.service.campus;

import com.github.pagehelper.PageInfo;
import com.stonedt.intelligence.entity.campus.CampusPublicWebWhitelist;

public interface CampusPublicWebWhitelistService {

    CampusPublicWebWhitelist save(CampusPublicWebWhitelist whitelist, Long operatorUserId);

    CampusPublicWebWhitelist updateStatus(Long whitelistId, Integer enabled, Long operatorUserId);

    void delete(Long whitelistId, Long operatorUserId);

    CampusPublicWebWhitelist requireEnabled(Long whitelistId);

    PageInfo<CampusPublicWebWhitelist> list(Integer pageNum,
                                            Integer pageSize,
                                            String keyword,
                                            String siteDomain,
                                            Integer enabled);
}
