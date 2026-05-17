package com.stonedt.intelligence.dao.campus;

import com.stonedt.intelligence.entity.campus.CampusDetectionTopic;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CampusDetectionTopicDao {

    int insert(CampusDetectionTopic topic);

    int update(CampusDetectionTopic topic);

    int logicalDelete(@Param("topicId") Long topicId, @Param("updateUserId") Long updateUserId);

    CampusDetectionTopic selectByTopicId(@Param("topicId") Long topicId);

    List<CampusDetectionTopic> list(@Param("keyword") String keyword,
                                    @Param("topicCategory") String topicCategory,
                                    @Param("enabled") Integer enabled);
}
