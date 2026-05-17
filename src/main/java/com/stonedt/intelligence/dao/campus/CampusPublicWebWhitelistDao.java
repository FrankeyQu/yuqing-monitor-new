package com.stonedt.intelligence.dao.campus;

import com.stonedt.intelligence.entity.campus.CampusPublicWebWhitelist;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CampusPublicWebWhitelistDao {

    int insert(CampusPublicWebWhitelist whitelist);

    int update(CampusPublicWebWhitelist whitelist);

    int updateStatus(@Param("whitelistId") Long whitelistId,
                     @Param("enabled") Integer enabled,
                     @Param("updateUserId") Long updateUserId);

    int logicalDelete(@Param("whitelistId") Long whitelistId,
                      @Param("updateUserId") Long updateUserId);

    CampusPublicWebWhitelist selectByWhitelistId(@Param("whitelistId") Long whitelistId);

    List<CampusPublicWebWhitelist> list(@Param("keyword") String keyword,
                                        @Param("siteDomain") String siteDomain,
                                        @Param("enabled") Integer enabled);
}
