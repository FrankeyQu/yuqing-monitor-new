package com.stonedt.intelligence.service.campus.ingest;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class CampusIngestRecordMapperContractTest {

    @Test
    public void uniqueKeyDuplicateLookupsIncludeSoftDeletedRecords() throws Exception {
        String xml = loadMapperXml();

        String externalIdLookup = selectBody(xml, "selectDuplicateByExternalId");
        String contentHashLookup = selectBody(xml, "selectDuplicateByContentHash");
        String platformTitleLookup = selectBody(xml, "selectDuplicateByPlatformTitle");

        Assert.assertTrue(externalIdLookup.contains("external_id = #{externalid}"));
        Assert.assertFalse(externalIdLookup.contains("deleted = 0"));
        Assert.assertTrue(contentHashLookup.contains("content_hash = #{contenthash}"));
        Assert.assertFalse(contentHashLookup.contains("deleted = 0"));
        Assert.assertTrue(platformTitleLookup.contains("deleted = 0"));
    }

    private static String selectBody(String xml, String id) {
        String marker = "<select id=\"" + id + "\"";
        int start = xml.indexOf(marker);
        Assert.assertTrue("Missing mapper select: " + id, start >= 0);
        int bodyStart = xml.indexOf('>', start) + 1;
        int end = xml.indexOf("</select>", bodyStart);
        Assert.assertTrue("Unclosed mapper select: " + id, end > bodyStart);
        return xml.substring(bodyStart, end)
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ");
    }

    private static String loadMapperXml() throws Exception {
        InputStream inputStream = CampusIngestRecordMapperContractTest.class
                .getClassLoader()
                .getResourceAsStream("mapper/campus/CampusIngestRecordMapper.xml");
        Assert.assertNotNull(inputStream);
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
            return new String(output.toByteArray(), StandardCharsets.UTF_8);
        } finally {
            inputStream.close();
        }
    }
}
