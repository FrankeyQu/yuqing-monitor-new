package com.stonedt.intelligence.service.campus;

/**
 * Report export service for generating docx and pptx binary files
 * from markdown-format report content.
 */
public interface ReportExportService {

    /**
     * Export report content as a .docx file.
     *
     * @param reportTitle   title of the report
     * @param reportContent markdown-format report content
     * @param reportType    report type: daily / weekly / monthly / special
     * @return byte array of the generated .docx file
     */
    byte[] exportDocx(String reportTitle, String reportContent, String reportType);

    /**
     * Export report content as a .pptx file.
     *
     * @param reportTitle   title of the report
     * @param reportContent markdown-format report content
     * @param reportType    report type: daily / weekly / monthly / special
     * @return byte array of the generated .pptx file
     */
    byte[] exportPptx(String reportTitle, String reportContent, String reportType);
}
