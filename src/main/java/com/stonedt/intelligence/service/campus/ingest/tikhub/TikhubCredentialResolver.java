package com.stonedt.intelligence.service.campus.ingest.tikhub;

import com.stonedt.intelligence.service.campus.ai.CampusAiRuntimeService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TikhubCredentialResolver {

    private final CampusAiRuntimeService campusAiRuntimeService;

    @Autowired
    public TikhubCredentialResolver(CampusAiRuntimeService campusAiRuntimeService) {
        this.campusAiRuntimeService = campusAiRuntimeService;
    }

    public TikhubCredentialResolver() {
        this.campusAiRuntimeService = null;
    }

    public String resolve(TikhubFetchConfig fetchConfig) {
        if (fetchConfig == null) {
            throw new TikhubIngestException("TikHub fetch_config is required");
        }
        String credentialRef = StringUtils.defaultIfBlank(fetchConfig.getCredentialRef(),
                TikhubFetchConfig.DEFAULT_CREDENTIAL_REF);
        if (campusAiRuntimeService != null) {
            credentialRef = campusAiRuntimeService.resolveProviderCredentialRef("tikhub", credentialRef);
            String credential = campusAiRuntimeService.resolveCredential(credentialRef, null);
            if (StringUtils.isNotBlank(credential)) {
                return credential;
            }
        }
        String credential = System.getenv(credentialRef);
        if (StringUtils.isBlank(credential)) {
            credential = System.getProperty(credentialRef);
        }
        if (StringUtils.isBlank(credential)) {
            throw new TikhubIngestException("TikHub credential is not configured: " + credentialRef);
        }
        return credential;
    }
}
