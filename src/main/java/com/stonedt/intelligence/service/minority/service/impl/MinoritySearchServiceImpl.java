package com.stonedt.intelligence.service.minority.service.impl;

import com.stonedt.intelligence.service.minority.analyze.MinorityLLMAnalyzer;
import com.stonedt.intelligence.service.minority.model.MinoritySearchParam;
import com.stonedt.intelligence.service.minority.model.MinoritySearchResult;
import com.stonedt.intelligence.service.minority.model.MinorityStatistics;
import com.stonedt.intelligence.service.minority.search.SearchEngineAdapter;
import com.stonedt.intelligence.service.minority.service.MinoritySearchService;
import com.stonedt.intelligence.service.minority.util.MinorityLanguageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 蒙语/维语搜索服务实现
 * 负责：引擎分发 -> 结果聚合 -> 语言检测 -> LLM 分析 -> 统计计算
 */
@Service
public class MinoritySearchServiceImpl implements MinoritySearchService {

    private static final Logger log = LoggerFactory.getLogger(MinoritySearchServiceImpl.class);

    @Autowired
    private List<SearchEngineAdapter> searchEngines;

    @Autowired(required = false)
    private MinorityLLMAnalyzer llmAnalyzer;

    /**
     * 引擎过滤辅助：根据 engine 参数筛选目标适配器
     */
    private List<SearchEngineAdapter> resolveEngines(String engine) {
        if ("all".equalsIgnoreCase(engine)) {
            return searchEngines;
        }
        return searchEngines.stream()
                .filter(adapter -> adapter.getEngineName().equalsIgnoreCase(engine))
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> search(MinoritySearchParam param) {
        try {
            String keyword = param.getKeyword();
            int page = param.getPage() != null ? param.getPage() : 1;
            String engine = param.getEngine() != null ? param.getEngine() : "all";

            // 1. 筛选搜索引擎并并发搜索
            List<SearchEngineAdapter> targets = resolveEngines(engine);
            List<MinoritySearchResult> allResults = new ArrayList<>();

            for (SearchEngineAdapter adapter : targets) {
                try {
                    List<MinoritySearchResult> results = adapter.search(keyword, page);
                    if (results != null) {
                        // 标记结果来源
                        for (MinoritySearchResult r : results) {
                            r.setEngine(adapter.getEngineName());
                        }
                        allResults.addAll(results);
                    }
                } catch (Exception e) {
                    log.warn("引擎 [{}] 搜索异常: {}", adapter.getEngineName(), e.getMessage());
                }
            }

            // 2. 检测语言
            String detectedLang = "unknown";
            if (!allResults.isEmpty()) {
                String firstTitle = allResults.get(0).getTitle();
                detectedLang = MinorityLanguageUtil.detect(firstTitle);
                // 对所有结果标注语言
                for (MinoritySearchResult r : allResults) {
                    if (r.getLanguage() == null) {
                        r.setLanguage(detectedLang);
                    }
                }
            }

            // 3. LLM 分析（可选）
            List<MinoritySearchResult> analyzedResults = allResults;
            Boolean analyze = param.getAnalyze() != null ? param.getAnalyze() : true;
            if (analyze && llmAnalyzer != null) {
                try {
                    analyzedResults = llmAnalyzer.analyze(allResults);
                } catch (Exception e) {
                    log.warn("LLM 分析异常，退回原始结果: {}", e.getMessage());
                    analyzedResults = allResults;
                }
            }

            // 4. 统计计算
            MinorityStatistics statistics = null;
            if (llmAnalyzer != null) {
                try {
                    statistics = llmAnalyzer.computeStatistics(analyzedResults);
                } catch (Exception e) {
                    log.warn("LLM 统计计算异常: {}", e.getMessage());
                }
            }

            // 5. 组装返回
            Map<String, Object> resultMap = new LinkedHashMap<>();
            resultMap.put("keyword", keyword);
            resultMap.put("detectedLang", detectedLang);
            resultMap.put("engine", engine);
            resultMap.put("total", analyzedResults.size());
            resultMap.put("list", analyzedResults);
            if (statistics != null) {
                resultMap.put("statistics", statistics);
            }
            return resultMap;

        } catch (Exception e) {
            log.error("少数民族搜索服务异常: {}", e.getMessage(), e);
            Map<String, Object> errorMap = new LinkedHashMap<>();
            errorMap.put("errorMessage", "搜索服务异常: " + e.getMessage());
            errorMap.put("keyword", param.getKeyword());
            errorMap.put("engine", param.getEngine());
            return errorMap;
        }
    }
}
