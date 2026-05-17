package com.stonedt.intelligence.service.campus.ingest.baidu;

import com.stonedt.intelligence.entity.campus.CampusIngestTask;
import com.stonedt.intelligence.service.campus.ingest.CampusIngestFetchRequest;
import com.stonedt.intelligence.service.campus.ingest.CampusIngestItem;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class BaiduIngestResponseMapperTest {

    @Test
    public void mapBaiduReferenceAsNewsPlatform() {
        BaiduIngestResponseMapper mapper = new BaiduIngestResponseMapper();

        List<CampusIngestItem> items = mapper.map(config(),
                "{\"references\":[{\"title\":\"新疆教育政策\",\"url\":\"https://news.example.edu.cn/a\","
                        + "\"content\":\"政策正文摘要\",\"date\":\"2026-05-15\"}]}");

        Assert.assertEquals(1, items.size());
        Assert.assertEquals("news", items.get(0).getPlatform());
        Assert.assertEquals("政策正文摘要", items.get(0).getContent());
    }

    private BaiduIngestFetchConfig config() {
        CampusIngestTask task = new CampusIngestTask();
        task.setFetchConfig("{\"provider\":\"baidu\",\"query\":\"新疆 教育\",\"topK\":10}");
        CampusIngestFetchRequest request = new CampusIngestFetchRequest();
        request.setTask(task);
        return BaiduIngestFetchConfig.fromRequest(request);
    }
}
