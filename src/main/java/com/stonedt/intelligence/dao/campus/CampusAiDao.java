package com.stonedt.intelligence.dao.campus;

import com.stonedt.intelligence.entity.campus.CampusAiCallLog;
import com.stonedt.intelligence.entity.campus.CampusAiFeatureBinding;
import com.stonedt.intelligence.entity.campus.CampusAiModel;
import com.stonedt.intelligence.entity.campus.CampusAiPromptTemplate;
import com.stonedt.intelligence.entity.campus.CampusAiProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Mapper
public interface CampusAiDao {

    int insertProvider(CampusAiProvider provider);

    int updateProvider(CampusAiProvider provider);

    int logicalDeleteProvider(@Param("providerCode") String providerCode,
                              @Param("updateUserId") Long updateUserId);

    CampusAiProvider selectProvider(@Param("providerCode") String providerCode);

    List<CampusAiProvider> listProviders(@Param("keyword") String keyword,
                                         @Param("providerType") String providerType,
                                         @Param("enabled") Integer enabled);

    int insertModel(CampusAiModel model);

    int updateModel(CampusAiModel model);

    int logicalDeleteModel(@Param("providerCode") String providerCode,
                           @Param("modelCode") String modelCode,
                           @Param("updateUserId") Long updateUserId);

    CampusAiModel selectModel(@Param("providerCode") String providerCode,
                              @Param("modelCode") String modelCode);

    CampusAiModel selectFirstEnabledModel(@Param("providerCode") String providerCode);

    List<CampusAiModel> listModels(@Param("providerCode") String providerCode,
                                   @Param("keyword") String keyword,
                                   @Param("enabled") Integer enabled);

    int insertFeatureBinding(CampusAiFeatureBinding binding);

    int updateFeatureBinding(CampusAiFeatureBinding binding);

    CampusAiFeatureBinding selectFeatureBinding(@Param("featureCode") String featureCode);

    List<CampusAiFeatureBinding> listFeatureBindings(@Param("keyword") String keyword,
                                                     @Param("featureType") String featureType,
                                                     @Param("enabled") Integer enabled);

    int insertPromptTemplate(CampusAiPromptTemplate promptTemplate);

    int updatePromptTemplate(CampusAiPromptTemplate promptTemplate);

    int logicalDeletePromptTemplate(@Param("templateId") Long templateId,
                                    @Param("updateUserId") Long updateUserId);

    CampusAiPromptTemplate selectPromptTemplate(@Param("templateId") Long templateId);

    CampusAiPromptTemplate selectActivePrompt(@Param("featureCode") String featureCode);

    List<CampusAiPromptTemplate> listPromptTemplates(@Param("featureCode") String featureCode,
                                                    @Param("keyword") String keyword,
                                                    @Param("enabled") Integer enabled);

    int insertCallLog(CampusAiCallLog callLog);

    List<CampusAiCallLog> listCallLogs(@Param("featureCode") String featureCode,
                                       @Param("providerCode") String providerCode,
                                       @Param("callStatus") String callStatus);

    int countActiveProviders();

    int countEnabledFeatures();

    int countLegacyFeatures();

    int countFailedCallsSince(@Param("since") Date since);

    List<Map<String, Object>> countCallsByStatusSince(@Param("since") Date since);
}
