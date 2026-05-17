package com.stonedt.intelligence.entity.campus;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

public class CampusReportIdSerializationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void reportIdsSerializeAsStrings() throws Exception {
        CampusReport report = new CampusReport();
        report.setReportId(2055914489227907072L);
        report.setTemplateId(2055914489227907073L);
        report.setEventId(2055914489227907074L);

        String json = objectMapper.writeValueAsString(report);

        Assert.assertTrue(json.contains("\"reportId\":\"2055914489227907072\""));
        Assert.assertTrue(json.contains("\"templateId\":\"2055914489227907073\""));
        Assert.assertTrue(json.contains("\"eventId\":\"2055914489227907074\""));
    }

    @Test
    public void autoReportIdsSerializeAsStrings() throws Exception {
        CampusReportTemplate template = new CampusReportTemplate();
        template.setTemplateId(2055914489227907072L);

        CampusReportJob job = new CampusReportJob();
        job.setReportJobId(2055913991108169728L);
        job.setTemplateId(2055914489227907072L);
        job.setEventId(2055914489227907073L);

        CampusReportGenerationLog log = new CampusReportGenerationLog();
        log.setGenerationLogId(2055913991108169729L);
        log.setReportJobId(2055913991108169728L);
        log.setReportId(2055914489227907072L);

        String templateJson = objectMapper.writeValueAsString(template);
        String jobJson = objectMapper.writeValueAsString(job);
        String logJson = objectMapper.writeValueAsString(log);

        Assert.assertTrue(templateJson.contains("\"templateId\":\"2055914489227907072\""));
        Assert.assertTrue(jobJson.contains("\"reportJobId\":\"2055913991108169728\""));
        Assert.assertTrue(jobJson.contains("\"templateId\":\"2055914489227907072\""));
        Assert.assertTrue(jobJson.contains("\"eventId\":\"2055914489227907073\""));
        Assert.assertTrue(logJson.contains("\"generationLogId\":\"2055913991108169729\""));
        Assert.assertTrue(logJson.contains("\"reportJobId\":\"2055913991108169728\""));
        Assert.assertTrue(logJson.contains("\"reportId\":\"2055914489227907072\""));
    }
}
