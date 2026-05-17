package com.stonedt.intelligence.service.minority.model;

/**
 * 蒙语/维语搜索请求参数
 */
public class MinoritySearchParam {

    /** 搜索关键词（蒙语或维语原文） */
    private String keyword;

    /** 搜索引擎：baidu / bing / all（默认 all） */
    private String engine = "all";

    /** 页码（默认 1） */
    private Integer page = 1;

    /** 是否启用 LLM 分析（默认 true） */
    private Boolean analyze = true;

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }

    public String getEngine() { return engine; }
    public void setEngine(String engine) { this.engine = engine; }

    public Integer getPage() { return page; }
    public void setPage(Integer page) { this.page = page; }

    public Boolean getAnalyze() { return analyze; }
    public void setAnalyze(Boolean analyze) { this.analyze = analyze; }
}
