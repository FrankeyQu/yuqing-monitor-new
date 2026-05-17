package com.stonedt.intelligence.dao.campus;

import com.stonedt.intelligence.entity.campus.CampusIngestRecord;
import com.stonedt.intelligence.entity.campus.CampusMonitorResult;
import com.stonedt.intelligence.entity.campus.CampusMonitorInformation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Mapper
public interface CampusMonitorResultDao {

    int insert(CampusMonitorResult result);

    int updateStatus(@Param("monitorResultId") Long monitorResultId,
                     @Param("resultStatus") String resultStatus,
                     @Param("alertId") Long alertId,
                     @Param("updateUserId") Long updateUserId);

    int updateStatusAndRisk(@Param("monitorResultId") Long monitorResultId,
                            @Param("resultStatus") String resultStatus,
                            @Param("alertId") Long alertId,
                            @Param("riskLevel") String riskLevel,
                            @Param("riskScore") Integer riskScore,
                            @Param("updateUserId") Long updateUserId);

    int updateClue(@Param("monitorResultId") Long monitorResultId,
                   @Param("resultStatus") String resultStatus,
                   @Param("clueId") Long clueId,
                   @Param("updateUserId") Long updateUserId);

    int updateSentiment(@Param("monitorResultId") Long monitorResultId,
                        @Param("sentiment") String sentiment,
                        @Param("updateUserId") Long updateUserId);

    int updateAiAnalysis(@Param("monitorResultId") Long monitorResultId,
                         @Param("sentiment") String sentiment,
                         @Param("aiSummary") String aiSummary,
                         @Param("aiHitRecommendation") String aiHitRecommendation,
                         @Param("aiHitReason") String aiHitReason,
                         @Param("aiConfidence") Integer aiConfidence,
                         @Param("aiAnalysisTime") Date aiAnalysisTime,
                         @Param("aiProviderCode") String aiProviderCode,
                         @Param("aiModelCode") String aiModelCode,
                         @Param("riskLevel") String riskLevel,
                         @Param("riskScore") Integer riskScore,
                         @Param("schoolRelevanceScore") Integer schoolRelevanceScore,
                         @Param("schoolRelevanceReason") String schoolRelevanceReason,
                         @Param("matchedSchoolTerms") String matchedSchoolTerms,
                         @Param("excludedReason") String excludedReason,
                         @Param("topicCategory") String topicCategory,
                         @Param("topicSubCategory") String topicSubCategory,
                         @Param("topicReason") String topicReason,
                         @Param("updateUserId") Long updateUserId);

    int updateSnapshot(@Param("monitorResultId") Long monitorResultId,
                       @Param("record") CampusIngestRecord record,
                       @Param("updateUserId") Long updateUserId);

    CampusMonitorResult selectByResultId(@Param("monitorResultId") Long monitorResultId);

    CampusMonitorResult selectByTaskAndRecord(@Param("monitorTaskId") Long monitorTaskId,
                                              @Param("ingestRecordId") Long ingestRecordId);

    List<CampusMonitorResult> listByResultIds(@Param("monitorResultIds") List<Long> monitorResultIds);

    List<CampusMonitorResult> listRecentForAi(@Param("monitorTaskId") Long monitorTaskId,
                                              @Param("limit") Integer limit);

    List<CampusMonitorResult> list(@Param("monitorTaskId") Long monitorTaskId,
                                   @Param("keyword") String keyword,
                                   @Param("riskLevel") String riskLevel,
                                   @Param("resultStatus") String resultStatus,
                                   @Param("platform") String platform,
                                   @Param("language") String language,
                                   @Param("converted") Boolean converted);

    List<CampusMonitorInformation> listInformation(@Param("keyword") String keyword,
                                                   @Param("monitorTaskId") Long monitorTaskId,
                                                   @Param("sourcePlatform") String sourcePlatform,
                                                   @Param("sourceSubPlatform") String sourceSubPlatform,
                                                   @Param("riskLevel") String riskLevel,
                                                   @Param("clueStatus") String clueStatus,
                                                   @Param("language") String language,
                                                   @Param("sentiment") String sentiment,
                                                   @Param("resultStatus") String resultStatus,
                                                   @Param("publishTimeStart") Date publishTimeStart,
                                                   @Param("publishTimeEnd") Date publishTimeEnd,
                                                   @Param("collectTimeStart") Date collectTimeStart,
                                                   @Param("collectTimeEnd") Date collectTimeEnd,
                                                   @Param("matchScope") String matchScope,
                                                   @Param("similarDedup") Boolean similarDedup,
                                                   @Param("hitScope") String hitScope,
                                                   @Param("sortBy") String sortBy);

    List<Map<String, Object>> countInformationByPlatform(@Param("keyword") String keyword,
                                                         @Param("monitorTaskId") Long monitorTaskId,
                                                         @Param("sourcePlatform") String sourcePlatform,
                                                         @Param("sourceSubPlatform") String sourceSubPlatform,
                                                         @Param("riskLevel") String riskLevel,
                                                         @Param("clueStatus") String clueStatus,
                                                         @Param("language") String language,
                                                         @Param("sentiment") String sentiment,
                                                         @Param("resultStatus") String resultStatus,
                                                         @Param("publishTimeStart") Date publishTimeStart,
                                                         @Param("publishTimeEnd") Date publishTimeEnd,
                                                         @Param("collectTimeStart") Date collectTimeStart,
                                                         @Param("collectTimeEnd") Date collectTimeEnd,
                                                         @Param("matchScope") String matchScope,
                                                         @Param("similarDedup") Boolean similarDedup,
                                                         @Param("hitScope") String hitScope);

    List<Map<String, Object>> countInformationBySubPlatform(@Param("keyword") String keyword,
                                                            @Param("monitorTaskId") Long monitorTaskId,
                                                            @Param("sourcePlatform") String sourcePlatform,
                                                            @Param("sourceSubPlatform") String sourceSubPlatform,
                                                            @Param("riskLevel") String riskLevel,
                                                            @Param("clueStatus") String clueStatus,
                                                            @Param("language") String language,
                                                            @Param("sentiment") String sentiment,
                                                            @Param("resultStatus") String resultStatus,
                                                            @Param("publishTimeStart") Date publishTimeStart,
                                                            @Param("publishTimeEnd") Date publishTimeEnd,
                                                            @Param("collectTimeStart") Date collectTimeStart,
                                                            @Param("collectTimeEnd") Date collectTimeEnd,
                                                            @Param("matchScope") String matchScope,
                                                            @Param("similarDedup") Boolean similarDedup,
                                                            @Param("hitScope") String hitScope);

    int countInformationToday(@Param("hitScope") String hitScope);

    List<Map<String, Object>> monitorInformationTrendByDay(@Param("hitScope") String hitScope,
                                                           @Param("days") Integer days);

    List<Map<String, Object>> monitorInformationSourceDistribution(@Param("hitScope") String hitScope,
                                                                   @Param("limit") Integer limit);

    List<Map<String, Object>> monitorInformationSentimentDistribution(@Param("hitScope") String hitScope);

    List<Map<String, Object>> monitorInformationTopicRiskDistribution(@Param("hitScope") String hitScope,
                                                                     @Param("limit") Integer limit);

    int countToday();

    int logicalDeleteBefore(@Param("expireBefore") Date expireBefore,
                            @Param("limit") Integer limit,
                            @Param("updateUserId") Long updateUserId);
}
