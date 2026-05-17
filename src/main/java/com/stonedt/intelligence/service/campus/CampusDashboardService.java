package com.stonedt.intelligence.service.campus;

import java.util.List;
import java.util.Map;

public interface CampusDashboardService {

    Map<String, Object> overview();

    Map<String, Object> statistics();

    List<Map<String, Object>> getWordCloud();

    List<Map<String, Object>> getTrend(int days);
}
