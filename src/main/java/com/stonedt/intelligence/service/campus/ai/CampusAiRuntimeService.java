package com.stonedt.intelligence.service.campus.ai;

import com.stonedt.intelligence.entity.campus.CampusAiCallLog;
import com.stonedt.intelligence.entity.campus.CampusAiPromptTemplate;
import com.stonedt.intelligence.entity.campus.CampusAiProvider;

public interface CampusAiRuntimeService {

    CampusAiRuntimeConfig resolveFeature(String featureCode,
                                         String defaultProviderCode,
                                         String defaultModelCode,
                                         String defaultEndpoint,
                                         String defaultCredentialRef,
                                         Integer defaultTimeoutMs);

    CampusAiProvider getProvider(String providerCode);

    String resolveProviderBaseUrl(String providerCode, String defaultBaseUrl);

    String resolveProviderCredentialRef(String providerCode, String defaultCredentialRef);

    int resolveProviderTimeoutMs(String providerCode, int defaultTimeoutMs);

    boolean isFeatureEnabled(String featureCode, boolean defaultEnabled);

    String resolveCredential(String credentialRef, String fallbackValue);

    CampusAiPromptTemplate getActivePrompt(String featureCode);

    void recordCall(CampusAiCallLog callLog);
}
