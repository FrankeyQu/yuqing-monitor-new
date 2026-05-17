package com.stonedt.intelligence.service.minority.search;

import com.stonedt.intelligence.service.minority.model.MinoritySearchResult;

import java.util.List;

/**
 * 搜索引擎适配器接口
 * 每种搜索引擎（百度、必应等）实现此接口
 */
public interface SearchEngineAdapter {

    /**
     * 执行搜索
     * @param keyword 搜索关键词
     * @param page    页码（从 1 开始）
     * @return 搜索结果列表
     */
    List<MinoritySearchResult> search(String keyword, int page);

    /**
     * 获取搜索引擎名称
     */
    String getEngineName();
}
