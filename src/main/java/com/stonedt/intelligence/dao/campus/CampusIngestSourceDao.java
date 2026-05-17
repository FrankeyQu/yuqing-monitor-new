package com.stonedt.intelligence.dao.campus;

import com.stonedt.intelligence.entity.campus.CampusIngestSource;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CampusIngestSourceDao {

    int insert(CampusIngestSource source);

    int update(CampusIngestSource source);

    int logicalDelete(@Param("sourceId") Long sourceId, @Param("updateUserId") Long updateUserId);

    CampusIngestSource selectBySourceId(@Param("sourceId") Long sourceId);

    List<CampusIngestSource> list(@Param("keyword") String keyword,
                                  @Param("sourceType") String sourceType,
                                  @Param("platform") String platform,
                                  @Param("enabled") Integer enabled);
}
