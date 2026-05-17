package com.stonedt.intelligence.service.impl.campus;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.stonedt.intelligence.dao.campus.CampusDashboardDao;
import com.stonedt.intelligence.dao.campus.CampusEventDao;
import com.stonedt.intelligence.dao.campus.CampusReportDao;
import com.stonedt.intelligence.dao.campus.CampusReportEventDao;
import com.stonedt.intelligence.dao.campus.CampusReportTemplateDao;
import com.stonedt.intelligence.entity.campus.CampusEvent;
import com.stonedt.intelligence.entity.campus.CampusReport;
import com.stonedt.intelligence.entity.campus.CampusReportEvent;
import com.stonedt.intelligence.entity.campus.CampusReportTemplate;
import com.stonedt.intelligence.service.campus.AiReportService;
import com.stonedt.intelligence.service.campus.CampusReportDataService;
import com.stonedt.intelligence.service.campus.CampusReportService;
import com.stonedt.intelligence.util.SnowflakeUtil;
import com.stonedt.intelligence.vo.ReportDataVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Service
public class CampusReportServiceImpl implements CampusReportService {

    private static final String STATUS_DRAFT = "draft";
    private static final String STATUS_GENERATED = "generated";
    private static final String FORMAT_MARKDOWN = "markdown";
    private static final String GENERATION_TEMPLATE = "template";
    private static final String GENERATION_AI = "ai";
    private static final String SCOPE_ALL = "all";
    private static final String PROFILE_BRIEF = "brief";

    private final CampusReportTemplateDao campusReportTemplateDao;
    private final CampusReportDao campusReportDao;
    private final CampusReportEventDao campusReportEventDao;
    private final CampusEventDao campusEventDao;
    private final CampusDashboardDao campusDashboardDao;
    private final CampusReportDataService campusReportDataService;
    private final AiReportService aiReportService;

    public CampusReportServiceImpl(CampusReportTemplateDao campusReportTemplateDao,
                                   CampusReportDao campusReportDao,
                                   CampusReportEventDao campusReportEventDao,
                                   CampusEventDao campusEventDao,
                                   CampusDashboardDao campusDashboardDao,
                                   CampusReportDataService campusReportDataService,
                                   AiReportService aiReportService) {
        this.campusReportTemplateDao = campusReportTemplateDao;
        this.campusReportDao = campusReportDao;
        this.campusReportEventDao = campusReportEventDao;
        this.campusEventDao = campusEventDao;
        this.campusDashboardDao = campusDashboardDao;
        this.campusReportDataService = campusReportDataService;
        this.aiReportService = aiReportService;
    }

    @Override
    public CampusReportTemplate saveTemplate(CampusReportTemplate template, Long operatorUserId) {
        validateTemplate(template);
        if (template.getTemplateId() == null) {
            template.setTemplateId(SnowflakeUtil.getId());
            template.setStatus(template.getStatus() == null ? 1 : template.getStatus());
            template.setDeleted(0);
            template.setCreateUserId(operatorUserId);
            template.setUpdateUserId(operatorUserId);
            campusReportTemplateDao.insert(template);
            return campusReportTemplateDao.selectByTemplateId(template.getTemplateId());
        }
        requireTemplate(template.getTemplateId());
        template.setUpdateUserId(operatorUserId);
        campusReportTemplateDao.update(template);
        return campusReportTemplateDao.selectByTemplateId(template.getTemplateId());
    }

    @Override
    public void deleteTemplate(Long templateId, Long operatorUserId) {
        requireTemplate(templateId);
        campusReportTemplateDao.logicalDelete(templateId, operatorUserId);
    }

    @Override
    public PageInfo<CampusReportTemplate> listTemplates(Integer pageNum,
                                                        Integer pageSize,
                                                        String keyword,
                                                        String reportType,
                                                        Integer status) {
        PageHelper.startPage(defaultPageNum(pageNum), defaultPageSize(pageSize));
        return new PageInfo<>(campusReportTemplateDao.list(keyword, reportType, status));
    }

    @Override
    @Transactional
    public CampusReport saveReport(CampusReport report, Long operatorUserId) {
        validateReport(report);
        if (report.getReportId() == null) {
            report.setReportId(SnowflakeUtil.getId());
            setReportDefaults(report);
            report.setCreateUserId(operatorUserId);
            report.setUpdateUserId(operatorUserId);
            campusReportDao.insert(report);
            refreshReportEvent(report, operatorUserId);
            return campusReportDao.selectByReportId(report.getReportId());
        }
        requireReport(report.getReportId());
        report.setUpdateUserId(operatorUserId);
        campusReportDao.update(report);
        refreshReportEvent(report, operatorUserId);
        return campusReportDao.selectByReportId(report.getReportId());
    }

    @Override
    public CampusReport detail(Long reportId) {
        return requireReport(reportId);
    }

    @Override
    public PageInfo<CampusReport> listReports(Integer pageNum,
                                              Integer pageSize,
                                              String keyword,
                                              String reportType,
                                              String reportStatus,
                                              Date startTime,
                                              Date endTime) {
        PageHelper.startPage(defaultPageNum(pageNum), defaultPageSize(pageSize));
        return new PageInfo<>(campusReportDao.list(keyword, reportType, reportStatus, startTime, endTime));
    }

    @Override
    @Transactional
    public CampusReport generate(Long reportId, Long operatorUserId) {
        CampusReport report = requireReport(reportId);
        String content = StringUtils.isBlank(report.getReportContent()) ? buildReportContent(report) : report.getReportContent();
        return saveGeneratedContent(report, content, operatorUserId, GENERATION_TEMPLATE, null, null, null);
    }

    @Override
    public CampusReport generateAi(Long reportId, Long operatorUserId, StringBuilder streamOutput) {
        return generateAi(reportId, operatorUserId, streamOutput, null);
    }

    @Override
    public CampusReport generateAi(Long reportId, Long operatorUserId, StringBuilder streamOutput,
                                   Consumer<String> chunkConsumer) {
        return generateAi(reportId, operatorUserId, streamOutput, chunkConsumer, null);
    }

    @Override
    public CampusReport generateAi(Long reportId, Long operatorUserId, StringBuilder streamOutput,
                                   Consumer<String> chunkConsumer, String aiUserPrompt) {
        CampusReport report = requireReport(reportId);
        String effectivePrompt = StringUtils.defaultIfBlank(aiUserPrompt, report.getAiUserPrompt());
        String dataJson = buildReportDataJson(report, effectivePrompt);
        String content = aiReportService.generateReport(
                report.getReportType(),
                report.getReportTitle(),
                dataJson,
                formatDate(report.getPeriodStartTime()),
                formatDate(report.getPeriodEndTime()),
                effectivePrompt,
                streamOutput,
                chunkConsumer);
        if (StringUtils.isBlank(content) && streamOutput != null) {
            content = streamOutput.toString();
        }
        if (StringUtils.isBlank(content)) {
            throw new IllegalStateException("AI报告生成未返回有效内容");
        }
        return saveGeneratedContent(report, content, operatorUserId, GENERATION_AI,
                "report_generate", effectivePrompt, StringUtils.left(dataJson, 60000));
    }

    @Override
    @Transactional
    public CampusReport archive(Long reportId, String archiveOpinion, Long operatorUserId) {
        requireReport(reportId);
        campusReportDao.archive(reportId, archiveOpinion, operatorUserId, operatorUserId);
        return campusReportDao.selectByReportId(reportId);
    }

    @Override
    @Transactional
    public void deleteReport(Long reportId, Long operatorUserId) {
        requireReport(reportId);
        campusReportDao.logicalDelete(reportId, operatorUserId);
        campusReportEventDao.logicalDeleteByReportId(reportId);
    }

    @Override
    public List<CampusReportEvent> listReportEvents(Long reportId) {
        requireReport(reportId);
        return campusReportEventDao.listByReportId(reportId);
    }

    @Override
    public ResponseEntity<InputStreamResource> download(Long reportId) {
        CampusReport report = requireReport(reportId);
        String content = StringUtils.defaultIfBlank(report.getReportContent(), buildReportContent(report));
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        String fileName = StringUtils.defaultIfBlank(report.getFileName(), buildFileName(report));
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
            headers.add("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName, "UTF-8"));
            headers.add("Pragma", "no-cache");
            headers.add("Expires", "0");
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(bytes.length)
                    .contentType(resolveMediaType(report.getReportFormat()))
                    .body(new InputStreamResource(new ByteArrayInputStream(bytes)));
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    private void refreshReportEvent(CampusReport report, Long operatorUserId) {
        if (report.getReportId() == null) {
            return;
        }
        campusReportEventDao.logicalDeleteByReportId(report.getReportId());
        if (report.getEventId() == null) {
            return;
        }
        CampusReportEvent relation = new CampusReportEvent();
        relation.setRelationId(SnowflakeUtil.getId());
        relation.setReportId(report.getReportId());
        relation.setEventId(report.getEventId());
        relation.setDeleted(0);
        relation.setCreateUserId(operatorUserId);
        campusReportEventDao.insert(relation);
    }

    private String buildReportContent(CampusReport report) {
        CampusReportTemplate template = report.getTemplateId() == null
                ? null
                : campusReportTemplateDao.selectByTemplateId(report.getTemplateId());
        CampusEvent event = report.getEventId() == null ? null : campusEventDao.selectByEventId(report.getEventId());

        ReportDataVO reportData = campusReportDataService.aggregateReportData(report);

        if (template != null && StringUtils.isNotBlank(template.getTemplateContent())) {
            return applyTemplate(template.getTemplateContent(), report, event, reportData);
        }

        // 无模板时生成完整 Markdown 报告（七大模块）
        StringBuilder b = new StringBuilder();

        // 标题
        b.append("# ").append(report.getReportTitle()).append("\n\n");
        b.append("- 报告类型：").append(StringUtils.defaultString(report.getReportType())).append("\n");
        b.append("- 统计周期：").append(formatDate(report.getPeriodStartTime())).append(" 至 ")
                .append(formatDate(report.getPeriodEndTime())).append("\n");
        b.append("- 生成时间：").append(formatDateTime(new Date())).append("\n\n");

        // 一、舆情概况
        b.append("## 一、舆情概况\n\n");
        b.append(reportData.getSummary()).append("\n\n");
        b.append("| 指标 | 数值 |\n");
        b.append("|------|------|\n");
        b.append("| 监测文章总数 | ").append(nullToZero(reportData.getTotalCount())).append(" |\n");
        b.append("| 负面文章数 | ").append(nullToZero(reportData.getNegativeCount())).append(" |\n");
        b.append("| 中性文章数 | ").append(nullToZero(reportData.getNeutralCount())).append(" |\n");
        b.append("| 正面文章数 | ").append(nullToZero(reportData.getPositiveCount())).append(" |\n\n");
        if (event != null) {
            b.append("### 关联事件\n\n");
            b.append("- 事件标题：").append(StringUtils.defaultString(event.getEventTitle())).append("\n");
            b.append("- 风险等级：").append(StringUtils.defaultString(event.getRiskLevel())).append("\n");
            b.append("- 当前状态：").append(StringUtils.defaultString(event.getEventStatus())).append("\n");
            b.append("- 事件摘要：").append(StringUtils.defaultString(event.getEventSummary())).append("\n\n");
        }

        // 二、舆情走势
        b.append("## 二、舆情走势\n\n");
        b.append(buildTrendTable(reportData.getTrend()));
        b.append("\n");

        // 三、媒体分析
        b.append("## 三、媒体分析\n\n");
        b.append("### 媒体分布\n\n");
        b.append(buildMediaTable(reportData.getMediaDistribution()));
        b.append("\n### 平台排名 TOP 10\n\n");
        b.append(buildPlatformRankingTable(reportData.getPlatformRanking()));
        b.append("\n");

        // 四、情感分析
        b.append("## 四、情感分析\n\n");
        b.append(buildSentimentTable(reportData.getSentimentDistribution()));
        b.append("\n");

        // 五、热词分析
        b.append("## 五、热词分析\n\n");
        b.append(buildKeywordTable(reportData.getHotKeywords()));
        b.append("\n");

        // 六、热点文章
        b.append("## 六、热点文章\n\n");
        b.append(buildHotArticlesList(reportData.getHotArticles()));
        b.append("\n");

        // 七、治理复盘
        b.append("## 七、治理复盘\n\n");
        b.append(buildGovernanceTable());
        b.append("\n");

        // 八、报告说明
        b.append("## 八、报告说明\n\n");
        b.append("- 数据来源：校园舆情监测系统\n");
        b.append("- 统计周期：").append(formatDate(report.getPeriodStartTime())).append(" 至 ")
                .append(formatDate(report.getPeriodEndTime())).append("\n");
        b.append("- 生成时间：").append(formatDateTime(new Date())).append("\n");
        b.append("- 如需更详细数据，请联系系统管理员。\n");

        return b.toString();
    }

    private CampusReport saveGeneratedContent(CampusReport report,
                                              String content,
                                              Long operatorUserId,
                                              String generationMode,
                                              String aiModel,
                                              String aiUserPrompt,
                                              String aiPromptSnapshot) {
        CampusReport update = new CampusReport();
        update.setReportId(report.getReportId());
        update.setReportStatus(STATUS_GENERATED);
        update.setGenerationMode(StringUtils.defaultIfBlank(generationMode, report.getGenerationMode()));
        update.setReportFormat(FORMAT_MARKDOWN);
        update.setReportContent(content);
        update.setFileName(buildFileNameForFormat(report, FORMAT_MARKDOWN));
        update.setAiModel(aiModel);
        update.setAiUserPrompt(aiUserPrompt);
        update.setAiPromptSnapshot(aiPromptSnapshot);
        update.setGeneratedBy(operatorUserId);
        update.setGenerateTime(new Date());
        update.setUpdateUserId(operatorUserId);
        campusReportDao.update(update);
        return campusReportDao.selectByReportId(report.getReportId());
    }

    private String buildReportDataJson(CampusReport report) {
        return buildReportDataJson(report, null);
    }

    String buildReportDataJson(CampusReport report, String aiUserPrompt) {
        CampusEvent event = report.getEventId() == null ? null : campusEventDao.selectByEventId(report.getEventId());
        ReportDataVO reportData = campusReportDataService.aggregateReportData(report);
        JSONObject data = new JSONObject();
        data.put("reportId", report.getReportId());
        data.put("reportTitle", report.getReportTitle());
        data.put("reportType", report.getReportType());
        data.put("generationMode", report.getGenerationMode());
        data.put("scopeType", report.getScopeType());
        data.put("scopeKeywords", report.getScopeKeywords());
        data.put("excludeKeywords", report.getExcludeKeywords());
        data.put("platformScope", report.getPlatformScope());
        data.put("riskLevels", report.getRiskLevels());
        data.put("departmentScope", report.getDepartmentScope());
        data.put("monitorTaskIds", report.getMonitorTaskIds());
        data.put("analysisProfile", report.getAnalysisProfile());
        data.put("aiUserPrompt", StringUtils.defaultString(aiUserPrompt));
        data.put("reportSummary", StringUtils.defaultString(report.getReportSummary()));
        data.put("periodStart", formatDate(report.getPeriodStartTime()));
        data.put("periodEnd", formatDate(report.getPeriodEndTime()));
        data.put("eventId", report.getEventId());
        if (event != null) {
            data.put("eventTitle", event.getEventTitle());
            data.put("eventSummary", event.getEventSummary());
            data.put("eventStatus", event.getEventStatus());
            data.put("riskLevel", event.getRiskLevel());
        }
        data.put("reportData", reportDataSnapshot(reportData));
        data.put("governance", governanceSnapshot());
        return data.toJSONString();
    }

    static JSONObject reportDataSnapshot(ReportDataVO reportData) {
        JSONObject snapshot = new JSONObject();
        if (reportData == null) {
            return snapshot;
        }
        snapshot.put("totalCount", reportData.getTotalCount());
        snapshot.put("negativeCount", reportData.getNegativeCount());
        snapshot.put("neutralCount", reportData.getNeutralCount());
        snapshot.put("positiveCount", reportData.getPositiveCount());
        snapshot.put("periodStart", reportData.getPeriodStart());
        snapshot.put("periodEnd", reportData.getPeriodEnd());
        snapshot.put("summary", reportData.getSummary());
        snapshot.put("trend", copyList(reportData.getTrend()));
        snapshot.put("mediaDistribution", copyList(reportData.getMediaDistribution()));
        snapshot.put("platformRanking", copyList(reportData.getPlatformRanking()));
        snapshot.put("sentimentDistribution", copyList(reportData.getSentimentDistribution()));
        snapshot.put("hotKeywords", copyList(reportData.getHotKeywords()));
        snapshot.put("hotArticles", copyList(reportData.getHotArticles()));
        return snapshot;
    }

    private JSONObject governanceSnapshot() {
        JSONObject snapshot = new JSONObject();
        if (campusDashboardDao == null) {
            return snapshot;
        }
        try {
            Map<String, Object> metrics = campusDashboardDao.governanceMetrics();
            snapshot.put("metrics", metrics == null ? new JSONObject() : new JSONObject(metrics));
            snapshot.put("topicRiskDistribution", copyList(campusDashboardDao.topicRiskDistribution()));
        } catch (Exception ignored) {
            snapshot.put("metrics", new JSONObject());
            snapshot.put("topicRiskDistribution", new JSONArray());
        }
        return snapshot;
    }

    private static JSONArray copyList(List<Map<String, Object>> rows) {
        JSONArray array = new JSONArray();
        if (rows == null) {
            return array;
        }
        for (Map<String, Object> row : rows) {
            array.add(row == null ? new JSONObject() : new JSONObject(row));
        }
        return array;
    }

    private String resolveReportKeyword(CampusReport report, CampusEvent event) {
        if (event != null && StringUtils.isNotBlank(event.getEventTitle())) {
            return event.getEventTitle();
        }
        return report == null ? null : StringUtils.trimToNull(report.getReportTitle());
    }

    private String applyTemplate(String templateContent,
                                 CampusReport report,
                                 CampusEvent event,
                                 ReportDataVO reportData) {
        String content = templateContent;
        // 基础变量
        content = content.replace("${reportTitle}", StringUtils.defaultString(report.getReportTitle()));
        content = content.replace("${reportType}", StringUtils.defaultString(report.getReportType()));
        content = content.replace("${reportSummary}", StringUtils.defaultString(report.getReportSummary()));
        content = content.replace("${periodStart}", formatDate(report.getPeriodStartTime()));
        content = content.replace("${periodEnd}", formatDate(report.getPeriodEndTime()));
        // overview 变量映射到报告摘要
        content = content.replace("${overview}",
                reportData != null && reportData.getSummary() != null ? reportData.getSummary() : "");
        // 事件变量
        content = content.replace("${eventTitle}", event == null ? "" : StringUtils.defaultString(event.getEventTitle()));
        content = content.replace("${eventSummary}", event == null ? "" : StringUtils.defaultString(event.getEventSummary()));
        content = content.replace("${riskLevel}", event == null ? "" : StringUtils.defaultString(event.getRiskLevel()));
        content = content.replace("${eventStatus}", event == null ? "" : StringUtils.defaultString(event.getEventStatus()));
        content = content.replace("${governanceTable}", buildGovernanceTable());

        if (reportData != null) {
            // 计数变量
            content = content.replace("${totalCount}", String.valueOf(nullToZero(reportData.getTotalCount())));
            content = content.replace("${negativeCount}", String.valueOf(nullToZero(reportData.getNegativeCount())));
            content = content.replace("${neutralCount}", String.valueOf(nullToZero(reportData.getNeutralCount())));
            content = content.replace("${positiveCount}", String.valueOf(nullToZero(reportData.getPositiveCount())));
            // 表格/列表变量
            content = content.replace("${trendTable}", buildTrendTable(reportData.getTrend()));
            content = content.replace("${mediaTable}", buildMediaTable(reportData.getMediaDistribution()));
            content = content.replace("${sentimentTable}", buildSentimentTable(reportData.getSentimentDistribution()));
            content = content.replace("${keywordTable}", buildKeywordTable(reportData.getHotKeywords()));
            content = content.replace("${hotArticles}", buildHotArticlesList(reportData.getHotArticles()));
            content = content.replace("${platformRanking}", buildPlatformRankingTable(reportData.getPlatformRanking()));
        } else {
            content = content.replace("${totalCount}", "0");
            content = content.replace("${negativeCount}", "0");
            content = content.replace("${neutralCount}", "0");
            content = content.replace("${positiveCount}", "0");
            content = content.replace("${trendTable}", "");
            content = content.replace("${mediaTable}", "");
            content = content.replace("${sentimentTable}", "");
            content = content.replace("${keywordTable}", "");
            content = content.replace("${hotArticles}", "");
            content = content.replace("${platformRanking}", "");
        }
        content = content.replace("${governanceTable}", buildGovernanceTable());
        return content;
    }

    private MediaType resolveMediaType(String reportFormat) {
        if ("html".equals(reportFormat)) {
            return MediaType.TEXT_HTML;
        }
        if ("text".equals(reportFormat)) {
            return MediaType.TEXT_PLAIN;
        }
        return MediaType.parseMediaType("text/markdown;charset=UTF-8");
    }

    private String buildFileName(CampusReport report) {
        return buildFileNameForFormat(report, report.getReportFormat());
    }

    private String buildFileNameForFormat(CampusReport report, String reportFormat) {
        String suffix = "html".equals(reportFormat) ? ".html" : ".md";
        if ("text".equals(reportFormat)) {
            suffix = ".txt";
        }
        String title = StringUtils.defaultIfBlank(report.getReportTitle(), "campus-report");
        return title.replaceAll("[\\\\/:*?\"<>|\\s]+", "_") + suffix;
    }

    private void validateTemplate(CampusReportTemplate template) {
        if (template == null) {
            throw new IllegalArgumentException("报告模板不能为空");
        }
        if (StringUtils.isBlank(template.getTemplateName())) {
            throw new IllegalArgumentException("模板名称不能为空");
        }
        if (StringUtils.isBlank(template.getReportType())) {
            throw new IllegalArgumentException("报告类型不能为空");
        }
    }

    private void validateReport(CampusReport report) {
        if (report == null) {
            throw new IllegalArgumentException("报告不能为空");
        }
        if (StringUtils.isBlank(report.getReportTitle())) {
            throw new IllegalArgumentException("报告标题不能为空");
        }
        if (StringUtils.isBlank(report.getReportType())) {
            throw new IllegalArgumentException("报告类型不能为空");
        }
    }

    private CampusReportTemplate requireTemplate(Long templateId) {
        if (templateId == null) {
            throw new IllegalArgumentException("模板ID不能为空");
        }
        CampusReportTemplate template = campusReportTemplateDao.selectByTemplateId(templateId);
        if (template == null) {
            throw new IllegalArgumentException("报告模板不存在");
        }
        return template;
    }

    private CampusReport requireReport(Long reportId) {
        if (reportId == null) {
            throw new IllegalArgumentException("报告ID不能为空");
        }
        CampusReport report = campusReportDao.selectByReportId(reportId);
        if (report == null) {
            throw new IllegalArgumentException("报告不存在");
        }
        return report;
    }

    private void setReportDefaults(CampusReport report) {
        if (StringUtils.isBlank(report.getReportStatus())) {
            report.setReportStatus(STATUS_DRAFT);
        }
        if (StringUtils.isBlank(report.getGenerationMode())) {
            report.setGenerationMode(GENERATION_TEMPLATE);
        }
        if (StringUtils.isBlank(report.getScopeType())) {
            report.setScopeType(SCOPE_ALL);
        }
        if (StringUtils.isBlank(report.getAnalysisProfile())) {
            report.setAnalysisProfile(PROFILE_BRIEF);
        }
        if (StringUtils.isBlank(report.getReportFormat())) {
            report.setReportFormat(FORMAT_MARKDOWN);
        }
        report.setDeleted(0);
    }

    private String formatDate(Date date) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }

    private String formatDateTime(Date date) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }

    private int defaultPageNum(Integer pageNum) {
        return pageNum == null || pageNum < 1 ? 1 : pageNum;
    }

    private int defaultPageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return 10;
        }
        return Math.min(pageSize, 100);
    }

    // ---- 模板变量渲染辅助方法 ----

    private int nullToZero(Integer val) {
        return val == null ? 0 : val;
    }

    private String buildTrendTable(List<Map<String, Object>> trend) {
        if (trend == null || trend.isEmpty()) {
            return "暂无走势数据。\n";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("| 日期 | 线索数量 | 告警数量 |\n");
        sb.append("|------|----------|----------|\n");
        for (Map<String, Object> row : trend) {
            sb.append("| ").append(orDash(row.get("date")))
              .append(" | ").append(orZero(row.get("clueCount")))
              .append(" | ").append(orZero(row.get("alertCount")))
              .append(" |\n");
        }
        return sb.toString();
    }

    private String buildMediaTable(List<Map<String, Object>> data) {
        if (data == null || data.isEmpty()) {
            return "暂无媒体分布数据。\n";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("| 媒体平台 | 文章数量 |\n");
        sb.append("|----------|----------|\n");
        for (Map<String, Object> row : data) {
            sb.append("| ").append(orDash(row.get("name")))
              .append(" | ").append(orZero(row.get("value")))
              .append(" |\n");
        }
        return sb.toString();
    }

    private String buildSentimentTable(List<Map<String, Object>> data) {
        if (data == null || data.isEmpty()) {
            return "暂无情感分布数据。\n";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("| 情感倾向 | 文章数量 |\n");
        sb.append("|----------|----------|\n");
        for (Map<String, Object> row : data) {
            sb.append("| ").append(orDash(row.get("name")))
              .append(" | ").append(orZero(row.get("value")))
              .append(" |\n");
        }
        return sb.toString();
    }

    private String buildKeywordTable(List<Map<String, Object>> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            return "暂无热词数据。\n";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("| 排名 | 关键词 | 出现次数 |\n");
        sb.append("|------|--------|----------|\n");
        int rank = 1;
        for (Map<String, Object> row : keywords) {
            sb.append("| ").append(rank++)
              .append(" | ").append(orDash(row.get("keyword")))
              .append(" | ").append(orZero(row.get("count")))
              .append(" |\n");
        }
        return sb.toString();
    }

    private String buildHotArticlesList(List<Map<String, Object>> articles) {
        if (articles == null || articles.isEmpty()) {
            return "暂无热点文章数据。\n";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("| 序号 | 标题 | 来源平台 | 情感倾向 |\n");
        sb.append("|------|------|----------|----------|\n");
        int index = 1;
        for (Map<String, Object> row : articles) {
            String title = orDash(row.get("title"));
            // 截断过长标题
            if (title.length() > 40) {
                title = title.substring(0, 40) + "...";
            }
            String url = orDash(row.get("url"));
            // 如果有 URL，将标题设为超链接
            if (row.get("url") != null && String.valueOf(row.get("url")).length() > 0) {
                title = "[" + title + "](" + url + ")";
            }
            sb.append("| ").append(index++)
              .append(" | ").append(title)
              .append(" | ").append(orDash(row.get("platform")))
              .append(" | ").append(orDash(row.get("sentiment")))
              .append(" |\n");
        }
        return sb.toString();
    }

    private String buildPlatformRankingTable(List<Map<String, Object>> data) {
        if (data == null || data.isEmpty()) {
            return "暂无平台排名数据。\n";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("| 排名 | 平台 | 文章数量 |\n");
        sb.append("|------|------|----------|\n");
        int rank = 1;
        for (Map<String, Object> row : data) {
            sb.append("| ").append(rank++)
              .append(" | ").append(orDash(row.get("name")))
              .append(" | ").append(orZero(row.get("value")))
              .append(" |\n");
        }
        return sb.toString();
    }

    private String buildGovernanceTable() {
        Map<String, Object> metrics = campusDashboardDao.governanceMetrics();
        List<Map<String, Object>> topicRisk = campusDashboardDao.topicRiskDistribution();
        StringBuilder sb = new StringBuilder();
        sb.append("| 治理指标 | 数值 |\n");
        sb.append("|----------|------|\n");
        sb.append("| 逾期处置任务 | ").append(orZero(metrics == null ? null : metrics.get("overdueTaskCount"))).append(" |\n");
        sb.append("| 24小时内到期任务 | ").append(orZero(metrics == null ? null : metrics.get("dueSoonTaskCount"))).append(" |\n");
        sb.append("| 待处理预警 | ").append(orZero(metrics == null ? null : metrics.get("pendingAlertCount"))).append(" |\n");
        sb.append("| 待归档复核事件 | ").append(orZero(metrics == null ? null : metrics.get("reviewedEventCount"))).append(" |\n");
        sb.append("| 今日归档事件 | ").append(orZero(metrics == null ? null : metrics.get("todayArchivedEventCount"))).append(" |\n\n");
        if (topicRisk == null || topicRisk.isEmpty()) {
            sb.append("暂无主题风险分布数据。\n");
            return sb.toString();
        }
        sb.append("| 主题 | 总量 | 一般预警 | 重大预警 | 特别重大 |\n");
        sb.append("|------|------|----------|----------|----------|\n");
        for (Map<String, Object> row : topicRisk) {
            sb.append("| ").append(orDash(row.get("name")))
                    .append(" | ").append(orZero(row.get("totalCount")))
                    .append(" | ").append(orZero(row.get("concernCount")))
                    .append(" | ").append(orZero(row.get("majorCount")))
                    .append(" | ").append(orZero(row.get("urgentCount")))
                    .append(" |\n");
        }
        return sb.toString();
    }

    private String orDash(Object val) {
        if (val == null) {
            return "-";
        }
        String s = val.toString().trim();
        return s.isEmpty() ? "-" : s;
    }

    private String orZero(Object val) {
        if (val == null) {
            return "0";
        }
        if (val instanceof Number) {
            return String.valueOf(((Number) val).longValue());
        }
        String s = val.toString().trim();
        if (s.isEmpty()) {
            return "0";
        }
        try {
            Long.parseLong(s);
            return s;
        } catch (NumberFormatException e) {
            return "0";
        }
    }
}
