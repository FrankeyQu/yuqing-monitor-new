package com.stonedt.intelligence.dao.campus;

import com.stonedt.intelligence.entity.campus.CampusClue;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface CampusEducationDao {

    List<CampusClue> listTopicClues(@Param("keywords") List<String> keywords,
                                    @Param("startTime") Date startTime,
                                    @Param("endTime") Date endTime,
                                    @Param("limit") Integer limit);

    List<CampusClue> listRankingClues(@Param("keyword") String keyword,
                                      @Param("startTime") Date startTime,
                                      @Param("endTime") Date endTime,
                                      @Param("limit") Integer limit);
}
