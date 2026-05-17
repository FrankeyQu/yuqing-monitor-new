package com.stonedt.intelligence.service.campus.ingest.normalize;

import com.stonedt.intelligence.entity.campus.CampusIngestRecord;
import com.stonedt.intelligence.entity.campus.CampusIngestSource;
import com.stonedt.intelligence.entity.campus.CampusIngestTask;
import com.stonedt.intelligence.service.campus.ingest.CampusIngestItem;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

public class CampusIngestRecordNormalizerTest {

    private final CampusIngestRecordNormalizer normalizer =
            new CampusIngestRecordNormalizer(new CampusIngestRawDataSanitizer());

    @Test
    public void normalizeBuildsStableRecordAndSanitizesRawData() {
        CampusIngestItem item = new CampusIngestItem();
        item.setExternalId(" ext-001 ");
        item.setContent("  Campus discussion sample content  ");
        item.setOriginalUrl(" https://example.edu/post/1 ");
        item.setPublishTime(new Date(1700000000000L));
        item.setAuthorName(" Student Observer ");
        item.setRawData("{\"access_token\":\"secret-token\",\"cookie\":\"sid=secret\","
                + "\"deviceId\":\"device-secret\",\"desc\":\"safe\"}");

        CampusIngestRecord record = normalizer.normalize(11L, task(), source(), item, 9L);

        Assert.assertEquals(11L, record.getRunId().longValue());
        Assert.assertEquals(1L, record.getSourceId().longValue());
        Assert.assertEquals(2L, record.getTaskId().longValue());
        Assert.assertEquals("ext-001", record.getExternalId());
        Assert.assertEquals("douyin", record.getPlatform());
        Assert.assertEquals("article", record.getContentType());
        Assert.assertEquals("Campus discussion sample content", record.getTitle());
        Assert.assertEquals("Student Observer", record.getAuthorName());
        Assert.assertEquals("pending", record.getNormalizedStatus());
        Assert.assertEquals("normal", record.getRiskLevel());
        Assert.assertNotNull(record.getContentHash());
        Assert.assertEquals(64, record.getContentHash().length());
        Assert.assertTrue(record.getRawData().contains("[REDACTED]"));
        Assert.assertFalse(record.getRawData().contains("secret-token"));
        Assert.assertFalse(record.getRawData().contains("sid=secret"));
        Assert.assertFalse(record.getRawData().contains("device-secret"));
    }

    @Test
    public void invalidWhenNoContentUrlOrExternalId() {
        CampusIngestRecord record = normalizer.normalize(11L, task(), source(), new CampusIngestItem(), 9L);

        Assert.assertTrue(normalizer.isInvalid(record));
    }

    @Test
    public void explicitContentHashIsPreserved() {
        CampusIngestItem item = new CampusIngestItem();
        item.setExternalId("ext-002");
        item.setContentHash("explicit-hash");

        CampusIngestRecord record = normalizer.normalize(11L, task(), source(), item, 9L);

        Assert.assertEquals("explicit-hash", record.getContentHash());
    }

    @Test
    public void normalizeStripsPlatformHighlightHtml() {
        CampusIngestItem item = new CampusIngestItem();
        item.setTitle("【26<em class=\"keyword\">新大</em>考研】");
        item.setContent("来自<em class=\"keyword\">新疆大学</em>&nbsp;的公开信息");
        item.setAuthorName("<em class=\"keyword\">Bili</em> Observer");

        CampusIngestRecord record = normalizer.normalize(11L, task(), source(), item, 9L);

        Assert.assertEquals("【26新大考研】", record.getTitle());
        Assert.assertEquals("来自新疆大学 的公开信息", record.getContent());
        Assert.assertEquals("Bili Observer", record.getAuthorName());
    }

    private CampusIngestSource source() {
        CampusIngestSource source = new CampusIngestSource();
        source.setSourceId(1L);
        source.setPlatform("Douyin");
        return source;
    }

    private CampusIngestTask task() {
        CampusIngestTask task = new CampusIngestTask();
        task.setTaskId(2L);
        return task;
    }
}
