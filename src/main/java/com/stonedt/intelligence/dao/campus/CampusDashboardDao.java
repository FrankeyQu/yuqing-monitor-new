package com.stonedt.intelligence.dao.campus;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface CampusDashboardDao {

    Map<String, Object> overview();

    Map<String, Object> monitorOverview();

    List<Map<String, Object>> riskDistribution();

    List<Map<String, Object>> clueSourceDistribution();

    List<Map<String, Object>> eventStatusDistribution();

    List<Map<String, Object>> trendByDay();

    List<Map<String, Object>> monitorTrendByDay();

    List<Map<String, Object>> alertRiskDistribution();

    List<Map<String, Object>> detectionHitRiskDistribution();

    List<Map<String, Object>> sourceRiskDistribution();

    List<Map<String, Object>> mediaDistribution();

    List<Map<String, Object>> topicRiskDistribution();

    Map<String, Object> governanceMetrics();
}
