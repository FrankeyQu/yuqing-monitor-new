package com.stonedt.intelligence.service.campus.ingest.tikhub;

import com.stonedt.intelligence.entity.campus.CampusIngestTask;
import com.stonedt.intelligence.service.campus.ingest.CampusIngestFetchRequest;
import org.junit.Assert;
import org.junit.Test;

public class TikhubCredentialResolverTest {

    @Test
    public void resolveCustomCredentialRefFromSystemProperty() {
        String credentialRef = "YUQING_TIKHUB_KEY";
        System.setProperty(credentialRef, "secret-value");
        try {
            TikhubFetchConfig fetchConfig = fromJson(credentialRef);
            Assert.assertEquals(credentialRef, fetchConfig.getCredentialRef());

            String credential = new TikhubCredentialResolver().resolve(fetchConfig);
            Assert.assertEquals("secret-value", credential);
        } finally {
            System.clearProperty(credentialRef);
        }
    }

    private TikhubFetchConfig fromJson(String credentialRef) {
        CampusIngestTask task = new CampusIngestTask();
        task.setFetchConfig("{\"provider\":\"tikhub\",\"endpointKey\":\"douyin_search_video_v2\","
                + "\"platform\":\"douyin\",\"query\":\"school canteen\",\"limit\":10,"
                + "\"credentialRef\":\"" + credentialRef + "\"}");
        CampusIngestFetchRequest request = new CampusIngestFetchRequest();
        request.setTask(task);
        return TikhubFetchConfig.fromRequest(request);
    }
}
