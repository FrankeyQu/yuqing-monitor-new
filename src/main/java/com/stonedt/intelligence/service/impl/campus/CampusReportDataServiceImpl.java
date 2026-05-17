package com.stonedt.intelligence.service.impl.campus;

import com.stonedt.intelligence.dao.campus.CampusClueDao;
import com.stonedt.intelligence.entity.campus.CampusClue;
import com.stonedt.intelligence.service.campus.CampusReportDataService;
import com.stonedt.intelligence.vo.ReportDataVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class CampusReportDataServiceImpl implements CampusReportDataService {

    private final CampusClueDao campusClueDao;

    public CampusReportDataServiceImpl(CampusClueDao campusClueDao) {
        this.campusClueDao = campusClueDao;
    }

    @Override
    public ReportDataVO aggregateReportData(String keyword, Date startTime, Date endTime) {
        return aggregateReportData(keyword, null, startTime, endTime);
    }

    @Override
    public ReportDataVO aggregateReportData(String keyword, Long eventId, Date startTime, Date endTime) {
        ReportDataVO vo = new ReportDataVO();

        // 日期格式化
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        if (startTime != null) {
            vo.setPeriodStart(sdf.format(startTime));
        }
        if (endTime != null) {
            vo.setPeriodEnd(sdf.format(endTime));
        }

        // 计算趋势天数：基于日期范围，最小 1 天，最大 30 天
        int days = computeDays(startTime, endTime);
        String scopedKeyword = eventId == null ? StringUtils.trimToNull(keyword) : null;
        List<CampusClue> scopedClues = campusClueDao.listForReportScope(scopedKeyword, eventId, startTime, endTime);
        if (scopedClues == null) {
            scopedClues = Collections.emptyList();
        }

        // ---- 1. 舆情走势 (daily trend) ----
        List<Map<String, Object>> trend = campusClueDao.getReportDailyTrend(scopedKeyword, eventId, startTime, endTime, days);
        vo.setTrend(trend != null ? trend : Collections.<Map<String, Object>>emptyList());

        // ---- 2. 媒体分布 (platform distribution) ----
        List<Map<String, Object>> mediaDistribution = buildPlatformDistribution(scopedClues);
        vo.setMediaDistribution(mediaDistribution);

        // ---- 3. 平台排名 (复用 mediaDistribution，已按 value DESC 排序 TOP 10) ----
        vo.setPlatformRanking(mediaDistribution);

        // ---- 4. 情感分布 (sentiment distribution) ----
        List<Map<String, Object>> sentimentDistribution = buildSentimentDistribution(scopedClues);
        vo.setSentimentDistribution(sentimentDistribution);

        // ---- 5. 情感计数 ----
        int negative = 0, neutral = 0, positive = 0;
        if (sentimentDistribution != null) {
            for (Map<String, Object> item : sentimentDistribution) {
                String name = String.valueOf(item.getOrDefault("name", ""));
                Object valObj = item.get("value");
                if (valObj == null) {
                    continue;
                }
                // MyBatis returns Long for COUNT(*)
                int val = 0;
                if (valObj instanceof Number) {
                    val = ((Number) valObj).intValue();
                } else {
                    try {
                        val = Integer.parseInt(valObj.toString());
                    } catch (NumberFormatException ignored) {
                    }
                }
                if (name.contains("负面")) {
                    negative += val;
                } else if (name.contains("中性")) {
                    neutral += val;
                } else if (name.contains("正面")) {
                    positive += val;
                }
            }
        }
        vo.setNegativeCount(negative);
        vo.setNeutralCount(neutral);
        vo.setPositiveCount(positive);

        // ---- 6. 总数 ----
        vo.setTotalCount(scopedClues.size());

        // ---- 7. 热词分析 (top 20 keywords by frequency) ----
        List<Map<String, Object>> hotKeywords = buildHotKeywords(scopedClues);
        vo.setHotKeywords(hotKeywords);

        // ---- 8. 热点文章 (top 10 hot articles) ----
        List<Map<String, Object>> hotArticles = buildHotArticles(scopedClues);
        vo.setHotArticles(hotArticles);

        // ---- 9. 概览摘要 ----
        vo.setSummary(buildSummary(vo));

        return vo;
    }

    /**
     * 计算趋势展示天数
     */
    private int computeDays(Date startTime, Date endTime) {
        if (startTime != null && endTime != null) {
            long diff = endTime.getTime() - startTime.getTime();
            long days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
            return Math.max(1, Math.min((int) days + 1, 30));
        }
        if (startTime != null) {
            long diff = System.currentTimeMillis() - startTime.getTime();
            long days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
            return Math.max(1, Math.min((int) days + 1, 30));
        }
        return 7;
    }

    private List<Map<String, Object>> buildPlatformDistribution(List<CampusClue> clues) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (CampusClue clue : clues) {
            String platform = StringUtils.defaultIfBlank(clue.getSourcePlatform(), "其它");
            counts.merge(platform, 1, Integer::sum);
        }
        return toSortedNameValueList(counts, 10);
    }

    private List<Map<String, Object>> buildSentimentDistribution(List<CampusClue> clues) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (CampusClue clue : clues) {
            counts.merge(sentimentLabel(clue.getSentiment()), 1, Integer::sum);
        }
        return toSortedNameValueList(counts, 10);
    }

    private List<Map<String, Object>> toSortedNameValueList(Map<String, Integer> counts, int limit) {
        return counts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(limit)
                .map(e -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("name", e.getKey());
                    m.put("value", e.getValue());
                    return m;
                })
                .collect(Collectors.toList());
    }

    private String sentimentLabel(String sentiment) {
        if ("positive".equals(sentiment) || StringUtils.contains(sentiment, "正")) {
            return "正面";
        }
        if ("neutral".equals(sentiment) || StringUtils.contains(sentiment, "中")) {
            return "中性";
        }
        if ("negative".equals(sentiment) || StringUtils.contains(sentiment, "负")) {
            return "负面";
        }
        return "未知";
    }

    /**
     * 构建热词 TOP 20，严格使用同一个报告 scope 下的线索。
     */
    private List<Map<String, Object>> buildHotKeywords(List<CampusClue> clues) {
        Map<String, Integer> freqMap = new LinkedHashMap<>();
        for (CampusClue clue : clues) {
            String kwStr = clue.getKeywords();
            if (StringUtils.isBlank(kwStr)) {
                continue;
            }
            String[] parts = kwStr.split("[,，;；、\\s]+");
            for (String part : parts) {
                String trimmed = part.trim();
                if (trimmed.isEmpty()) {
                    continue;
                }
                freqMap.merge(trimmed, 1, Integer::sum);
            }
        }

        // 排序并取 TOP 20
        return freqMap.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(20)
                .map(e -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("keyword", e.getKey());
                    m.put("count", e.getValue());
                    return m;
                })
                .collect(Collectors.toList());
    }

    /**
     * 构建热点文章 TOP 10
     * 按 discover_time 倒序，取前 10 条
     */
    private List<Map<String, Object>> buildHotArticles(List<CampusClue> clues) {
        if (clues == null || clues.isEmpty()) {
            return Collections.emptyList();
        }

        int limit = Math.min(clues.size(), 10);
        List<Map<String, Object>> result = new ArrayList<>(limit);
        for (int i = 0; i < limit; i++) {
            CampusClue clue = clues.get(i);
            Map<String, Object> m = new HashMap<>();
            m.put("title", StringUtils.defaultString(clue.getClueTitle()));
            m.put("platform", StringUtils.defaultString(clue.getSourcePlatform()));
            m.put("url", StringUtils.defaultString(clue.getOriginalUrl()));
            // 情感映射为中文
            String sentiment = clue.getSentiment();
            if ("positive".equals(sentiment)) {
                m.put("sentiment", "正面");
            } else if ("neutral".equals(sentiment)) {
                m.put("sentiment", "中性");
            } else if ("negative".equals(sentiment)) {
                m.put("sentiment", "负面");
            } else {
                m.put("sentiment", StringUtils.defaultString(sentiment));
            }
            result.add(m);
        }
        return result;
    }

    /**
     * 自动生成概览摘要文本
     */
    private String buildSummary(ReportDataVO vo) {
        int total = vo.getTotalCount() != null ? vo.getTotalCount() : 0;
        int neg = vo.getNegativeCount() != null ? vo.getNegativeCount() : 0;
        int neu = vo.getNeutralCount() != null ? vo.getNeutralCount() : 0;
        int pos = vo.getPositiveCount() != null ? vo.getPositiveCount() : 0;

        StringBuilder platforms = new StringBuilder();
        List<Map<String, Object>> dist = vo.getMediaDistribution();
        if (dist != null && !dist.isEmpty()) {
            int count = 0;
            for (Map<String, Object> item : dist) {
                if (count >= 5) break;
                Object name = item.get("name");
                if (name != null) {
                    if (platforms.length() > 0) {
                        platforms.append("、");
                    }
                    platforms.append(name.toString());
                    count++;
                }
            }
        }
        if (platforms.length() == 0) {
            platforms.append("多种渠道");
        }

        return "监测周期内共监测到相关舆情" + total + "篇，其中负面" + neg + "篇、中性"
                + neu + "篇、正面" + pos + "篇。主要来源为" + platforms + "。";
    }
}
