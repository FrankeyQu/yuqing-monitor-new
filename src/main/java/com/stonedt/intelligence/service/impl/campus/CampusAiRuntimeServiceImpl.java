package com.stonedt.intelligence.service.impl.campus;

import com.stonedt.intelligence.dao.campus.CampusAiDao;
import com.stonedt.intelligence.entity.campus.CampusAiCallLog;
import com.stonedt.intelligence.entity.campus.CampusAiFeatureBinding;
import com.stonedt.intelligence.entity.campus.CampusAiModel;
import com.stonedt.intelligence.entity.campus.CampusAiPromptTemplate;
import com.stonedt.intelligence.entity.campus.CampusAiProvider;
import com.stonedt.intelligence.service.campus.ai.CampusAiRuntimeConfig;
import com.stonedt.intelligence.service.campus.ai.CampusAiRuntimeService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class CampusAiRuntimeServiceImpl implements CampusAiRuntimeService {

    private static final Logger log = LoggerFactory.getLogger(CampusAiRuntimeServiceImpl.class);

    private final CampusAiDao campusAiDao;

    public CampusAiRuntimeServiceImpl(CampusAiDao campusAiDao) {
        this.campusAiDao = campusAiDao;
    }

    @Override
    public CampusAiRuntimeConfig resolveFeature(String featureCode,
                                                String defaultProviderCode,
                                                String defaultModelCode,
                                                String defaultEndpoint,
                                                String defaultCredentialRef,
                                                Integer defaultTimeoutMs) {
        CampusAiRuntimeConfig config = new CampusAiRuntimeConfig();
        config.setFeatureCode(featureCode);
        CampusAiFeatureBinding binding = StringUtils.isBlank(featureCode)
                ? null
                : campusAiDao.selectFeatureBinding(featureCode);

        String providerCode = StringUtils.defaultIfBlank(binding == null ? null : binding.getProviderCode(), defaultProviderCode);
        String modelCode = StringUtils.defaultIfBlank(binding == null ? null : binding.getModelCode(), defaultModelCode);
        config.setProviderCode(providerCode);
        config.setModelCode(modelCode);
        config.setFeatureEnabled(binding == null || binding.getEnabled() == null || binding.getEnabled() == 1);
        config.setFailureStrategy(StringUtils.defaultIfBlank(binding == null ? null : binding.getFailureStrategy(), "fail"));
        config.setLogPrompt(binding != null && binding.getLogPrompt() != null && binding.getLogPrompt() == 1);

        CampusAiProvider provider = StringUtils.isBlank(providerCode) ? null : campusAiDao.selectProvider(providerCode);
        config.setProviderEnabled(provider == null || provider.getEnabled() == null || provider.getEnabled() == 1);
        config.setEndpoint(StringUtils.defaultIfBlank(provider == null ? null : provider.getBaseUrl(), defaultEndpoint));
        config.setCredentialRef(StringUtils.defaultIfBlank(provider == null ? null : provider.getCredentialRef(), defaultCredentialRef));

        int timeout = defaultTimeoutMs == null || defaultTimeoutMs <= 0 ? 30000 : defaultTimeoutMs;
        if (provider != null && provider.getTimeoutMs() != null && provider.getTimeoutMs() > 0) {
            timeout = provider.getTimeoutMs();
        }
        if (binding != null && binding.getTimeoutMs() != null && binding.getTimeoutMs() > 0) {
            timeout = binding.getTimeoutMs();
        }
        config.setTimeoutMs(timeout);

        CampusAiModel model = null;
        if (StringUtils.isNotBlank(providerCode) && StringUtils.isNotBlank(modelCode)) {
            model = campusAiDao.selectModel(providerCode, modelCode);
        }
        if (model == null && StringUtils.isNotBlank(providerCode)) {
            model = campusAiDao.selectFirstEnabledModel(providerCode);
            if (model != null) {
                config.setModelCode(model.getModelCode());
            }
        }
        config.setModelEnabled(model == null || model.getEnabled() == null || model.getEnabled() == 1);
        config.setMaxTokens(model == null || model.getDefaultMaxTokens() == null ? 4096 : model.getDefaultMaxTokens());
        config.setTemperature(model == null || model.getDefaultTemperature() == null
                ? new BigDecimal("0.20")
                : model.getDefaultTemperature());
        return config;
    }

    @Override
    public CampusAiProvider getProvider(String providerCode) {
        if (StringUtils.isBlank(providerCode)) {
            return null;
        }
        return campusAiDao.selectProvider(providerCode);
    }

    @Override
    public String resolveProviderBaseUrl(String providerCode, String defaultBaseUrl) {
        CampusAiProvider provider = getProvider(providerCode);
        if (provider == null || provider.getEnabled() == null || provider.getEnabled() != 1) {
            return defaultBaseUrl;
        }
        return StringUtils.defaultIfBlank(provider.getBaseUrl(), defaultBaseUrl);
    }

    @Override
    public String resolveProviderCredentialRef(String providerCode, String defaultCredentialRef) {
        CampusAiProvider provider = getProvider(providerCode);
        if (provider == null || provider.getEnabled() == null || provider.getEnabled() != 1) {
            return defaultCredentialRef;
        }
        return StringUtils.defaultIfBlank(provider.getCredentialRef(), defaultCredentialRef);
    }

    @Override
    public int resolveProviderTimeoutMs(String providerCode, int defaultTimeoutMs) {
        CampusAiProvider provider = getProvider(providerCode);
        if (provider == null || provider.getEnabled() == null || provider.getEnabled() != 1
                || provider.getTimeoutMs() == null || provider.getTimeoutMs() <= 0) {
            return defaultTimeoutMs;
        }
        return provider.getTimeoutMs();
    }

    @Override
    public boolean isFeatureEnabled(String featureCode, boolean defaultEnabled) {
        if (StringUtils.isBlank(featureCode)) {
            return defaultEnabled;
        }
        CampusAiFeatureBinding binding = campusAiDao.selectFeatureBinding(featureCode);
        if (binding == null || binding.getEnabled() == null) {
            return defaultEnabled;
        }
        return binding.getEnabled() == 1;
    }

    @Override
    public String resolveCredential(String credentialRef, String fallbackValue) {
        if (StringUtils.isNotBlank(credentialRef)) {
            String key = credentialRef.trim();
            String env = System.getenv(key);
            if (StringUtils.isNotBlank(env)) {
                return env.trim();
            }
            String prop = System.getProperty(key);
            if (StringUtils.isNotBlank(prop)) {
                return prop.trim();
            }
        }
        String fallback = StringUtils.trimToNull(fallbackValue);
        if (fallback == null || "your-deepseek-api-key".equalsIgnoreCase(fallback)) {
            return null;
        }
        return fallback;
    }

    @Override
    public CampusAiPromptTemplate getActivePrompt(String featureCode) {
        if (StringUtils.isBlank(featureCode)) {
            return null;
        }
        return campusAiDao.selectActivePrompt(featureCode);
    }

    @Override
    public void recordCall(CampusAiCallLog callLog) {
        if (callLog == null) {
            return;
        }
        try {
            campusAiDao.insertCallLog(callLog);
        } catch (Exception ex) {
            log.warn("Failed to insert campus AI call log: {}", ex.getMessage());
        }
    }
}
