package com.stonedt.intelligence.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 报告聚合数据 VO - 承载报告生成所需的全部数据维度
 */
@Data
public class ReportDataVO {

    /** 每日舆情走势数据 (date, count) */
    private List<Map<String, Object>> trend;

    /** 平台分布 (name, value) */
    private List<Map<String, Object>> mediaDistribution;

    /** 情感分布 (name, value): 正面/中性/负面 */
    private List<Map<String, Object>> sentimentDistribution;

    /** 热词 TOP 20 (keyword, count) */
    private List<Map<String, Object>> hotKeywords;

    /** 热点文章 TOP 10 (title, platform, url, sentiment) */
    private List<Map<String, Object>> hotArticles;

    /** 平台排名 TOP 10 (name, value) */
    private List<Map<String, Object>> platformRanking;

    /** 概览摘要文本 */
    private String summary;

    /** 监测周期内文章总数 */
    private Integer totalCount;

    /** 负面文章数 */
    private Integer negativeCount;

    /** 中性文章数 */
    private Integer neutralCount;

    /** 正面文章数 */
    private Integer positiveCount;

    /** 监测周期开始日期字符串 (yyyy-MM-dd) */
    private String periodStart;

    /** 监测周期结束日期字符串 (yyyy-MM-dd) */
    private String periodEnd;
}
