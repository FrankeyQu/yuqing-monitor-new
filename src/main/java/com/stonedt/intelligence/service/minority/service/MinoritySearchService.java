package com.stonedt.intelligence.service.minority.service;

import com.stonedt.intelligence.service.minority.model.MinoritySearchParam;

import java.util.Map;

/**
 * 蒙语/维语搜索服务接口
 */
public interface MinoritySearchService {

    /**
     * 执行搜索 + AI分析 + 统计
     *
     * @param param 搜索请求参数（keyword, engine, page, analyze）
     * @return Map 包含 keyword, detectedLang, engine, total, list, statistics
     */
    Map<String, Object> search(MinoritySearchParam param);
}
