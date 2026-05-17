package com.stonedt.intelligence.service.impl.campus;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.stonedt.intelligence.entity.campus.CampusAiPromptTemplate;
import com.stonedt.intelligence.service.campus.ai.CampusAiChatRequest;
import com.stonedt.intelligence.service.campus.ai.CampusAiChatResponse;
import com.stonedt.intelligence.service.campus.ai.CampusAiChatService;
import com.stonedt.intelligence.service.campus.ai.CampusAiKeywordService;
import com.stonedt.intelligence.service.campus.ai.CampusAiRuntimeService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CampusAiKeywordServiceImpl implements CampusAiKeywordService {

    private static final Logger log = LoggerFactory.getLogger(CampusAiKeywordServiceImpl.class);
    private static final String FEATURE_WORD_CLOUD = "word_cloud_extract";

    private final CampusAiRuntimeService campusAiRuntimeService;
    private final CampusAiChatService campusAiChatService;

    public CampusAiKeywordServiceImpl(CampusAiRuntimeService campusAiRuntimeService,
                                      CampusAiChatService campusAiChatService) {
        this.campusAiRuntimeService = campusAiRuntimeService;
        this.campusAiChatService = campusAiChatService;
    }

    @Override
    public List<Map<String, Object>> extractWordCloud(List<String> texts,
                                                      List<Map<String, Object>> fallback,
                                                      int limit) {
        List<Map<String, Object>> safeFallback = fallback == null ? new ArrayList<Map<String, Object>>() : fallback;
        if (!campusAiRuntimeService.isFeatureEnabled(FEATURE_WORD_CLOUD, true)) {
            return safeFallback;
        }
        String text = buildText(texts);
        if (StringUtils.isBlank(text)) {
            return safeFallback;
        }
        try {
            CampusAiPromptTemplate template = campusAiRuntimeService.getActivePrompt(FEATURE_WORD_CLOUD);
            String systemPrompt = StringUtils.defaultIfBlank(template == null ? null : template.getSystemPrompt(),
                    "你是舆情热词提取助手。请只提取能代表话题的短词，过滤虚词、泛词、平台词和无意义词。");
            String userPrompt = StringUtils.defaultIfBlank(template == null ? null : template.getUserPrompt(),
                    "请从以下校园舆情文本中提取最多 30 个中文热词，并给出权重。文本：${text}");
            userPrompt = userPrompt.replace("${text}", text);
            if (StringUtils.isNotBlank(template == null ? null : template.getOutputFormat())) {
                userPrompt = userPrompt + "\n输出格式：" + template.getOutputFormat();
            }

            CampusAiChatRequest request = new CampusAiChatRequest();
            request.setFeatureCode(FEATURE_WORD_CLOUD);
            request.setSystemPrompt(systemPrompt);
            request.setUserPrompt(userPrompt);
            request.setMaxTokens(2048);
            request.setTemperature(new BigDecimal("0.20"));
            CampusAiChatResponse response = campusAiChatService.chat(request);
            List<Map<String, Object>> parsed = parseWordCloud(response == null ? null : response.getContent(), limit);
            return parsed.isEmpty() ? safeFallback : parsed;
        } catch (Exception ex) {
            log.warn("AI word cloud extraction failed, fallback to keyword statistics: {}", ex.getMessage());
            return safeFallback;
        }
    }

    private String buildText(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String text : texts) {
            if (StringUtils.isBlank(text)) {
                continue;
            }
            if (sb.length() >= 6000) {
                break;
            }
            sb.append(StringUtils.left(text.replaceAll("\\s+", " ").trim(), 300)).append('\n');
        }
        return StringUtils.left(sb.toString(), 6000);
    }

    private List<Map<String, Object>> parseWordCloud(String content, int limit) {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        if (StringUtils.isBlank(content)) {
            return result;
        }
        String json = cleanupJson(content);
        try {
            JSONArray array = JSON.parseArray(json);
            Map<String, Integer> merged = new LinkedHashMap<String, Integer>();
            for (int i = 0; i < array.size(); i++) {
                JSONObject item = array.getJSONObject(i);
                if (item == null) {
                    continue;
                }
                String name = StringUtils.trimToNull(item.getString("name"));
                Integer value = item.getInteger("value");
                if (name == null || name.length() > 16) {
                    continue;
                }
                merged.put(name, merged.getOrDefault(name, 0) + Math.max(value == null ? 1 : value, 1));
            }
            int max = limit <= 0 ? 30 : limit;
            for (Map.Entry<String, Integer> entry : merged.entrySet()) {
                if (result.size() >= max) {
                    break;
                }
                Map<String, Object> row = new LinkedHashMap<String, Object>();
                row.put("name", entry.getKey());
                row.put("value", entry.getValue());
                result.add(row);
            }
        } catch (Exception ex) {
            log.warn("Failed to parse AI word cloud response: {}", ex.getMessage());
        }
        return result;
    }

    private String cleanupJson(String content) {
        String json = content.trim();
        if (json.startsWith("```")) {
            int start = json.indexOf('\n');
            if (start >= 0 && start < json.length() - 1) {
                json = json.substring(start + 1);
            }
            int end = json.lastIndexOf("```");
            if (end > 0) {
                json = json.substring(0, end);
            }
        }
        json = json.trim();
        if (!json.startsWith("[")) {
            int start = json.indexOf('[');
            int end = json.lastIndexOf(']');
            if (start >= 0 && end > start) {
                json = json.substring(start, end + 1);
            }
        }
        return json;
    }
}
