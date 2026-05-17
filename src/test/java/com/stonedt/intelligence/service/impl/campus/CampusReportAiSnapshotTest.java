package com.stonedt.intelligence.service.impl.campus;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.stonedt.intelligence.vo.ReportDataVO;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CampusReportAiSnapshotTest {

    @Test
    public void reportDataSnapshotDoesNotEmitCircularReferences() {
        ReportDataVO vo = new ReportDataVO();
        List<Map<String, Object>> sharedPlatformRows = new ArrayList<>();
        Map<String, Object> platform = new HashMap<>();
        platform.put("name", "weibo");
        platform.put("value", 3);
        sharedPlatformRows.add(platform);
        vo.setMediaDistribution(sharedPlatformRows);
        vo.setPlatformRanking(sharedPlatformRows);
        vo.setTotalCount(3);
        vo.setNegativeCount(1);

        String json = CampusReportServiceImpl.reportDataSnapshot(vo).toJSONString();
        JSONObject parsed = JSON.parseObject(json);

        Assert.assertFalse(json.contains("$ref"));
        Assert.assertEquals(3, parsed.getInteger("totalCount").intValue());
        Assert.assertEquals(1, parsed.getJSONArray("mediaDistribution").size());
        Assert.assertEquals(1, parsed.getJSONArray("platformRanking").size());
    }
}
