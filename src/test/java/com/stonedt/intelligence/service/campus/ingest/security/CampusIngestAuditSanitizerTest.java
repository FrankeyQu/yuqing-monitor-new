package com.stonedt.intelligence.service.campus.ingest.security;

import org.junit.Assert;
import org.junit.Test;

public class CampusIngestAuditSanitizerTest {

    @Test
    public void sanitizeJsonAndTextSecretsButKeepCredentialRefName() {
        String source = "{\"provider\":\"tikhub\",\"credentialRef\":\"TIKHUB_API_KEY\","
                + "\"apiKey\":\"sk_test_BATCH26_SECRET\","
                + "\"cookie\":\"session=batch26-secret\","
                + "\"signature\":\"batch26-signature\"}"
                + " Authorization: Bearer batch26-bearer-token";

        String sanitized = CampusIngestAuditSanitizer.sanitize(source);

        Assert.assertTrue(sanitized.contains("TIKHUB_API_KEY"));
        Assert.assertFalse(sanitized.contains("sk_test_BATCH26_SECRET"));
        Assert.assertFalse(sanitized.contains("batch26-secret"));
        Assert.assertFalse(sanitized.contains("batch26-signature"));
        Assert.assertFalse(sanitized.contains("batch26-bearer-token"));
        Assert.assertTrue(sanitized.contains("[REDACTED]"));
    }

    @Test
    public void sanitizeEscapedFetchConfigJsonString() {
        String source = "{\"fetchConfig\":\"{\\\"provider\\\":\\\"tikhub\\\","
                + "\\\"apiKey\\\":\\\"sk_test_ESCAPED_SECRET\\\","
                + "\\\"signature\\\":\\\"escaped-signature\\\"}\"}";

        String sanitized = CampusIngestAuditSanitizer.sanitize(source);

        Assert.assertFalse(sanitized.contains("sk_test_ESCAPED_SECRET"));
        Assert.assertFalse(sanitized.contains("escaped-signature"));
        Assert.assertTrue(sanitized.contains("[REDACTED]"));
    }
}
