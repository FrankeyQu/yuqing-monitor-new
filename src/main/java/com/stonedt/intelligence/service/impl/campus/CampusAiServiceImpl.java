package com.stonedt.intelligence.service.impl.campus;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.stonedt.intelligence.dao.campus.CampusAiDao;
import com.stonedt.intelligence.entity.campus.CampusAiCallLog;
import com.stonedt.intelligence.entity.campus.CampusAiFeatureBinding;
import com.stonedt.intelligence.entity.campus.CampusAiModel;
import com.stonedt.intelligence.entity.campus.CampusAiPromptTemplate;
import com.stonedt.intelligence.entity.campus.CampusAiProvider;
import com.stonedt.intelligence.service.campus.ai.CampusAiRuntimeService;
import com.stonedt.intelligence.service.campus.ai.CampusAiService;
import com.stonedt.intelligence.util.SnowflakeUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class CampusAiServiceImpl implements CampusAiService {

    private final CampusAiDao campusAiDao;
    private final CampusAiRuntimeService campusAiRuntimeService;

    public CampusAiServiceImpl(CampusAiDao campusAiDao,
                               CampusAiRuntimeService campusAiRuntimeService) {
        this.campusAiDao = campusAiDao;
        this.campusAiRuntimeService = campusAiRuntimeService;
    }

    @Override
    public Map<String, Object> overview() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, -24);
        Date since = calendar.getTime();
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("activeProviderCount", campusAiDao.countActiveProviders());
        result.put("enabledFeatureCount", campusAiDao.countEnabledFeatures());
        result.put("legacyFeatureCount", campusAiDao.countLegacyFeatures());
        result.put("failedCallCount24h", campusAiDao.countFailedCallsSince(since));
        result.put("callStatus24h", campusAiDao.countCallsByStatusSince(since));
        return result;
    }

    @Override
    public PageInfo<CampusAiProvider> listProviders(Integer pageNum, Integer pageSize,
                                                    String keyword, String providerType, Integer enabled) {
        PageHelper.startPage(defaultPageNum(pageNum), defaultPageSize(pageSize));
        return new PageInfo<CampusAiProvider>(campusAiDao.listProviders(keyword, providerType, enabled));
    }

    @Override
    public CampusAiProvider saveProvider(CampusAiProvider provider, Long operatorUserId) {
        validateProvider(provider);
        setProviderDefaults(provider, operatorUserId);
        CampusAiProvider old = campusAiDao.selectProvider(provider.getProviderCode());
        if (old == null) {
            if (provider.getProviderId() == null) {
                provider.setProviderId(SnowflakeUtil.getId());
            }
            campusAiDao.insertProvider(provider);
        } else {
            provider.setProviderId(old.getProviderId());
            campusAiDao.updateProvider(provider);
        }
        return campusAiDao.selectProvider(provider.getProviderCode());
    }

    @Override
    public void deleteProvider(String providerCode, Long operatorUserId) {
        if (StringUtils.isBlank(providerCode)) {
            throw new IllegalArgumentException("供应商编码不能为空");
        }
        campusAiDao.logicalDeleteProvider(providerCode, operatorUserId);
    }

    @Override
    public PageInfo<CampusAiModel> listModels(Integer pageNum, Integer pageSize,
                                              String providerCode, String keyword, Integer enabled) {
        PageHelper.startPage(defaultPageNum(pageNum), defaultPageSize(pageSize));
        return new PageInfo<CampusAiModel>(campusAiDao.listModels(providerCode, keyword, enabled));
    }

    @Override
    public CampusAiModel saveModel(CampusAiModel model, Long operatorUserId) {
        validateModel(model);
        setModelDefaults(model, operatorUserId);
        CampusAiModel old = campusAiDao.selectModel(model.getProviderCode(), model.getModelCode());
        if (old == null) {
            if (model.getModelId() == null) {
                model.setModelId(SnowflakeUtil.getId());
            }
            campusAiDao.insertModel(model);
        } else {
            model.setModelId(old.getModelId());
            campusAiDao.updateModel(model);
        }
        return campusAiDao.selectModel(model.getProviderCode(), model.getModelCode());
    }

    @Override
    public void deleteModel(String providerCode, String modelCode, Long operatorUserId) {
        if (StringUtils.isAnyBlank(providerCode, modelCode)) {
            throw new IllegalArgumentException("供应商编码和模型编码不能为空");
        }
        campusAiDao.logicalDeleteModel(providerCode, modelCode, operatorUserId);
    }

    @Override
    public PageInfo<CampusAiFeatureBinding> listFeatureBindings(Integer pageNum, Integer pageSize,
                                                                String keyword, String featureType, Integer enabled) {
        PageHelper.startPage(defaultPageNum(pageNum), defaultPageSize(pageSize));
        return new PageInfo<CampusAiFeatureBinding>(campusAiDao.listFeatureBindings(keyword, featureType, enabled));
    }

    @Override
    public CampusAiFeatureBinding saveFeatureBinding(CampusAiFeatureBinding binding, Long operatorUserId) {
        validateFeatureBinding(binding);
        setFeatureDefaults(binding, operatorUserId);
        CampusAiFeatureBinding old = campusAiDao.selectFeatureBinding(binding.getFeatureCode());
        if (old == null) {
            if (binding.getBindingId() == null) {
                binding.setBindingId(SnowflakeUtil.getId());
            }
            campusAiDao.insertFeatureBinding(binding);
        } else {
            binding.setBindingId(old.getBindingId());
            campusAiDao.updateFeatureBinding(binding);
        }
        return campusAiDao.selectFeatureBinding(binding.getFeatureCode());
    }

    @Override
    public PageInfo<CampusAiPromptTemplate> listPromptTemplates(Integer pageNum, Integer pageSize,
                                                                String featureCode, String keyword, Integer enabled) {
        PageHelper.startPage(defaultPageNum(pageNum), defaultPageSize(pageSize));
        return new PageInfo<CampusAiPromptTemplate>(campusAiDao.listPromptTemplates(featureCode, keyword, enabled));
    }

    @Override
    public CampusAiPromptTemplate savePromptTemplate(CampusAiPromptTemplate promptTemplate, Long operatorUserId) {
        validatePromptTemplate(promptTemplate);
        setPromptDefaults(promptTemplate, operatorUserId);
        CampusAiPromptTemplate old = promptTemplate.getTemplateId() == null
                ? null
                : campusAiDao.selectPromptTemplate(promptTemplate.getTemplateId());
        if (old == null) {
            promptTemplate.setTemplateId(SnowflakeUtil.getId());
            campusAiDao.insertPromptTemplate(promptTemplate);
        } else {
            campusAiDao.updatePromptTemplate(promptTemplate);
        }
        return campusAiDao.selectPromptTemplate(promptTemplate.getTemplateId());
    }

    @Override
    public void deletePromptTemplate(Long templateId, Long operatorUserId) {
        if (templateId == null) {
            throw new IllegalArgumentException("提示词模板ID不能为空");
        }
        campusAiDao.logicalDeletePromptTemplate(templateId, operatorUserId);
    }

    @Override
    public PageInfo<CampusAiCallLog> listCallLogs(Integer pageNum, Integer pageSize,
                                                  String featureCode, String providerCode, String callStatus) {
        PageHelper.startPage(defaultPageNum(pageNum), defaultPageSize(pageSize));
        return new PageInfo<CampusAiCallLog>(campusAiDao.listCallLogs(featureCode, providerCode, callStatus));
    }

    @Override
    public Map<String, Object> testProvider(String providerCode) {
        if (StringUtils.isBlank(providerCode)) {
            throw new IllegalArgumentException("供应商编码不能为空");
        }
        CampusAiProvider provider = campusAiDao.selectProvider(providerCode);
        if (provider == null) {
            throw new IllegalArgumentException("供应商不存在");
        }
        String credentialRef = StringUtils.defaultString(provider.getCredentialRef());
        String credential = campusAiRuntimeService.resolveCredential(credentialRef, null);
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("providerCode", provider.getProviderCode());
        result.put("providerName", provider.getProviderName());
        result.put("enabled", provider.getEnabled());
        result.put("baseUrl", provider.getBaseUrl());
        result.put("credentialRef", credentialRef);
        result.put("credentialConfigured", StringUtils.isNotBlank(credential));
        result.put("ready", provider.getEnabled() != null && provider.getEnabled() == 1
                && StringUtils.isNotBlank(provider.getBaseUrl())
                && ("none".equalsIgnoreCase(provider.getAuthType()) || StringUtils.isNotBlank(credential)));
        if (provider.getEnabled() == null || provider.getEnabled() != 1) {
            result.put("status", "disabled");
            result.put("message", "供应商已停用");
        } else if (StringUtils.isBlank(provider.getBaseUrl())) {
            result.put("status", "endpoint_missing");
            result.put("message", "接入点未配置");
        } else if (!"none".equalsIgnoreCase(provider.getAuthType()) && StringUtils.isBlank(credential)) {
            result.put("status", "credential_missing");
            result.put("message", "未读取到密钥环境变量或系统属性：" + credentialRef);
        } else {
            result.put("status", "ready");
            result.put("message", "配置可用；实际联网调用由业务功能触发");
        }
        return result;
    }

    private void validateProvider(CampusAiProvider provider) {
        if (provider == null) {
            throw new IllegalArgumentException("供应商信息不能为空");
        }
        if (StringUtils.isBlank(provider.getProviderCode())) {
            throw new IllegalArgumentException("供应商编码不能为空");
        }
        if (StringUtils.isBlank(provider.getProviderName())) {
            throw new IllegalArgumentException("供应商名称不能为空");
        }
        if (StringUtils.isBlank(provider.getProviderType())) {
            throw new IllegalArgumentException("供应商类型不能为空");
        }
    }

    private void validateModel(CampusAiModel model) {
        if (model == null) {
            throw new IllegalArgumentException("模型信息不能为空");
        }
        if (StringUtils.isAnyBlank(model.getProviderCode(), model.getModelCode(), model.getModelName())) {
            throw new IllegalArgumentException("供应商编码、模型编码和模型名称不能为空");
        }
        if (campusAiDao.selectProvider(model.getProviderCode()) == null) {
            throw new IllegalArgumentException("供应商不存在");
        }
    }

    private void validateFeatureBinding(CampusAiFeatureBinding binding) {
        if (binding == null) {
            throw new IllegalArgumentException("功能绑定不能为空");
        }
        if (StringUtils.isAnyBlank(binding.getFeatureCode(), binding.getFeatureName(), binding.getFeatureType())) {
            throw new IllegalArgumentException("功能编码、名称和类型不能为空");
        }
        if (StringUtils.isNotBlank(binding.getProviderCode()) && campusAiDao.selectProvider(binding.getProviderCode()) == null) {
            throw new IllegalArgumentException("主供应商不存在");
        }
        if (StringUtils.isNoneBlank(binding.getProviderCode(), binding.getModelCode())
                && campusAiDao.selectModel(binding.getProviderCode(), binding.getModelCode()) == null) {
            throw new IllegalArgumentException("主模型不存在");
        }
    }

    private void validatePromptTemplate(CampusAiPromptTemplate promptTemplate) {
        if (promptTemplate == null) {
            throw new IllegalArgumentException("提示词模板不能为空");
        }
        if (StringUtils.isAnyBlank(promptTemplate.getFeatureCode(), promptTemplate.getTemplateName())) {
            throw new IllegalArgumentException("功能编码和模板名称不能为空");
        }
    }

    private void setProviderDefaults(CampusAiProvider provider, Long operatorUserId) {
        provider.setProviderCode(provider.getProviderCode().trim());
        provider.setProviderType(StringUtils.defaultIfBlank(provider.getProviderType(), "llm"));
        provider.setAuthType(StringUtils.defaultIfBlank(provider.getAuthType(), "bearer"));
        provider.setEnabled(provider.getEnabled() == null ? 1 : provider.getEnabled());
        provider.setTimeoutMs(provider.getTimeoutMs() == null ? 30000 : Math.max(provider.getTimeoutMs(), 1000));
        provider.setMaxRetries(provider.getMaxRetries() == null ? 0 : Math.max(provider.getMaxRetries(), 0));
        provider.setQuotaUsedToday(provider.getQuotaUsedToday() == null ? 0 : Math.max(provider.getQuotaUsedToday(), 0));
        provider.setDeleted(0);
        provider.setCreateUserId(operatorUserId);
        provider.setUpdateUserId(operatorUserId);
    }

    private void setModelDefaults(CampusAiModel model, Long operatorUserId) {
        model.setProviderCode(model.getProviderCode().trim());
        model.setModelCode(model.getModelCode().trim());
        model.setDefaultTemperature(model.getDefaultTemperature() == null ? new BigDecimal("0.20") : model.getDefaultTemperature());
        model.setDefaultMaxTokens(model.getDefaultMaxTokens() == null ? 4096 : Math.max(model.getDefaultMaxTokens(), 1));
        model.setSupportStream(model.getSupportStream() == null ? 1 : model.getSupportStream());
        model.setEnabled(model.getEnabled() == null ? 1 : model.getEnabled());
        model.setDeleted(0);
        model.setCreateUserId(operatorUserId);
        model.setUpdateUserId(operatorUserId);
    }

    private void setFeatureDefaults(CampusAiFeatureBinding binding, Long operatorUserId) {
        binding.setFeatureCode(binding.getFeatureCode().trim());
        binding.setFeatureType(StringUtils.defaultIfBlank(binding.getFeatureType(), "llm"));
        binding.setEnabled(binding.getEnabled() == null ? 1 : binding.getEnabled());
        binding.setFailureStrategy(StringUtils.defaultIfBlank(binding.getFailureStrategy(), "fail"));
        binding.setLogPrompt(binding.getLogPrompt() == null ? 0 : binding.getLogPrompt());
        binding.setQuotaUsedToday(binding.getQuotaUsedToday() == null ? 0 : Math.max(binding.getQuotaUsedToday(), 0));
        binding.setDeleted(0);
        binding.setCreateUserId(operatorUserId);
        binding.setUpdateUserId(operatorUserId);
    }

    private void setPromptDefaults(CampusAiPromptTemplate promptTemplate, Long operatorUserId) {
        promptTemplate.setFeatureCode(promptTemplate.getFeatureCode().trim());
        promptTemplate.setTemplateVersion(StringUtils.defaultIfBlank(promptTemplate.getTemplateVersion(), "v1"));
        promptTemplate.setEnabled(promptTemplate.getEnabled() == null ? 1 : promptTemplate.getEnabled());
        promptTemplate.setDeleted(0);
        promptTemplate.setCreateUserId(operatorUserId);
        promptTemplate.setUpdateUserId(operatorUserId);
    }

    private int defaultPageNum(Integer pageNum) {
        return pageNum == null || pageNum < 1 ? 1 : pageNum;
    }

    private int defaultPageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return 10;
        }
        return Math.min(pageSize, 100);
    }
}
