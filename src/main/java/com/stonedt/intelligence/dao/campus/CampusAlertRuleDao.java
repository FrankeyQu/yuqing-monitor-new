package com.stonedt.intelligence.dao.campus;

import com.stonedt.intelligence.entity.campus.CampusAlertRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CampusAlertRuleDao {

    int insert(CampusAlertRule rule);

    int update(CampusAlertRule rule);

    int logicalDelete(@Param("ruleId") Long ruleId, @Param("updateUserId") Long updateUserId);

    CampusAlertRule selectByRuleId(@Param("ruleId") Long ruleId);

    List<CampusAlertRule> list(@Param("keyword") String keyword,
                               @Param("ruleType") String ruleType,
                               @Param("enabled") Integer enabled);
}
