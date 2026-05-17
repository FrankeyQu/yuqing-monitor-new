package com.stonedt.intelligence.service.campus.ai;

import com.github.pagehelper.PageInfo;
import com.stonedt.intelligence.entity.campus.CampusAiCallLog;
import com.stonedt.intelligence.entity.campus.CampusAiFeatureBinding;
import com.stonedt.intelligence.entity.campus.CampusAiModel;
import com.stonedt.intelligence.entity.campus.CampusAiPromptTemplate;
import com.stonedt.intelligence.entity.campus.CampusAiProvider;

import java.util.Map;

public interface CampusAiService {

    Map<String, Object> overview();

    PageInfo<CampusAiProvider> listProviders(Integer pageNum, Integer pageSize,
                                             String keyword, String providerType, Integer enabled);

    CampusAiProvider saveProvider(CampusAiProvider provider, Long operatorUserId);

    void deleteProvider(String providerCode, Long operatorUserId);

    PageInfo<CampusAiModel> listModels(Integer pageNum, Integer pageSize,
                                       String providerCode, String keyword, Integer enabled);

    CampusAiModel saveModel(CampusAiModel model, Long operatorUserId);

    void deleteModel(String providerCode, String modelCode, Long operatorUserId);

    PageInfo<CampusAiFeatureBinding> listFeatureBindings(Integer pageNum, Integer pageSize,
                                                         String keyword, String featureType, Integer enabled);

    CampusAiFeatureBinding saveFeatureBinding(CampusAiFeatureBinding binding, Long operatorUserId);

    PageInfo<CampusAiPromptTemplate> listPromptTemplates(Integer pageNum, Integer pageSize,
                                                         String featureCode, String keyword, Integer enabled);

    CampusAiPromptTemplate savePromptTemplate(CampusAiPromptTemplate promptTemplate, Long operatorUserId);

    void deletePromptTemplate(Long templateId, Long operatorUserId);

    PageInfo<CampusAiCallLog> listCallLogs(Integer pageNum, Integer pageSize,
                                           String featureCode, String providerCode, String callStatus);

    Map<String, Object> testProvider(String providerCode);
}
