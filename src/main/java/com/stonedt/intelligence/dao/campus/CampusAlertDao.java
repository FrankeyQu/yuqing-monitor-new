package com.stonedt.intelligence.dao.campus;

import com.stonedt.intelligence.entity.campus.CampusAlert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CampusAlertDao {

    int insert(CampusAlert alert);

    int handle(@Param("alertId") Long alertId,
               @Param("alertStatus") String alertStatus,
               @Param("handleOpinion") String handleOpinion,
               @Param("handlerUserId") Long handlerUserId,
               @Param("updateUserId") Long updateUserId);

    CampusAlert selectByAlertId(@Param("alertId") Long alertId);

    int countExisting(@Param("alertSource") String alertSource,
                      @Param("sourceObjectId") Long sourceObjectId,
                      @Param("ruleId") Long ruleId,
                      @Param("matchedKeywords") String matchedKeywords);

    List<CampusAlert> list(@Param("keyword") String keyword,
                           @Param("alertSource") String alertSource,
                           @Param("riskLevel") String riskLevel,
                           @Param("alertStatus") String alertStatus);

    List<CampusAlert> listMonitorAlerts(@Param("monitorTaskId") Long monitorTaskId,
                                        @Param("keyword") String keyword,
                                        @Param("riskLevel") String riskLevel,
                                        @Param("alertStatus") String alertStatus);

    int countBySourceAndStatus(@Param("alertSource") String alertSource,
                               @Param("alertStatus") String alertStatus);
}
