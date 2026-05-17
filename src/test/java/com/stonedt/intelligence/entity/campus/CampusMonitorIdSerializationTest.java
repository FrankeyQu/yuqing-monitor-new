package com.stonedt.intelligence.entity.campus;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

public class CampusMonitorIdSerializationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void monitorInformationIdsSerializeAsStrings() throws Exception {
        CampusMonitorInformation information = new CampusMonitorInformation();
        information.setInfoId(2055870813135048704L);
        information.setMonitorResultId(2055870813135048704L);
        information.setMonitorTaskId(2054480494304825344L);
        information.setClueId(2055870813135048705L);
        information.setIngestRecordId(2055870813135048706L);
        information.setAlertId(2055870813135048707L);

        String json = objectMapper.writeValueAsString(information);

        Assert.assertTrue(json.contains("\"infoId\":\"2055870813135048704\""));
        Assert.assertTrue(json.contains("\"monitorResultId\":\"2055870813135048704\""));
        Assert.assertTrue(json.contains("\"monitorTaskId\":\"2054480494304825344\""));
        Assert.assertTrue(json.contains("\"clueId\":\"2055870813135048705\""));
        Assert.assertTrue(json.contains("\"ingestRecordId\":\"2055870813135048706\""));
        Assert.assertTrue(json.contains("\"alertId\":\"2055870813135048707\""));
    }

    @Test
    public void monitorActionIdsSerializeAsStrings() throws Exception {
        CampusMonitorResult result = new CampusMonitorResult();
        result.setMonitorResultId(2055870813135048704L);
        result.setMonitorTaskId(2054480494304825344L);
        result.setClueId(2055870813135048705L);
        result.setAlertId(2055870813135048706L);

        CampusMonitorWatchTarget target = new CampusMonitorWatchTarget();
        target.setTargetId(2055870813135048707L);
        target.setMonitorTaskId(2054480494304825344L);
        target.setSourceObjectId(2055870813135048704L);

        String resultJson = objectMapper.writeValueAsString(result);
        String targetJson = objectMapper.writeValueAsString(target);

        Assert.assertTrue(resultJson.contains("\"monitorResultId\":\"2055870813135048704\""));
        Assert.assertTrue(resultJson.contains("\"monitorTaskId\":\"2054480494304825344\""));
        Assert.assertTrue(resultJson.contains("\"clueId\":\"2055870813135048705\""));
        Assert.assertTrue(resultJson.contains("\"alertId\":\"2055870813135048706\""));
        Assert.assertTrue(targetJson.contains("\"targetId\":\"2055870813135048707\""));
        Assert.assertTrue(targetJson.contains("\"monitorTaskId\":\"2054480494304825344\""));
        Assert.assertTrue(targetJson.contains("\"sourceObjectId\":\"2055870813135048704\""));
    }
}
