package com.stonedt.intelligence.dao.campus;

import com.stonedt.intelligence.entity.campus.CampusAccountContent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface CampusAccountContentDao {

    int insert(CampusAccountContent content);

    CampusAccountContent selectByContentId(@Param("contentId") Long contentId);

    List<CampusAccountContent> list(@Param("accountId") Long accountId,
                                    @Param("taskId") Long taskId,
                                    @Param("riskLevel") String riskLevel,
                                    @Param("keyword") String keyword);

    List<CampusAccountContent> listForDetection(@Param("startTime") Date startTime,
                                                @Param("endTime") Date endTime);
}
