package com.stonedt.intelligence.service.minority.model;

import java.util.List;
import java.util.Map;

/**
 * 蒙语/维语舆情统计聚合数据
 */
public class MinorityStatistics {

    /** 情感分布：{positive: N, negative: N, neutral: N} */
    private Map<String, Integer> sentimentDist;

    /** 主题分布：{topicName: count, ...} */
    private Map<String, Integer> topicDist;

    /** 来源分布：{sourceName: count, ...} */
    private Map<String, Integer> sourceDist;

    /** 高频词 Top 20 */
    private List<String> topWords;

    /** 时间趋势：[{date: "2025-01-01", count: N}, ...] */
    private List<Map<String, Object>> timeTrend;

    public Map<String, Integer> getSentimentDist() { return sentimentDist; }
    public void setSentimentDist(Map<String, Integer> sentimentDist) { this.sentimentDist = sentimentDist; }

    public Map<String, Integer> getTopicDist() { return topicDist; }
    public void setTopicDist(Map<String, Integer> topicDist) { this.topicDist = topicDist; }

    public Map<String, Integer> getSourceDist() { return sourceDist; }
    public void setSourceDist(Map<String, Integer> sourceDist) { this.sourceDist = sourceDist; }

    public List<String> getTopWords() { return topWords; }
    public void setTopWords(List<String> topWords) { this.topWords = topWords; }

    public List<Map<String, Object>> getTimeTrend() { return timeTrend; }
    public void setTimeTrend(List<Map<String, Object>> timeTrend) { this.timeTrend = timeTrend; }
}
