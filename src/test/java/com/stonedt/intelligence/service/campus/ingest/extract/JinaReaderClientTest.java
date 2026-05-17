package com.stonedt.intelligence.service.campus.ingest.extract;

import org.junit.Assert;
import org.junit.Test;

public class JinaReaderClientTest {

    @Test
    public void parseReaderMarkdownMetadataAndBody() {
        JinaReaderClient client = new JinaReaderClient(null);

        ContentExtractionResult result = client.parse("https://example.edu/news/1",
                "Title: Campus News\n"
                        + "URL Source: https://example.edu/news/1\n\n"
                        + "Markdown Content:\n"
                        + "# Campus News\n\n正文内容");

        Assert.assertEquals("Campus News", result.getTitle());
        Assert.assertEquals("https://example.edu/news/1", result.getSourceUrl());
        Assert.assertEquals("jina_reader", result.getProvider());
        Assert.assertTrue(result.getContent().contains("正文内容"));
        Assert.assertFalse(result.getContent().contains("URL Source:"));
    }
}
