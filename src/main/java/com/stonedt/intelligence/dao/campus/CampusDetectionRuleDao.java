package com.stonedt.intelligence.dao.campus;

import com.stonedt.intelligence.entity.campus.CampusDetectionRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CampusDetectionRuleDao {

    int insert(CampusDetectionRule rule);

    int update(CampusDetectionRule rule);

    int logicalDelete(@Param("ruleId") Long ruleId, @Param("updateUserId") Long updateUserId);

    CampusDetectionRule selectByRuleId(@Param("ruleId") Long ruleId);

    List<CampusDetectionRule> list(@Param("topicId") Long topicId,
                                   @Param("ruleType") String ruleType,
                                   @Param("enabled") Integer enabled);

    List<CampusDetectionRule> listEnabledByTopicId(@Param("topicId") Long topicId);
}
