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
        List<Map<String, Object>> monitorTrendAll = monitorTrend;
        List<Map<String, Object>> monitorTrendRisk = monitorTrend;
        List<Map<String, Object>> monitorSourceDistribution = new ArrayList<>();
        List<Map<String, Object>> monitorSentimentDistribution = new ArrayList<>();
        List<Map<String, Object>> monitorTopicRiskDistribution = new ArrayList<>();
        if (campusMonitorResultDao != null) {
            monitorOverview = monitorOverview == null ? new LinkedHashMap<>() : new LinkedHashMap<>(monitorOverview);
            int todayAllResultCount = campusMonitorResultDao.countInformationToday("all");
            int todayRiskResultCount = campusMonitorResultDao.countInformationToday("risk");
            monitorSentimentDistribution = campusMonitorResultDao.monitorInformationSentimentDistribution("all");
            monitorOverview.put("todayAllResultCount", todayAllResultCount);
            monitorOverview.put("todayRiskResultCount", todayRiskResultCount);
            monitorOverview.put("negativeRate", calculateNegativeRate(monitorSentimentDistribution));
            monitorOverview.put("todayResultCount", todayRiskResultCount);
            monitorTrendAll = campusMonitorResultDao.monitorInformationTrendByDay("all", 7);
            monitorTrendRisk = campusMonitorResultDao.monitorInformationTrendByDay("risk", 7);
            monitorTrend = monitorTrendRisk;
            monitorSourceDistribution = campusMonitorResultDao.monitorInformationSourceDistribution("all", 12);
            monitorTopicRiskDistribution = campusMonitorResultDao.monitorInformationTopicRiskDistribution("all", 12);
        }
        statistics.put("overview", campusDashboardDao.overview());
        statistics.put("monitorOverview", monitorOverview);
        statistics.put("riskDistribution", campusDashboardDao.riskDistribution());
        statistics.put("clueSourceDistribution", campusDashboardDao.clueSourceDistribution());
        statistics.put("eventStatusDistribution", campusDashboardDao.eventStatusDistribution());
        statistics.put("trendByDay", campusDashboardDao.trendByDay());
        statistics.put("monitorTrendByDay", monitorTrend);
        statistics.put("monitorTrendAllByDay", monitorTrendAll);
        statistics.put("monitorTrendRiskByDay", monitorTrendRisk);
        statistics.put("alertRiskDistribution", campusDashboardDao.alertRiskDistribution());
        statistics.put("detectionHitRiskDistribution", campusDashboardDao.detectionHitRiskDistribution());
        statistics.put("sourceRiskDistribution", campusDashboardDao.sourceRiskDistribution());
        statistics.put("topicRiskDistribution", campusDashboardDao.topicRiskDistribution());
        statistics.put("governanceMetrics", campusDashboardDao.governanceMetrics());
        statistics.put("sentimentDistribution", campusClueDao.countBySentiment());
        statistics.put("mediaDistribution", campusDashboardDao.mediaDistribution());
        statistics.put("monitorSourceDistribution", monitorSourceDistribution);
        statistics.put("monitorSentimentDistribution", monitorSentimentDistribution);
        statistics.put("monitorTopicRiskDistribution", monitorTopicRiskDistribution);
        return statistics;
    }

    private int calculateNegativeRate(List<Map<String, Object>> distribution) {
        int total = 0;
        int negative = 0;
        for (Map<String, Object> item : distribution) {
            int value = toInt(item.get("value"));
            total += value;
            Object name = item.get("name");
            if ("negative".equals(String.valueOf(name))) {
                negative += value;
            }
        }
        if (total <= 0) {
            return 0;
        }
        return Math.min(100, Math.round(negative * 100f / total));
    }

    private int toInt(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception ignored) {
            return 0;
        }
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
