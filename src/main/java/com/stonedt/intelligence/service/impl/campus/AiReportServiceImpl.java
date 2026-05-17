package com.stonedt.intelligence.service.impl.campus;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.stonedt.intelligence.service.campus.AiReportService;
import com.stonedt.intelligence.service.campus.ai.CampusAiChatRequest;
import com.stonedt.intelligence.service.campus.ai.CampusAiChatResponse;
import com.stonedt.intelligence.service.campus.ai.CampusAiChatService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.function.Consumer;

/**
 * AI report generation service implementation.
 * Calls the DeepSeek-v4-pro API (OpenAI-compatible chat completion endpoint).
 */
@Service
public class AiReportServiceImpl implements AiReportService {

    private static final Logger log = LoggerFactory.getLogger(AiReportServiceImpl.class);

    private final CampusAiChatService campusAiChatService;

    public AiReportServiceImpl(CampusAiChatService campusAiChatService) {
        this.campusAiChatService = campusAiChatService;
    }

    @Override
    public String generateReport(String reportType, String reportTitle, String dataJson,
                                  String periodStart, String periodEnd, StringBuilder streamOutput) {
        return generateReport(reportType, reportTitle, dataJson, periodStart, periodEnd, streamOutput, null);
    }

    @Override
    public String generateReport(String reportType, String reportTitle, String dataJson,
                                  String periodStart, String periodEnd, StringBuilder streamOutput,
                                  Consumer<String> chunkConsumer) {
        return generateReport(reportType, reportTitle, dataJson, periodStart, periodEnd, null, streamOutput, chunkConsumer);
    }

    @Override
    public String generateReport(String reportType, String reportTitle, String dataJson,
                                 String periodStart, String periodEnd, String aiUserPrompt,
                                 StringBuilder streamOutput, Consumer<String> chunkConsumer) {
        String prompt = buildPrompt(reportType, reportTitle, dataJson, periodStart, periodEnd, aiUserPrompt);
        log.info("AiReportService generating {} report, title: {}", reportType, reportTitle);

        boolean isStream = (streamOutput != null);

        try {
            CampusAiChatRequest request = new CampusAiChatRequest();
            request.setFeatureCode("report_generate");
            request.setSystemPrompt("你是一个专业的舆情分析师，擅长撰写舆情报告。请严格按照用户要求的格式输出markdown格式的报告内容。");
            request.setUserPrompt(prompt);
            request.setMaxTokens(4096);
            request.setTemperature(new BigDecimal("0.70"));
            request.setStream(isStream);
            CampusAiChatResponse response = isStream
                    ? campusAiChatService.chatStreaming(request, streamOutput, chunkConsumer)
                    : campusAiChatService.chat(request);
            return response == null ? "" : response.getContent();
        } catch (Exception e) {
            log.error("DeepSeek API call failed for {} report", reportType, e);
            throw new IllegalStateException("AI报告生成失败：" + e.getMessage(), e);
        }
    }

    private String buildPrompt(String reportType, String reportTitle, String dataJson,
                               String periodStart, String periodEnd, String aiUserPrompt) {
        String typeLabel;
        String typePrompt;

        switch (StringUtils.defaultString(reportType, "daily")) {
            case "daily":
                typeLabel = "舆情日报";
                typePrompt = "请根据以下舆情数据生成一份简洁的舆情日报。报告需包含：舆情概况、重点舆情事件、舆情走势分析、"
                        + "媒体关注度分析、情感分析、热点关键词、热点文章摘要和建议关注事项。使用markdown格式。";
                break;
            case "weekly":
                typeLabel = "舆情周报";
                typePrompt = "请根据以下舆情数据生成一份舆情周报。报告需包含：本周舆情概况、重点舆情事件回顾、"
                        + "本周舆情走势分析（对比上周变化趋势）、媒体关注度分析、情感分析、热点关键词、"
                        + "热点文章摘要和建议关注事项。重点体现本周与上周的对比变化。使用markdown格式。";
                break;
            case "monthly":
                typeLabel = "舆情月报";
                typePrompt = "请根据以下舆情数据生成一份全面的舆情月报。报告需包含：本月舆情总体概况、"
                        + "重点舆情事件详细分析、月度舆情走势分析、话题演变分析、媒体关注度变化分析、"
                        + "情感分析趋势、热点关键词汇总、热点文章摘要、本月舆情特点总结和下月舆情预测建议。"
                        + "报告应更加全面深入。使用markdown格式。";
                break;
            case "special":
                typeLabel = "舆情专报";
                typePrompt = "请根据以下舆情数据生成一份舆情专题深度分析报告。报告需包含：专题背景概述、"
                        + "事件发展脉络梳理、核心议题分析、各方观点汇总、媒体传播路径分析、社会影响评估、"
                        + "风险研判和处置建议。报告应聚焦专题进行深入分析。使用markdown格式。";
                break;
            default:
                typeLabel = "舆情报告";
                typePrompt = "请根据以下舆情数据生成一份舆情报告。报告需包含：舆情概况、重点舆情事件、"
                        + "舆情走势分析、媒体关注度分析、情感分析、热点关键词、热点文章摘要和建议关注事项。"
                        + "使用markdown格式。";
                break;
        }

        String profilePrompt = buildProfilePrompt(dataJson);
        return typePrompt + "\n\n"
                + profilePrompt + "\n\n"
                + buildUserPromptInstruction(aiUserPrompt) + "\n\n"
                + "报告标题：" + StringUtils.defaultString(reportTitle, typeLabel) + "\n"
                + "报告类型：" + typeLabel + "\n"
                + "统计周期：" + StringUtils.defaultString(periodStart, "") + " 至 "
                + StringUtils.defaultString(periodEnd, "") + "\n\n"
                + "数据如下：\n" + StringUtils.defaultString(dataJson, "{}");
    }

    private String buildUserPromptInstruction(String aiUserPrompt) {
        if (StringUtils.isBlank(aiUserPrompt)) {
            return "本次没有额外生成要求，请基于规则统计数据进行客观分析。";
        }
        return "本次用户补充要求：\n" + aiUserPrompt.trim()
                + "\n请在不改变统计口径和关键事实的前提下，按上述要求调整报告重点、表达方式和建议部分。";
    }

    private String buildProfilePrompt(String dataJson) {
        String profile = "brief";
        try {
            JSONObject data = JSON.parseObject(StringUtils.defaultIfBlank(dataJson, "{}"));
            profile = StringUtils.defaultIfBlank(data.getString("analysisProfile"), "brief");
        } catch (Exception ignored) {
        }
        if ("risk".equals(profile)) {
            return "分析档位：风险研判。请突出负面/预警线索、风险等级变化、传播扩散可能性和需重点盯防的问题。";
        }
        if ("disposal".equals(profile)) {
            return "分析档位：处置建议。请突出责任分工、响应优先级、处置动作、复盘指标和后续跟踪建议。";
        }
        return "分析档位：概览简报。请突出总体态势、主要变化、关键数据和简明关注建议。";
    }
}
