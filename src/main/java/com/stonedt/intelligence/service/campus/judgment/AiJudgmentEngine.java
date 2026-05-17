package com.stonedt.intelligence.service.campus.judgment;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.stonedt.intelligence.entity.campus.CampusClue;
import com.stonedt.intelligence.service.campus.ai.CampusAiChatRequest;
import com.stonedt.intelligence.service.campus.ai.CampusAiChatResponse;
import com.stonedt.intelligence.service.campus.ai.CampusAiChatService;
import com.stonedt.intelligence.service.campus.support.CampusSentimentNormalizer;
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

/**
 * Minority language AI judgment engine using DeepSeek LLM.
 * <p>
 * Handles judgment for Mongolian and Uyghur language clues by calling
 * the DeepSeek Chat API. The LLM translates content to Chinese,
 * classifies sentiment/topic/risk, and generates a summary.
 * </p>
 * <p>
 * API call pattern follows {@code MinorityLLMAnalyzer.callDeepSeek()}
 * -- same timeouts, same auth header, same non-streaming approach.
 * </p>
 */
@Component
public class AiJudgmentEngine {

    private static final Logger log = LoggerFactory.getLogger(AiJudgmentEngine.class);

    private static final int CONNECT_TIMEOUT_MS = 60_000;
    private static final int READ_TIMEOUT_MS = 180_000;

    @Value("${deepseek.api.url:https://api.deepseek.com/v1/chat/completions}")
    private String apiUrl;

    @Value("${deepseek.api.key:}")
    private String apiKey;

    private final CampusAiChatService campusAiChatService;

    public AiJudgmentEngine(CampusAiChatService campusAiChatService) {
        this.campusAiChatService = campusAiChatService;
    }

    /**
     * Sentiment mapping: AI output -> canonical storage value.
     */
    private static String mapSentiment(String raw) {
        return CampusSentimentNormalizer.normalizeOrDefault(raw, "neutral");
    }

    /**
     * Judge a single minority-language clue using DeepSeek AI.
     * <p>
     * If the API key is not configured or the call fails, the clue is
     * left unchanged (clueStatus stays "pending_judge").
     * Errors are logged but never thrown.
     * </p>
     *
     * @param clue the clue to judge (must not be null; already persisted)
     */
    public void judge(CampusClue clue) {
        // 1. Build system prompt
        String systemPrompt = "你是一个少数民族语言舆情分析专家。请分析以下内容，返回JSON格式结果。";

        // 2. Build user prompt with title, content, and output format instructions
        String userPrompt = buildUserPrompt(clue);

        // 3. Call configured LLM provider
        String responseContent = callDeepSeek(systemPrompt, userPrompt);
        if (StringUtils.isBlank(responseContent)) {
            log.warn("DeepSeek returned empty response for clue {}, AI judgment skipped",
                    clue.getClueId());
            return;
        }

        // 5. Parse the JSON response
        JSONObject analysis = parseAiResponse(responseContent);
        if (analysis == null) {
            log.warn("Failed to parse AI response for clue {}, AI judgment skipped",
                    clue.getClueId());
            return;
        }

        // 6. Apply analysis results to clue
        applyAnalysis(clue, analysis);

        log.info("AI judgment completed for clue {}: riskLevel={}, sentiment={}",
                clue.getClueId(), clue.getRiskLevel(), clue.getSentiment());
    }

    // ==================== Prompt building ====================

    /**
     * Build the user prompt with the original-language title and content,
     * plus structured output format instructions.
     */
    private String buildUserPrompt(CampusClue clue) {
        StringBuilder sb = new StringBuilder();
        sb.append("标题：").append(StringUtils.defaultString(clue.getClueTitle())).append("\n");
        sb.append("内容：").append(StringUtils.defaultString(clue.getClueContent())).append("\n\n");
        sb.append("请翻译标题和内容为中文，判断情感倾向(positive/negative/neutral)，");
        sb.append("判断主题分类(教育/民生/文化/经济/政治/其他)，");
        sb.append("评估风险等级(urgent/major/concern/normal)，");
        sb.append("给出风险理由(50字以内)，生成中文摘要(50字以内)。");
        sb.append("只返回JSON，格式：\n");
        sb.append("{\"translatedTitle\":\"...\",\"translatedContent\":\"...\",");
        sb.append("\"sentiment\":\"...\",\"topic\":\"...\",");
        sb.append("\"riskLevel\":\"...\",\"riskReason\":\"...\",\"summary\":\"...\"}");
        return sb.toString();
    }

    // ==================== DeepSeek API call ====================

    /**
     * Call the DeepSeek Chat API with the given prompts.
     * <p>
     * Follows the exact same pattern as
     * {@code MinorityLLMAnalyzer.callDeepSeek()}: JDK HttpURLConnection,
     * non-streaming, low temperature for stable output format.
     * </p>
     *
     * @param systemPrompt system role prompt
     * @param userPrompt   user role prompt
     * @return the assistant's text content, or null on failure
     */
    private String callDeepSeek(String systemPrompt, String userPrompt) {
        try {
            CampusAiChatRequest request = new CampusAiChatRequest();
            request.setFeatureCode("minority_judgment");
            request.setSystemPrompt(systemPrompt);
            request.setUserPrompt(userPrompt);
            request.setMaxTokens(2048);
            request.setTemperature(new BigDecimal("0.10"));
            CampusAiChatResponse response = campusAiChatService.chat(request);
            return response == null ? null : response.getContent();
        } catch (Exception e) {
            log.error("Failed to call DeepSeek API for AI judgment", e);
            return null;
        }
    }

    // ==================== Response parsing ====================

    /**
     * Robustly parse the AI response text as a JSON object.
     * <p>
     * Supports:
     * <ul>
     *   <li>Pure JSON object: {@code {"translatedTitle":"..."}}</li>
     *   <li>Markdown code block: {@code ```json\n{...}\n```}</li>
     *   <li>Extra text before/after -- auto-extracts the JSON object</li>
     * </ul>
     * </p>
     *
     * @param content the raw text returned by the LLM
     * @return parsed JSONObject, or null on failure
     */
    private JSONObject parseAiResponse(String content) {
        if (StringUtils.isBlank(content)) {
            return null;
        }

        String jsonStr = content.trim();

        // 1. Strip Markdown code fences
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

        // 2. Extract the JSON object if extra text surrounds it
        if (!jsonStr.startsWith("{")) {
            int braceStart = jsonStr.indexOf('{');
            int braceEnd = jsonStr.lastIndexOf('}');
            if (braceStart >= 0 && braceEnd > braceStart) {
                jsonStr = jsonStr.substring(braceStart, braceEnd + 1);
            }
        }

        // 3. Parse
        try {
            return JSON.parseObject(jsonStr);
        } catch (Exception e) {
            log.warn("Failed to parse AI response as JSON: {}", e.getMessage());
            return null;
        }
    }

    // ==================== Apply analysis to clue ====================

    /**
     * Populate the clue entity fields from the parsed AI analysis.
     */
    private void applyAnalysis(CampusClue clue, JSONObject analysis) {
        // Risk level
        String riskLevel = analysis.getString("riskLevel");
        if (StringUtils.isNotBlank(riskLevel)) {
            clue.setRiskLevel(riskLevel.trim().toLowerCase());
        }

        // Sentiment (canonical storage value)
        String sentiment = analysis.getString("sentiment");
        clue.setSentiment(mapSentiment(sentiment));

        // Build judgeOpinion from AI analysis fields
        String topic = analysis.getString("topic");
        String riskReason = analysis.getString("riskReason");
        String summary = analysis.getString("summary");
        String translatedContent = analysis.getString("translatedContent");
        String translatedTitle = analysis.getString("translatedTitle");

        clue.setJudgeOpinion(buildAiOpinion(topic, riskReason, summary,
                translatedTitle, translatedContent));
    }

    /**
     * Build {@code judgeOpinion} string from AI analysis result fields.
     * Formatted as structured text for human readability.
     */
    private String buildAiOpinion(String topic, String riskReason, String summary,
                                   String translatedTitle, String translatedContent) {
        StringBuilder sb = new StringBuilder();
        sb.append("[AI研判]");
        if (StringUtils.isNotBlank(topic)) {
            sb.append(" 主题: ").append(topic).append(";");
        }
        if (StringUtils.isNotBlank(riskReason)) {
            sb.append(" 风险理由: ").append(riskReason).append(";");
        }
        if (StringUtils.isNotBlank(summary)) {
            sb.append(" 摘要: ").append(summary).append(";");
        }
        if (StringUtils.isNotBlank(translatedTitle)) {
            sb.append(" 中文标题: ").append(translatedTitle).append(";");
        }
        if (StringUtils.isNotBlank(translatedContent)) {
            sb.append(" 中文翻译: ").append(translatedContent).append(";");
        }
        return sb.toString();
    }

    // ==================== Utility ====================

    /**
     * Read all text from an input stream, returning empty string on null.
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
}
