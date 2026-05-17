package com.stonedt.intelligence.service.campus.ingest.normalize;

import org.junit.Assert;
import org.junit.Test;

public class CampusIngestDedupResultTest {

    @Test
    public void duplicateExternalIdIsDuplicateButNotInserted() {
        CampusIngestDedupResult result = CampusIngestDedupResult.duplicateExternalId(100L);

        Assert.assertFalse(result.isInserted());
        Assert.assertTrue(result.isDuplicate());
        Assert.assertFalse(result.isInvalid());
        Assert.assertEquals("duplicate_external_id", result.getStatus());
        Assert.assertEquals(100L, result.getRecordId().longValue());
    }

    @Test
    public void invalidCarriesMessage() {
        CampusIngestDedupResult result = CampusIngestDedupResult.invalid("empty");

        Assert.assertFalse(result.isInserted());
        Assert.assertFalse(result.isDuplicate());
        Assert.assertTrue(result.isInvalid());
        Assert.assertEquals("invalid", result.getStatus());
        Assert.assertEquals("empty", result.getMessage());
    }
}
