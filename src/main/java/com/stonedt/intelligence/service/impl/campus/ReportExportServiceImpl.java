package com.stonedt.intelligence.service.impl.campus;

import com.stonedt.intelligence.service.campus.ReportExportService;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextParagraph;
import org.apache.poi.xslf.usermodel.XSLFTextRun;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.awt.geom.Rectangle2D;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Report export service implementation.
 * Generates docx and pptx files from markdown-format report content using Apache POI.
 */
@Service
public class ReportExportServiceImpl implements ReportExportService {

    private static final Logger log = LoggerFactory.getLogger(ReportExportServiceImpl.class);

    private static final String FONT_HEADING = "SimHei";
    private static final String FONT_BODY = "SimSun";
    private static final int DOCX_HEADING_1_SIZE = 22;
    private static final int DOCX_HEADING_2_SIZE = 16;
    private static final int DOCX_BODY_SIZE = 12;

    @Override
    public byte[] exportDocx(String reportTitle, String reportContent, String reportType) {
        try (XWPFDocument doc = new XWPFDocument();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            // Title
            XWPFParagraph titlePara = doc.createParagraph();
            titlePara.setAlignment(org.apache.poi.xwpf.usermodel.ParagraphAlignment.CENTER);
            XWPFRun titleRun = titlePara.createRun();
            titleRun.setText(StringUtils.defaultString(reportTitle, "舆情报告"));
            titleRun.setBold(true);
            titleRun.setFontSize(DOCX_HEADING_1_SIZE);
            titleRun.setFontFamily(FONT_HEADING);

            // Metadata line: report type + date
            XWPFParagraph metaPara = doc.createParagraph();
            metaPara.setAlignment(org.apache.poi.xwpf.usermodel.ParagraphAlignment.CENTER);
            XWPFRun metaRun = metaPara.createRun();
            String typeLabel = resolveTypeLabel(reportType);
            String dateStr = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            metaRun.setText("报告类型：" + typeLabel + "    生成日期：" + dateStr);
            metaRun.setFontSize(DOCX_BODY_SIZE);
            metaRun.setFontFamily(FONT_BODY);
            metaRun.setColor("666666");

            // Blank line
            doc.createParagraph();

            if (StringUtils.isBlank(reportContent)) {
                XWPFParagraph emptyPara = doc.createParagraph();
                XWPFRun emptyRun = emptyPara.createRun();
                emptyRun.setText("(报告内容为空)");
                emptyRun.setFontSize(DOCX_BODY_SIZE);
                emptyRun.setFontFamily(FONT_BODY);
                emptyRun.setItalic(true);
                emptyRun.setColor("999999");
            } else {
                parseMarkdownToDocx(doc, reportContent);
            }

            doc.write(bos);
            return bos.toByteArray();

        } catch (IOException e) {
            log.error("Failed to generate docx for report: {}", reportTitle, e);
            throw new RuntimeException("导出docx失败: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] exportPptx(String reportTitle, String reportContent, String reportType) {
        try (XMLSlideShow ppt = new XMLSlideShow();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            // Slide 1: Title slide
            XSLFSlide titleSlide = ppt.createSlide();
            addTitleSlide(titleSlide, reportTitle, reportType);

            if (StringUtils.isBlank(reportContent)) {
                XSLFSlide emptySlide = ppt.createSlide();
                addEmptyContentSlide(emptySlide);
            } else {
                // Parse ## sections into separate slides
                String[] sections = reportContent.split("\n(?=## )");
                boolean firstSection = true;
                for (String section : sections) {
                    if (firstSection) {
                        firstSection = false;
                        // The first section (before first ## or containing the # title)
                        // We already handled title separately, check if it has ## content
                        String trimmed = section.trim();
                        if (trimmed.startsWith("## ")) {
                            XSLFSlide slide = ppt.createSlide();
                            addContentSlide(slide, trimmed);
                        } else if (StringUtils.isNotBlank(trimmed)) {
                            // Text before any ## heading — put as overview
                            XSLFSlide slide = ppt.createSlide();
                            addTextOnlySlide(slide, "报告概述", trimmed);
                        }
                    } else {
                        XSLFSlide slide = ppt.createSlide();
                        addContentSlide(slide, section.trim());
                    }
                }

                // Ensure at least one content slide exists
                if (ppt.getSlides().size() <= 1) {
                    XSLFSlide slide = ppt.createSlide();
                    addTextOnlySlide(slide, "报告内容", reportContent);
                }
            }

            ppt.write(bos);
            return bos.toByteArray();

        } catch (IOException e) {
            log.error("Failed to generate pptx for report: {}", reportTitle, e);
            throw new RuntimeException("导出pptx失败: " + e.getMessage(), e);
        }
    }

    // ───────── docx helpers ─────────

    private void parseMarkdownToDocx(XWPFDocument doc, String markdown) {
        String[] lines = markdown.split("\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                // empty line
                doc.createParagraph();
            } else if (trimmed.startsWith("# ")) {
                addDocxHeading(doc, trimmed.substring(2), DOCX_HEADING_1_SIZE, true);
            } else if (trimmed.startsWith("## ")) {
                addDocxHeading(doc, trimmed.substring(3), DOCX_HEADING_2_SIZE, true);
            } else if (trimmed.startsWith("### ")) {
                addDocxHeading(doc, trimmed.substring(4), DOCX_HEADING_2_SIZE, false);
            } else if (trimmed.startsWith("- ") || trimmed.startsWith("* ")) {
                addDocxBullet(doc, trimmed.substring(2));
            } else if (trimmed.startsWith("> ")) {
                addDocxQuote(doc, trimmed.substring(2));
            } else if (trimmed.startsWith("|") && trimmed.contains("|")) {
                // Skip table separator lines (| --- |)
                if (!trimmed.matches("\\|[\\s\\-:]+\\|.*")) {
                    addDocxTableRow(doc, trimmed);
                }
            } else if (trimmed.startsWith("```")) {
                // Skip code block fences
            } else {
                addDocxParagraph(doc, trimmed);
            }
        }
    }

    private void addDocxHeading(XWPFDocument doc, String text, int fontSize, boolean bold) {
        XWPFParagraph para = doc.createParagraph();
        XWPFRun run = para.createRun();
        run.setText(text);
        run.setBold(bold);
        run.setFontSize(fontSize);
        run.setFontFamily(FONT_HEADING);
        // Add spacing before heading
        if (fontSize >= DOCX_HEADING_2_SIZE) {
            para.setSpacingBefore(200);
        }
        para.setSpacingAfter(100);
    }

    private void addDocxParagraph(XWPFDocument doc, String text) {
        XWPFParagraph para = doc.createParagraph();
        XWPFRun run = para.createRun();
        // Strip inline markdown formatting
        text = stripInlineMarkdown(text);
        run.setText(text);
        run.setFontSize(DOCX_BODY_SIZE);
        run.setFontFamily(FONT_BODY);
    }

    private void addDocxBullet(XWPFDocument doc, String text) {
        XWPFParagraph para = doc.createParagraph();
        para.setIndentationLeft(480);
        para.setIndentationHanging(240);
        XWPFRun run = para.createRun();
        run.setText("• " + stripInlineMarkdown(text));
        run.setFontSize(DOCX_BODY_SIZE);
        run.setFontFamily(FONT_BODY);
    }

    private void addDocxQuote(XWPFDocument doc, String text) {
        XWPFParagraph para = doc.createParagraph();
        para.setIndentationLeft(360);
        XWPFRun run = para.createRun();
        run.setText(stripInlineMarkdown(text));
        run.setFontSize(DOCX_BODY_SIZE);
        run.setFontFamily(FONT_BODY);
        run.setItalic(true);
        run.setColor("555555");
    }

    private void addDocxTableRow(XWPFDocument doc, String line) {
        String[] cells = line.split("\\|");
        XWPFParagraph para = doc.createParagraph();
        para.setIndentationLeft(240);
        XWPFRun run = para.createRun();
        StringBuilder rowText = new StringBuilder();
        for (int i = 0; i < cells.length; i++) {
            String cell = cells[i].trim();
            if (cell.isEmpty()) {
                continue;
            }
            if (rowText.length() > 0) {
                rowText.append("  |  ");
            }
            rowText.append(cell);
        }
        run.setText(rowText.toString());
        run.setFontSize(DOCX_BODY_SIZE);
        run.setFontFamily(FONT_BODY);
    }

    private String stripInlineMarkdown(String text) {
        if (text == null) {
            return "";
        }
        // Remove bold/italic markers
        text = text.replaceAll("\\*\\*(.+?)\\*\\*", "$1");
        text = text.replaceAll("\\*(.+?)\\*", "$1");
        text = text.replaceAll("~~(.+?)~~", "$1");
        text = text.replaceAll("`(.+?)`", "$1");
        return text.trim();
    }

    // ───────── pptx helpers ─────────

    private void addTitleSlide(XSLFSlide slide, String reportTitle, String reportType) {
        XSLFTextShape titleShape = slide.createTextBox();
        titleShape.setAnchor(new Rectangle2D.Double(72, 180, 576, 120));
        titleShape.setWordWrap(true);
        XSLFTextParagraph titlePara = titleShape.addNewTextParagraph();
        XSLFTextRun titleRun = titlePara.addNewTextRun();
        titleRun.setText(StringUtils.defaultString(reportTitle, "舆情报告"));
        titleRun.setBold(true);
        titleRun.setFontSize(36.0);
        titleRun.setFontFamily(FONT_HEADING);

        XSLFTextShape infoShape = slide.createTextBox();
        infoShape.setAnchor(new Rectangle2D.Double(72, 330, 576, 80));
        infoShape.setWordWrap(true);

        XSLFTextParagraph typePara = infoShape.addNewTextParagraph();
        XSLFTextRun typeRun = typePara.addNewTextRun();
        String typeLabel = resolveTypeLabel(reportType);
        typeRun.setText("报告类型：" + typeLabel);
        typeRun.setFontSize(18.0);
        typeRun.setFontFamily(FONT_BODY);
        typeRun.setFontColor(java.awt.Color.GRAY);

        XSLFTextParagraph datePara = infoShape.addNewTextParagraph();
        XSLFTextRun dateRun = datePara.addNewTextRun();
        String dateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
        dateRun.setText("生成日期：" + dateStr);
        dateRun.setFontSize(14.0);
        dateRun.setFontFamily(FONT_BODY);
        dateRun.setFontColor(java.awt.Color.GRAY);
    }

    private void addContentSlide(XSLFSlide slide, String sectionContent) {
        String[] lines = sectionContent.split("\n", 2);
        String heading = lines[0].replaceFirst("^##\\s+", "").trim();
        String body = lines.length > 1 ? lines[1].trim() : "";

        // Heading
        XSLFTextShape headingShape = slide.createTextBox();
        headingShape.setAnchor(new Rectangle2D.Double(54, 36, 612, 54));
        headingShape.setWordWrap(true);
        XSLFTextParagraph headingPara = headingShape.addNewTextParagraph();
        XSLFTextRun headingRun = headingPara.addNewTextRun();
        headingRun.setText(heading);
        headingRun.setBold(true);
        headingRun.setFontSize(24.0);
        headingRun.setFontFamily(FONT_HEADING);

        // Body
        XSLFTextShape bodyShape = slide.createTextBox();
        bodyShape.setAnchor(new Rectangle2D.Double(54, 108, 612, 400));
        bodyShape.setWordWrap(true);

        if (StringUtils.isNotBlank(body)) {
            String[] bodyLines = body.split("\n");
            for (String line : bodyLines) {
                String trimmed = line.trim();
                if (trimmed.isEmpty()) {
                    XSLFTextParagraph p = bodyShape.addNewTextParagraph();
                    XSLFTextRun r = p.addNewTextRun();
                    r.setText(" ");
                    r.setFontSize(10.0);
                } else if (trimmed.startsWith("- ") || trimmed.startsWith("* ")) {
                    XSLFTextParagraph p = bodyShape.addNewTextParagraph();
                    XSLFTextRun r = p.addNewTextRun();
                    r.setText("• " + stripInlineMarkdown(trimmed.substring(2)));
                    r.setFontSize(14.0);
                    r.setFontFamily(FONT_BODY);
                } else if (trimmed.startsWith("> ")) {
                    XSLFTextParagraph p = bodyShape.addNewTextParagraph();
                    XSLFTextRun r = p.addNewTextRun();
                    r.setText(stripInlineMarkdown(trimmed.substring(2)));
                    r.setFontSize(14.0);
                    r.setFontFamily(FONT_BODY);
                    r.setItalic(true);
                    r.setFontColor(new java.awt.Color(100, 100, 100));
                } else if (trimmed.startsWith("#") || trimmed.startsWith("```") || trimmed.startsWith("|")) {
                    // Skip nested headings, code fences, and tables in slides
                    continue;
                } else {
                    XSLFTextParagraph p = bodyShape.addNewTextParagraph();
                    XSLFTextRun r = p.addNewTextRun();
                    r.setText(stripInlineMarkdown(trimmed));
                    r.setFontSize(14.0);
                    r.setFontFamily(FONT_BODY);
                }
            }
        }
    }

    private void addTextOnlySlide(XSLFSlide slide, String heading, String body) {
        XSLFTextShape headingShape = slide.createTextBox();
        headingShape.setAnchor(new Rectangle2D.Double(54, 36, 612, 54));
        XSLFTextParagraph headingPara = headingShape.addNewTextParagraph();
        XSLFTextRun headingRun = headingPara.addNewTextRun();
        headingRun.setText(heading);
        headingRun.setBold(true);
        headingRun.setFontSize(24.0);
        headingRun.setFontFamily(FONT_HEADING);

        XSLFTextShape bodyShape = slide.createTextBox();
        bodyShape.setAnchor(new Rectangle2D.Double(54, 108, 612, 400));
        bodyShape.setWordWrap(true);

        if (StringUtils.isNotBlank(body)) {
            for (String line : body.split("\n")) {
                XSLFTextParagraph p = bodyShape.addNewTextParagraph();
                XSLFTextRun r = p.addNewTextRun();
                r.setText(stripInlineMarkdown(line.trim()));
                r.setFontSize(14.0);
                r.setFontFamily(FONT_BODY);
            }
        }
    }

    private void addEmptyContentSlide(XSLFSlide slide) {
        XSLFTextShape shape = slide.createTextBox();
        shape.setAnchor(new Rectangle2D.Double(100, 200, 520, 100));
        XSLFTextParagraph p = shape.addNewTextParagraph();
        p.setTextAlign(org.apache.poi.sl.usermodel.TextParagraph.TextAlign.CENTER);
        XSLFTextRun r = p.addNewTextRun();
        r.setText("(报告内容为空)");
        r.setFontSize(18.0);
        r.setFontFamily(FONT_BODY);
        r.setItalic(true);
        r.setFontColor(java.awt.Color.GRAY);
    }

    // ───────── utils ─────────

    private String resolveTypeLabel(String reportType) {
        switch (StringUtils.defaultString(reportType, "daily")) {
            case "daily":
                return "舆情日报";
            case "weekly":
                return "舆情周报";
            case "monthly":
                return "舆情月报";
            case "special":
                return "舆情专报";
            default:
                return "舆情报告";
        }
    }
}
