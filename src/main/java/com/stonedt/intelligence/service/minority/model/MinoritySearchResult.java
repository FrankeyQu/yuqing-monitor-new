package com.stonedt.intelligence.service.minority.model;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * 蒙语/维语单条搜索结果
 * 包含原始搜索结果和 LLM 分析后的增强字段
 */
public class MinoritySearchResult {

    /** 标题（原文） */
    private String title;

    /** 摘要（原文） */
    private String snippet;

    /** 原文链接 */
    private String url;

    /** 来源网站 */
    private String source;

    /** 发布日期 */
    private String publishDate;

    /** 搜索引擎来源：baidu / bing */
    private String engine;

    /** 检测到的语言：mongolian / uyghur */
    private String language;

    /* ---- LLM 分析增强字段 ---- */

    /** 情感：positive / negative / neutral */
    private String sentiment;

    /** 主题标签 */
    private String topic;

    /** 中文摘要（50字以内） */
    private String summary;

    /** 重复标记：-1 表示无重复，>0 表示与第几条重复 */
    @JSONField(name = "duplicate_of")
    private Integer duplicateOf = -1;

    // Getters & Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSnippet() { return snippet; }
    public void setSnippet(String snippet) { this.snippet = snippet; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getPublishDate() { return publishDate; }
    public void setPublishDate(String publishDate) { this.publishDate = publishDate; }

    public String getEngine() { return engine; }
    public void setEngine(String engine) { this.engine = engine; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getSentiment() { return sentiment; }
    public void setSentiment(String sentiment) { this.sentiment = sentiment; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public Integer getDuplicateOf() { return duplicateOf; }
    public void setDuplicateOf(Integer duplicateOf) { this.duplicateOf = duplicateOf; }
}
