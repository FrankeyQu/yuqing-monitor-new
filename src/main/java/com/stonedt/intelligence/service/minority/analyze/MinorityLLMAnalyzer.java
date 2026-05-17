package com.stonedt.intelligence.service.minority.analyze;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.stonedt.intelligence.service.campus.ai.CampusAiChatRequest;
import com.stonedt.intelligence.service.campus.ai.CampusAiChatResponse;
import com.stonedt.intelligence.service.campus.ai.CampusAiChatService;
import com.stonedt.intelligence.service.minority.model.MinoritySearchResult;
import com.stonedt.intelligence.service.minority.model.MinorityStatistics;
import com.stonedt.intelligence.service.minority.util.MinorityLanguageUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 少数民族舆情 LLM 分析器。
 * <p>
 * 调用 DeepSeek API 对搜索结果进行多语言（蒙语/维语/中文）情感分析、主题分类、
 * 中文摘要生成和重复检测，并提供纯内存统计聚合。
 * </p>
 */
@Component
public class MinorityLLMAnalyzer {

    private static final Logger log = LoggerFactory.getLogger(MinorityLLMAnalyzer.class);

    private static final int CONNECT_TIMEOUT_MS = 60_000;
    private static final int READ_TIMEOUT_MS = 180_000;

    @Value("${deepseek.api.url:https://api.deepseek.com/v1/chat/completions}")
    private String apiUrl;

    @Value("${deepseek.api.key:}")
    private String apiKey;

    private final CampusAiChatService campusAiChatService;

    public MinorityLLMAnalyzer(CampusAiChatService campusAiChatService) {
        this.campusAiChatService = campusAiChatService;
    }

    // ==================== 对外公开方法 ====================

    /**
     * 调用 DeepSeek API 对搜索结果进行 LLM 分析增强。
     * <p>
     * 逐条填充以下字段：{@code sentiment}（情感分类）、{@code topic}（主题标签）、
     * {@code summary}（中文摘要，不超过 50 字）、{@code duplicateOf}（重复标记）。
     * </p>
     *
     * @param rawResults 原始搜索结果列表
     * @return 增强后的结果列表（与原列表为同一引用，字段已填充）；
     *         若 API 调用失败或未配置 key，则直接返回原列表不变
     */
    public List<MinoritySearchResult> analyze(List<MinoritySearchResult> rawResults) {
        if (rawResults == null || rawResults.isEmpty()) {
            return rawResults;
        }
        String systemPrompt = "你是一个精通蒙语、维语和中文的多语言舆情分析专家。"
                + "请分析每条搜索结果，输出 JSON 数组。语言要求：summary 字段用中文。";

        String userPrompt = buildUserPrompt(rawResults);

        try {
            String responseContent = callDeepSeek(systemPrompt, userPrompt);
            if (StringUtils.isBlank(responseContent)) {
                log.warn("DeepSeek returned empty response, returning original results");
                return rawResults;
            }

            JSONArray analysisArray = parseLLMResponse(responseContent);
            if (analysisArray == null || analysisArray.isEmpty()) {
                log.warn("Failed to parse LLM response as JSON array, returning original results");
                return rawResults;
            }

            applyAnalysisToResults(rawResults, analysisArray);
            log.info("LLM analysis completed for {} results", rawResults.size());

        } catch (Exception e) {
            log.error("LLM analysis failed unexpectedly, returning original results", e);
        }

        return rawResults;
    }

    /**
     * 在内存中对已分析结果进行统计聚合。
     * <p>
     * 纯内存计算，不涉及数据库或外部服务。
     * </p>
     *
     * @param analyzedResults LLM 分析后的结果列表
     * @return 统计聚合数据（情感分布 / 主题分布 / 来源分布 / 高频词 Top20 / 时间趋势）
     */
    public MinorityStatistics computeStatistics(List<MinoritySearchResult> analyzedResults) {
        MinorityStatistics stats = new MinorityStatistics();

        if (analyzedResults == null || analyzedResults.isEmpty()) {
            stats.setSentimentDist(new LinkedHashMap<String, Integer>());
            stats.setTopicDist(new LinkedHashMap<String, Integer>());
            stats.setSourceDist(new LinkedHashMap<String, Integer>());
            stats.setTopWords(new ArrayList<String>());
            stats.setTimeTrend(new ArrayList<Map<String, Object>>());
            return stats;
        }

        // 1. 情感分布
        stats.setSentimentDist(computeSentimentDist(analyzedResults));

        // 2. 主题分布
        stats.setTopicDist(computeTopicDist(analyzedResults));

        // 3. 来源分布（从 URL 提取域名）
        stats.setSourceDist(computeSourceDist(analyzedResults));

        // 4. 高频词 Top20
        stats.setTopWords(computeTopWords(analyzedResults));

        // 5. 时间趋势
        stats.setTimeTrend(computeTimeTrend(analyzedResults));

        return stats;
    }

    // ==================== Prompt 构建 ====================

    /**
     * 构建 user prompt，将搜索结果格式化为带序号的文本列表，
     * 明确要求 LLM 按 JSON 数组格式返回分析结果。
     */
    private String buildUserPrompt(List<MinoritySearchResult> results) {
        StringBuilder sb = new StringBuilder();
        sb.append("请分析以下搜索结果，对每条结果输出：\n")
                .append("- sentiment: positive / negative / neutral（情感分类）\n")
                .append("- topic: 教育 / 民生 / 文化 / 经济 / 其他（主题标签）\n")
                .append("- summary: 50字以内中文摘要\n")
                .append("- duplicateOf: -1 表示无重复，>0 表示与第几条重复\n\n")
                .append("请严格按以下 JSON 数组格式返回，不要包含任何其他内容：\n")
                .append("[\n")
                .append("  {\"index\": 0, \"sentiment\": \"...\", \"topic\": \"...\", \"summary\": \"...\", \"duplicateOf\": -1},\n")
                .append("  ...\n")
                .append("]\n\n")
                .append("搜索结果如下：\n");

        for (int i = 0; i < results.size(); i++) {
            MinoritySearchResult r = results.get(i);
            sb.append("---\n")
                    .append("index: ").append(i).append("\n")
                    .append("title: ").append(StringUtils.defaultString(r.getTitle())).append("\n")
                    .append("snippet: ").append(StringUtils.defaultString(r.getSnippet())).append("\n")
                    .append("url: ").append(StringUtils.defaultString(r.getUrl())).append("\n");
        }

        return sb.toString();
    }

    // ==================== DeepSeek API 调用 ====================

    /**
     * 向 DeepSeek API 发送非流式请求并返回响应文本。
     * <p>
     * 参考 {@code AiReportServiceImpl.callNonStreamingApi} 的实现模式。
     * 低温度（0.1）保证输出格式稳定。
     * </p>
     *
     * @param systemPrompt system 角色 prompt
     * @param userPrompt   user 角色 prompt
     * @return API 返回的 content 文本，异常时返回 null
     */
    private String callDeepSeek(String systemPrompt, String userPrompt) {
        try {
            CampusAiChatRequest request = new CampusAiChatRequest();
            request.setFeatureCode("minority_judgment");
            request.setSystemPrompt(systemPrompt);
            request.setUserPrompt(userPrompt);
            request.setMaxTokens(4096);
            request.setTemperature(new BigDecimal("0.10"));
            CampusAiChatResponse response = campusAiChatService.chat(request);
            return response == null ? null : response.getContent();
        } catch (Exception e) {
            log.error("Failed to call DeepSeek API", e);
            return null;
        }
    }

    // ==================== LLM 响应解析 ====================

    /**
     * 鲁棒解析 LLM 返回的文本，尝试提取 JSON 数组。
     * <p>
     * 支持以下格式：
     * <ul>
     *   <li>纯 JSON 数组：{@code [{"index":0,...}]}</li>
     *   <li>Markdown 代码块包裹：{@code ```json\n[{"index":0,...}]\n```}</li>
     *   <li>额外文本前缀/后缀：自动定位首 {@code [} 和尾 {@code ]}</li>
     * </ul>
     * </p>
     *
     * @param content LLM 返回的原始文本
     * @return 解析成功的 JSONArray，解析失败返回 null
     */
    private JSONArray parseLLMResponse(String content) {
        if (StringUtils.isBlank(content)) {
            return null;
        }

        String jsonStr = content.trim();

        // 1. 去除 Markdown 代码块标记 (```json 和 ```)
        if (jsonStr.startsWith("```")) {
            int startIdx = jsonStr.indexOf('\n');
            if (startIdx > 0 && startIdx < jsonStr.length() - 1) {
                jsonStr = jsonStr.substring(startIdx + 1);
            }
            int endIdx = jsonStr.lastIndexOf("```");
            if (endIdx > 0) {
                jsonStr = jsonStr.substring(0, endIdx);
            }
            jsonStr = jsonStr.trim();
        }

        // 2. 尝试提取 [] 包围的 JSON 数组段（丢弃前置/后置文本）
        if (!jsonStr.startsWith("[")) {
            int bracketStart = jsonStr.indexOf('[');
            int bracketEnd = jsonStr.lastIndexOf(']');
            if (bracketStart >= 0 && bracketEnd > bracketStart) {
                jsonStr = jsonStr.substring(bracketStart, bracketEnd + 1);
            }
        }

        // 3. 正式解析
        try {
            return JSON.parseArray(jsonStr);
        } catch (Exception e) {
            log.warn("Failed to parse LLM response as JSON array after cleanup: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 将 LLM 返回的分析结果 JSON 数组应用到原始搜索结果上。
     * <p>
     * 通过 {@code index} 字段匹配，逐条赋值。
     * 缺失字段或非法索引时静默跳过。
     * </p>
     */
    private void applyAnalysisToResults(List<MinoritySearchResult> results, JSONArray analysisArray) {
        for (int i = 0; i < analysisArray.size(); i++) {
            JSONObject item = analysisArray.getJSONObject(i);
            if (item == null) {
                continue;
            }

            Integer idx = item.getInteger("index");
            if (idx == null || idx < 0 || idx >= results.size()) {
                continue;
            }

            MinoritySearchResult target = results.get(idx);

            // sentiment
            String sentiment = item.getString("sentiment");
            if (StringUtils.isNotBlank(sentiment)) {
                target.setSentiment(sentiment.trim().toLowerCase());
            }

            // topic
            String topic = item.getString("topic");
            if (StringUtils.isNotBlank(topic)) {
                target.setTopic(topic.trim());
            }

            // summary
            String summary = item.getString("summary");
            if (StringUtils.isNotBlank(summary)) {
                target.setSummary(summary.trim());
            }

            // duplicateOf
            Integer duplicateOf = item.getInteger("duplicateOf");
            if (duplicateOf != null) {
                target.setDuplicateOf(duplicateOf);
            }
        }
    }

    // ==================== 统计聚合（纯内存） ====================

    /**
     * 遍历统计 sentiment 各分类的数量。
     * 按首次出现顺序返回（LinkedHashMap）。
     */
    private Map<String, Integer> computeSentimentDist(List<MinoritySearchResult> results) {
        Map<String, Integer> dist = new LinkedHashMap<String, Integer>();
        for (MinoritySearchResult r : results) {
            String key = StringUtils.defaultString(r.getSentiment(), "unknown");
            dist.put(key, dist.getOrDefault(key, 0) + 1);
        }
        return dist;
    }

    /**
     * 遍历统计 topic 各分类的数量。
     * 按首次出现顺序返回（LinkedHashMap）。
     */
    private Map<String, Integer> computeTopicDist(List<MinoritySearchResult> results) {
        Map<String, Integer> dist = new LinkedHashMap<String, Integer>();
        for (MinoritySearchResult r : results) {
            String key = StringUtils.defaultString(r.getTopic(), "其他");
            dist.put(key, dist.getOrDefault(key, 0) + 1);
        }
        return dist;
    }

    /**
     * 从 URL 中提取域名并按域名统计来源分布。
     * 按首次出现顺序返回（LinkedHashMap）。
     */
    private Map<String, Integer> computeSourceDist(List<MinoritySearchResult> results) {
        Map<String, Integer> dist = new LinkedHashMap<String, Integer>();
        for (MinoritySearchResult r : results) {
            String source = extractDomain(r.getUrl());
            if (StringUtils.isBlank(source)) {
                source = "unknown";
            }
            dist.put(source, dist.getOrDefault(source, 0) + 1);
        }
        return dist;
    }

    /**
     * 收集所有 title + snippet 文本，委托 {@link MinorityLanguageUtil#extractTopWords} 获取 Top20。
     */
    private List<String> computeTopWords(List<MinoritySearchResult> results) {
        List<String> texts = new ArrayList<String>();
        for (MinoritySearchResult r : results) {
            if (StringUtils.isNotBlank(r.getTitle())) {
                texts.add(r.getTitle());
            }
            if (StringUtils.isNotBlank(r.getSnippet())) {
                texts.add(r.getSnippet());
            }
        }
        return MinorityLanguageUtil.extractTopWords(texts, 20);
    }

    /**
     * 按 publishDate 分组统计文章数量，按日期升序排列。
     * 使用 TreeMap 保证自然排序（文字日期格式如 "2025-01-01" 可正确排序）。
     */
    private List<Map<String, Object>> computeTimeTrend(List<MinoritySearchResult> results) {
        Map<String, Integer> dateCountMap = new TreeMap<String, Integer>();
        for (MinoritySearchResult r : results) {
            String date = StringUtils.defaultString(r.getPublishDate(), "unknown");
            dateCountMap.put(date, dateCountMap.getOrDefault(date, 0) + 1);
        }

        List<Map<String, Object>> trend = new ArrayList<Map<String, Object>>();
        for (Map.Entry<String, Integer> entry : dateCountMap.entrySet()) {
            Map<String, Object> point = new LinkedHashMap<String, Object>();
            point.put("date", entry.getKey());
            point.put("count", entry.getValue());
            trend.add(point);
        }
        return trend;
    }

    // ==================== 工具方法 ====================

    /**
     * 读取输入流中的全部文本。
     */
    private String readStream(InputStream stream) throws java.io.IOException {
        if (stream == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    /**
     * 从 URL 中提取域名（host）。
     * <p>
     * 优先使用 {@link URL#getHost()}，降级时手动解析 {@code http://} / {@code https://} 前缀。
     * 自动去除端口号部分。
     * </p>
     *
     * @param url 原始 URL 字符串
     * @return 域名，解析失败返回 {@code null}
     */
    private String extractDomain(String url) {
        if (StringUtils.isBlank(url)) {
            return null;
        }
        try {
            return new URL(url).getHost();
        } catch (Exception e) {
            // 非标准 URL，手动解析
            String trimmed = url.trim();
            if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
                int start = trimmed.indexOf("://") + 3;
                int end = trimmed.indexOf('/', start);
                if (end < 0) {
                    end = trimmed.length();
                }
                String host = url.substring(start, end);
                int portIdx = host.indexOf(':');
                if (portIdx > 0) {
                    host = host.substring(0, portIdx);
                }
                return host;
            }
            return null;
        }
    }
}
