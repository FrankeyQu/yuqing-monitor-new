package com.stonedt.intelligence.service.impl.campus;

import com.stonedt.intelligence.dao.campus.CampusClueDao;
import com.stonedt.intelligence.service.campus.CampusCompareService;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CampusCompareServiceImpl implements CampusCompareService {

    private final CampusClueDao campusClueDao;

    public CampusCompareServiceImpl(CampusClueDao campusClueDao) {
        this.campusClueDao = campusClueDao;
    }

    @Override
    public Map<String, Object> compare(String selfSubject, String competitorSubject) {
        Map<String, Object> result = new LinkedHashMap<>();

        // ---------------- 查询真实数据 ----------------
        int selfCount = campusClueDao.countByKeyword(selfSubject);
        int competitorCount = campusClueDao.countByKeyword(competitorSubject);

        List<Map<String, Object>> selfSentiment = campusClueDao.countBySentimentByKeyword(selfSubject);
        List<Map<String, Object>> competitorSentiment = campusClueDao.countBySentimentByKeyword(competitorSubject);

        List<Map<String, Object>> selfPlatform = campusClueDao.countByPlatformByKeyword(selfSubject);
        List<Map<String, Object>> competitorPlatform = campusClueDao.countByPlatformByKeyword(competitorSubject);

        List<Map<String, Object>> selfTrend = campusClueDao.getDailyTrendByKeyword(7, selfSubject);
        List<Map<String, Object>> competitorTrend = campusClueDao.getDailyTrendByKeyword(7, competitorSubject);

        // ---------------- 雷达图 ----------------
        result.put("radarData", buildRadarData(selfCount, competitorCount,
                selfSentiment, competitorSentiment,
                selfPlatform, competitorPlatform,
                selfTrend, competitorTrend));

        // ---------------- 声量趋势 ----------------
        result.put("volumeTrend", buildVolumeTrend(selfTrend, competitorTrend));

        // ---------------- 本品/竞品情感分布 ----------------
        result.put("selfSentiment", selfSentiment.isEmpty() ? createDefaultSentiment() : selfSentiment);
        result.put("competitorSentiment", competitorSentiment.isEmpty() ? createDefaultSentiment() : competitorSentiment);

        // ---------------- 本品/竞品媒体分布 ----------------
        result.put("selfMediaDistribution", selfPlatform.isEmpty() ? createDefaultPlatform() : selfPlatform);
        result.put("competitorMediaDistribution", competitorPlatform.isEmpty() ? createDefaultPlatform() : competitorPlatform);

        return result;
    }

    // ======================== 雷达图 ========================

    private List<Map<String, Object>> buildRadarData(int selfCount, int competitorCount,
                                                     List<Map<String, Object>> selfSentiment,
                                                     List<Map<String, Object>> competitorSentiment,
                                                     List<Map<String, Object>> selfPlatform,
                                                     List<Map<String, Object>> competitorPlatform,
                                                     List<Map<String, Object>> selfTrend,
                                                     List<Map<String, Object>> competitorTrend) {
        double selfPositiveRate = calcPositiveRate(selfSentiment);
        double competitorPositiveRate = calcPositiveRate(competitorSentiment);

        int selfPlatformCount = selfPlatform != null ? selfPlatform.size() : 0;
        int competitorPlatformCount = competitorPlatform != null ? competitorPlatform.size() : 0;

        double selfSpeed = calcSpeed(selfTrend, selfCount);
        double competitorSpeed = calcSpeed(competitorTrend, competitorCount);

        List<Map<String, Object>> radarData = new ArrayList<>();

        // 网络声量: 归一化到 0-100
        radarData.add(createRadarItem("网络声量",
                normalize(selfCount, competitorCount),
                normalize(competitorCount, selfCount)));

        // 正面率: 天然 0-100
        radarData.add(createRadarItem("正面率", selfPositiveRate, competitorPositiveRate));

        // 媒体覆盖: 归一化到 0-100
        radarData.add(createRadarItem("媒体覆盖",
                normalize(selfPlatformCount, competitorPlatformCount),
                normalize(competitorPlatformCount, selfPlatformCount)));

        // 地域覆盖: 暂用固定值 50
        radarData.add(createRadarItem("地域覆盖", 50, 50));

        // 传播速度: 天然 0-100
        radarData.add(createRadarItem("传播速度", selfSpeed, competitorSpeed));

        return radarData;
    }

    /**
     * 归一化：以两者中的最大值为基准，映射到 0-100
     */
    private double normalize(int value, int maxOfOther) {
        int max = Math.max(value, maxOfOther);
        if (max == 0) return 0;
        return Math.round(value * 100.0 / max);
    }

    private Map<String, Object> createRadarItem(String dimension, double self, double competitor) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("dimension", dimension);
        item.put("self", self);
        item.put("competitor", competitor);
        return item;
    }

    /**
     * 计算正面率 = 正面数量 / 总数 * 100
     */
    private double calcPositiveRate(List<Map<String, Object>> sentimentList) {
        if (sentimentList == null || sentimentList.isEmpty()) return 0;
        int total = 0, positive = 0;
        for (Map<String, Object> m : sentimentList) {
            int v = toInt(m.get("value"));
            total += v;
            if ("正面".equals(String.valueOf(m.get("name")))) {
                positive = v;
            }
        }
        if (total == 0) return 0;
        return Math.round(positive * 100.0 / total);
    }

    /**
     * 计算传播速度 = 今天线索数 / 总线索数 * 100
     */
    private double calcSpeed(List<Map<String, Object>> trend, int total) {
        if (trend == null || trend.isEmpty() || total == 0) return 0;
        Map<String, Object> today = trend.get(trend.size() - 1);
        int todayCount = toInt(today.get("clueCount"));
        return Math.round(todayCount * 100.0 / total);
    }

    // ======================== 声量趋势 ========================

    private List<Map<String, Object>> buildVolumeTrend(List<Map<String, Object>> selfTrend,
                                                       List<Map<String, Object>> competitorTrend) {
        int len = Math.max(selfTrend != null ? selfTrend.size() : 0,
                competitorTrend != null ? competitorTrend.size() : 0);
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < len; i++) {
            Map<String, Object> item = new LinkedHashMap<>();
            String date = getTrendField(selfTrend, i, "date");
            int selfCount = getTrendInt(selfTrend, i, "clueCount");
            int competitorCount = getTrendInt(competitorTrend, i, "clueCount");
            item.put("date", date);
            item.put("self", selfCount);
            item.put("competitor", competitorCount);
            result.add(item);
        }
        return result;
    }

    private String getTrendField(List<Map<String, Object>> list, int idx, String key) {
        if (list != null && idx < list.size()) {
            Object val = list.get(idx).get(key);
            return val != null ? String.valueOf(val) : "";
        }
        return "";
    }

    private int getTrendInt(List<Map<String, Object>> list, int idx, String key) {
        if (list != null && idx < list.size()) {
            return toInt(list.get(idx).get(key));
        }
        return 0;
    }

    // ======================== 默认值 ========================

    private List<Map<String, Object>> createDefaultSentiment() {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> p = new LinkedHashMap<>(); p.put("name", "正面"); p.put("value", 50); list.add(p);
        Map<String, Object> n = new LinkedHashMap<>(); n.put("name", "中性"); n.put("value", 30); list.add(n);
        Map<String, Object> neg = new LinkedHashMap<>(); neg.put("name", "负面"); neg.put("value", 20); list.add(neg);
        return list;
    }

    private List<Map<String, Object>> createDefaultPlatform() {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> a = new LinkedHashMap<>(); a.put("name", "微博"); a.put("value", 45); list.add(a);
        Map<String, Object> b = new LinkedHashMap<>(); b.put("name", "微信"); b.put("value", 30); list.add(b);
        Map<String, Object> c = new LinkedHashMap<>(); c.put("name", "新闻"); c.put("value", 25); list.add(c);
        Map<String, Object> d = new LinkedHashMap<>(); d.put("name", "抖音"); d.put("value", 20); list.add(d);
        Map<String, Object> e = new LinkedHashMap<>(); e.put("name", "论坛"); e.put("value", 15); list.add(e);
        return list;
    }

    // ======================== 工具方法 ========================

    /**
     * 安全地将 Object 转为 int，兼容 Long/BigInteger/BigDecimal 等常见 JDBC 返回类型
     */
    private int toInt(Object obj) {
        if (obj == null) return 0;
        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(obj));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
