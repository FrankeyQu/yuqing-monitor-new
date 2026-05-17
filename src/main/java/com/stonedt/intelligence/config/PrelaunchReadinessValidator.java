package com.stonedt.intelligence.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Optional fail-fast checks for school trial deployment.
 */
@Component
public class PrelaunchReadinessValidator implements ApplicationRunner {

    @Value("${prelaunch.strict:0}")
    private Integer strict;

    @Value("${prelaunch.allow-legacy-external:0}")
    private Integer allowLegacyExternal;

    @Value("${token.private-key:}")
    private String tokenPrivateKey;

    @Value("${spring.datasource.druid.username:}")
    private String dbUsername;

    @Value("${spring.datasource.druid.password:}")
    private String dbPassword;

    @Value("${springdoc.api-docs.enabled:false}")
    private Boolean apiDocsEnabled;

    @Value("${springdoc.swagger-ui.enabled:false}")
    private Boolean swaggerUiEnabled;

    @Value("${knife4j.enable:false}")
    private Boolean knife4jEnabled;

    @Value("${schedule.analysis.open:0}")
    private Integer scheduleAnalysisOpen;

    @Value("${schedule.analysispt.open:0}")
    private Integer scheduleAnalysisPtOpen;

    @Value("${schedule.warning.open:0}")
    private Integer scheduleWarningOpen;

    @Value("${schedule.volume.open:0}")
    private Integer scheduleVolumeOpen;

    @Value("${schedule.volumept.open:0}")
    private Integer scheduleVolumePtOpen;

    @Value("${schedule.dayreport.open:0}")
    private Integer scheduleDayReportOpen;

    @Value("${schedule.weekreport.open:0}")
    private Integer scheduleWeekReportOpen;

    @Value("${schedule.monthreport.open:0}")
    private Integer scheduleMonthReportOpen;

    @Value("${schedule.report.open:0}")
    private Integer scheduleReportOpen;

    @Value("${schedule.wechat.open:0}")
    private Integer scheduleWechatOpen;

    @Value("${schedule.wechatqrcode.open:0}")
    private Integer scheduleWechatQrCodeOpen;

    @Value("${schedule.monitor-warning.open:0}")
    private Integer scheduleMonitorWarningOpen;

    @Value("${schedule.publicoption.open:0}")
    private Integer schedulePublicOptionOpen;

    @Value("${es.hot.open:0}")
    private Integer esHotOpen;

    @Value("${legacy.spider.open:0}")
    private Integer legacySpiderOpen;

    @Value("${legacy.spider.websocket-url:}")
    private String legacySpiderWebsocketUrl;

    @Value("${es.search.url:}")
    private String esSearchUrl;

    @Value("${es.hot.search.url:}")
    private String esHotSearchUrl;

    @Value("${kafuka.url:}")
    private String kafkaUrl;

    @Value("${insertnewwords.url:}")
    private String insertNewWordsUrl;

    @Value("${platform.nlp.ocr-url:}")
    private String nlpOcrUrl;

    @Value("${platform.nlp.classpic-url:}")
    private String nlpClasspicUrl;

    @Value("${platform.nlp.check-url:}")
    private String nlpCheckUrl;

    @Value("${platform.xie.url:}")
    private String xieUrl;

    @Value("${platform.synthesize.url:}")
    private String synthesizeUrl;

    @Override
    public void run(ApplicationArguments args) {
        if (!isEnabled(strict)) {
            return;
        }
        List<String> failures = new ArrayList<>();
        validateToken(failures);
        validateDatabase(failures);
        validateApiDocs(failures);
        validateLegacySchedules(failures);
        validateLegacyExternalUrls(failures);
        if (!failures.isEmpty()) {
            throw new IllegalStateException("Prelaunch configuration check failed: " + StringUtils.join(failures, "; "));
        }
    }

    private void validateToken(List<String> failures) {
        if (StringUtils.isBlank(tokenPrivateKey)
                || tokenPrivateKey.contains("change-this")
                || tokenPrivateKey.length() < 32) {
            failures.add("TOKEN_PRIVATE_KEY must be set to a non-default value of at least 32 characters");
        }
    }

    private void validateDatabase(List<String> failures) {
        if ("root".equalsIgnoreCase(StringUtils.trimToEmpty(dbUsername))) {
            failures.add("DB_USERNAME must not be root");
        }
        if ("123456".equals(StringUtils.trimToEmpty(dbPassword))) {
            failures.add("DB_PASSWORD must not use the local development default");
        }
    }

    private void validateApiDocs(List<String> failures) {
        if (Boolean.TRUE.equals(apiDocsEnabled)
                || Boolean.TRUE.equals(swaggerUiEnabled)
                || Boolean.TRUE.equals(knife4jEnabled)) {
            failures.add("API documentation must stay disabled unless a restricted access plan is approved");
        }
    }

    private void validateLegacySchedules(List<String> failures) {
        if (isEnabled(scheduleAnalysisOpen)
                || isEnabled(scheduleAnalysisPtOpen)
                || isEnabled(scheduleVolumeOpen)
                || isEnabled(scheduleVolumePtOpen)
                || isEnabled(scheduleDayReportOpen)
                || isEnabled(scheduleWeekReportOpen)
                || isEnabled(scheduleMonthReportOpen)
                || isEnabled(scheduleReportOpen)
                || isEnabled(scheduleWarningOpen)
                || isEnabled(scheduleWechatOpen)
                || isEnabled(scheduleWechatQrCodeOpen)
                || isEnabled(scheduleMonitorWarningOpen)
                || isEnabled(schedulePublicOptionOpen)
                || isEnabled(esHotOpen)) {
            failures.add("legacy schedules must be disabled before school trial deployment");
        }
        if (isEnabled(legacySpiderOpen) && !isEnabled(allowLegacyExternal)) {
            failures.add("legacy realtime search bridge must stay disabled unless explicitly approved");
        }
    }

    private void validateLegacyExternalUrls(List<String> failures) {
        if (isEnabled(allowLegacyExternal)) {
            return;
        }
        String[] urls = new String[]{
                esSearchUrl, esHotSearchUrl, kafkaUrl, insertNewWordsUrl,
                nlpOcrUrl, nlpClasspicUrl, nlpCheckUrl, xieUrl, synthesizeUrl,
                legacySpiderWebsocketUrl
        };
        for (String url : urls) {
            if (containsLegacyHost(url)) {
                failures.add("legacy external service URL must be removed or explicitly approved: " + redactUrl(url));
                return;
            }
        }
    }

    private boolean isEnabled(Integer value) {
        return value != null && value == 1;
    }

    private boolean containsLegacyHost(String url) {
        if (StringUtils.isBlank(url)) {
            return false;
        }
        String lower = url.toLowerCase();
        return lower.contains("stonedt.com") || lower.contains("dx1.") || lower.contains("dx2.");
    }

    private String redactUrl(String url) {
        if (StringUtils.isBlank(url)) {
            return "";
        }
        int queryIndex = url.indexOf('?');
        return queryIndex >= 0 ? url.substring(0, queryIndex) + "?..." : url;
    }
}
