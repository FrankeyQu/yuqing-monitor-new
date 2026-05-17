package com.stonedt.intelligence.service.campus;

/**
 * AI-powered report generation service using DeepSeek-v4-pro.
 * Supports daily, weekly, monthly, and special report types.
 */
public interface AiReportService {

    /**
     * Generate a report via DeepSeek AI.
     *
     * @param reportType   report type: daily / weekly / monthly / special
     * @param reportTitle  title of the report
     * @param dataJson     JSON string of aggregated report data
     * @param periodStart  start of the reporting period (yyyy-MM-dd)
     * @param periodEnd    end of the reporting period (yyyy-MM-dd)
     * @param streamOutput if not null, streaming chunks are appended for SSE
     * @return the full generated report content in markdown
     */
    String generateReport(String reportType, String reportTitle, String dataJson,
                           String periodStart, String periodEnd, StringBuilder streamOutput);
}
