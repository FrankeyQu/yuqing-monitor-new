package com.stonedt.intelligence.service.impl.campus;

import com.stonedt.intelligence.dao.campus.CampusClueDao;
import com.stonedt.intelligence.dao.campus.CampusDashboardDao;
import com.stonedt.intelligence.dao.campus.CampusMonitorResultDao;
import com.stonedt.intelligence.service.campus.CampusDashboardService;
import com.stonedt.intelligence.service.campus.ai.CampusAiKeywordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CampusDashboardServiceImpl implements CampusDashboardService {

    private static final long WORD_CLOUD_CACHE_MILLIS = 10 * 60 * 1000L;

    private final CampusDashboardDao campusDashboardDao;
    private final CampusClueDao campusClueDao;
    private final CampusMonitorResultDao campusMonitorResultDao;
    private final CampusAiKeywordService campusAiKeywordService;
    private volatile List<Map<String, Object>> cachedWordCloud;
    private volatile long cachedWordCloudAt;

    @Autowired
    public CampusDashboardServiceImpl(CampusDashboardDao campusDashboardDao,
                                       CampusClueDao campusClueDao,
                                       CampusMonitorResultDao campusMonitorResultDao,
                                       CampusAiKeywordService campusAiKeywordService) {
        this.campusDashboardDao = campusDashboardDao;
        this.campusClueDao = campusClueDao;
        this.campusMonitorResultDao = campusMonitorResultDao;
        this.campusAiKeywordService = campusAiKeywordService;
    }

    public CampusDashboardServiceImpl(CampusDashboardDao campusDashboardDao,
                                      CampusClueDao campusClueDao) {
        this.campusDashboardDao = campusDashboardDao;
        this.campusClueDao = campusClueDao;
        this.campusMonitorResultDao = null;
        this.campusAiKeywordService = null;
    }

    @Override
    public Map<String, Object> overview() {
        return campusDashboardDao.overview();
    }

    @Override
    public Map<String, Object> statistics() {
        Map<String, Object> statistics = new LinkedHashMap<>();
        Map<String, Object> monitorOverview = campusDashboardDao.monitorOverview();
        List<Map<String, Object>> monitorTrend = campusDashboardDao.monitorTrendByDay();
        if (campusMonitorResultDao != null) {
            monitorOverview = monitorOverview == null ? new LinkedHashMap<>() : new LinkedHashMap<>(monitorOverview);
            monitorOverview.put("todayResultCount", campusMonitorResultDao.countInformationToday("risk"));
            monitorTrend = campusMonitorResultDao.monitorInformationTrendByDay("risk", 7);
        }
        statistics.put("overview", campusDashboardDao.overview());
        statistics.put("monitorOverview", monitorOverview);
        statistics.put("riskDistribution", campusDashboardDao.riskDistribution());
        statistics.put("clueSourceDistribution", campusDashboardDao.clueSourceDistribution());
        statistics.put("eventStatusDistribution", campusDashboardDao.eventStatusDistribution());
        statistics.put("trendByDay", campusDashboardDao.trendByDay());
        statistics.put("monitorTrendByDay", monitorTrend);
        statistics.put("alertRiskDistribution", campusDashboardDao.alertRiskDistribution());
        statistics.put("detectionHitRiskDistribution", campusDashboardDao.detectionHitRiskDistribution());
        statistics.put("sourceRiskDistribution", campusDashboardDao.sourceRiskDistribution());
        statistics.put("topicRiskDistribution", campusDashboardDao.topicRiskDistribution());
        statistics.put("governanceMetrics", campusDashboardDao.governanceMetrics());
        statistics.put("sentimentDistribution", campusClueDao.countBySentiment());
        statistics.put("mediaDistribution", campusDashboardDao.mediaDistribution());
        return statistics;
    }

    @Override
    public List<Map<String, Object>> getWordCloud() {
        long now = System.currentTimeMillis();
        if (cachedWordCloud != null && now - cachedWordCloudAt < WORD_CLOUD_CACHE_MILLIS) {
            return cachedWordCloud;
        }
        List<Map<String, Object>> fallback = buildKeywordWordCloud();
        List<Map<String, Object>> result = fallback;
        if (campusAiKeywordService != null) {
            List<String> texts = campusClueDao.listRecentWordCloudTexts(80);
            result = campusAiKeywordService.extractWordCloud(texts, fallback, 50);
        }
        cachedWordCloud = result;
        cachedWordCloudAt = now;
        return result;
    }

    private List<Map<String, Object>> buildKeywordWordCloud() {
        List<String> keywordList = campusClueDao.getAllKeywords();
        Map<String, Integer> freqMap = new HashMap<>();
        for (String kw : keywordList) {
            if (kw == null || kw.trim().isEmpty()) {
                continue;
            }
            String[] parts = kw.split("[,，;；、\\s]+");
            for (String part : parts) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) {
                    freqMap.merge(trimmed, 1, Integer::sum);
                }
            }
        }
        return freqMap.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(50)
                .map(e -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("name", e.getKey());
                    m.put("value", e.getValue());
                    return m;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getTrend(int days) {
        if (days <= 0) {
            days = 7;
        }
        return campusClueDao.getDailyTrend(days);
    }
}
