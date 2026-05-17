package com.stonedt.intelligence.dao.campus;

import com.stonedt.intelligence.entity.campus.CampusSensitiveWord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CampusSensitiveWordDao {

    int insert(CampusSensitiveWord word);

    int update(CampusSensitiveWord word);

    int logicalDelete(@Param("wordId") Long wordId, @Param("updateUserId") Long updateUserId);

    CampusSensitiveWord selectByWordId(@Param("wordId") Long wordId);

    List<CampusSensitiveWord> list(@Param("keyword") String keyword,
                                   @Param("wordCategory") String wordCategory,
                                   @Param("riskLevel") String riskLevel,
                                   @Param("status") Integer status);

    List<CampusSensitiveWord> listEnabled();
}
