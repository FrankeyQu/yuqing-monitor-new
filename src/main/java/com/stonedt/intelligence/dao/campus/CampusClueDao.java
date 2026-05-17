package com.stonedt.intelligence.dao.campus;

import com.stonedt.intelligence.entity.campus.CampusClue;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Mapper
public interface CampusClueDao {

    int insert(CampusClue clue);

    int update(CampusClue clue);

    int judge(@Param("clueId") Long clueId,
              @Param("riskLevel") String riskLevel,
              @Param("judgeOpinion") String judgeOpinion,
              @Param("judgeUserId") Long judgeUserId,
              @Param("updateUserId") Long updateUserId);

    int archive(@Param("clueId") Long clueId,
                @Param("remark") String remark,
                @Param("updateUserId") Long updateUserId);

    int markConverted(@Param("clueId") Long clueId,
                      @Param("eventId") Long eventId,
                      @Param("updateUserId") Long updateUserId);

    int logicalDelete(@Param("clueId") Long clueId, @Param("updateUserId") Long updateUserId);

    CampusClue selectByClueId(@Param("clueId") Long clueId);

    List<CampusClue> list(@Param("keyword") String keyword,
                          @Param("clueSource") String clueSource,
                          @Param("sourcePlatform") String sourcePlatform,
                          @Param("sourceSubPlatform") String sourceSubPlatform,
                          @Param("riskLevel") String riskLevel,
                          @Param("clueStatus") String clueStatus,
                          @Param("language") String language,
                          @Param("sentiment") String sentiment,
                          @Param("articleStatus") String articleStatus,
                          @Param("startTime") Date startTime,
                          @Param("endTime") Date endTime,
                          @Param("publishTimeStart") Date publishTimeStart,
                          @Param("publishTimeEnd") Date publishTimeEnd,
                          @Param("collectTimeStart") Date collectTimeStart,
                          @Param("collectTimeEnd") Date collectTimeEnd,
                          @Param("matchScope") String matchScope,
                          @Param("similarDedup") Boolean similarDedup,
                          @Param("sortBy") String sortBy);

    List<CampusClue> listForDetection(@Param("startTime") Date startTime,
                                      @Param("endTime") Date endTime);

    List<CampusClue> listForReportScope(@Param("keyword") String keyword,
                                        @Param("keywords") List<String> keywords,
                                        @Param("excludeKeywords") List<String> excludeKeywords,
                                        @Param("eventId") Long eventId,
                                        @Param("platforms") List<String> platforms,
                                        @Param("riskLevels") List<String> riskLevels,
                                        @Param("departmentIds") List<Long> departmentIds,
                                        @Param("monitorTaskIds") List<Long> monitorTaskIds,
                                        @Param("startTime") Date startTime,
                                        @Param("endTime") Date endTime);

    List<Map<String, Object>> getReportDailyTrend(@Param("keyword") String keyword,
                                                  @Param("keywords") List<String> keywords,
                                                  @Param("excludeKeywords") List<String> excludeKeywords,
                                                  @Param("eventId") Long eventId,
                                                  @Param("platforms") List<String> platforms,
                                                  @Param("riskLevels") List<String> riskLevels,
                                                  @Param("departmentIds") List<Long> departmentIds,
                                                  @Param("monitorTaskIds") List<Long> monitorTaskIds,
                                                  @Param("startTime") Date startTime,
                                                  @Param("endTime") Date endTime,
                                                  @Param("days") int days);

    List<CampusClue> listSimilarForEvent(@Param("eventId") Long eventId,
                                         @Param("excludeClueIds") List<Long> excludeClueIds,
                                         @Param("topicCategory") String topicCategory,
                                         @Param("riskLevel") String riskLevel,
                                         @Param("keyword") String keyword,
                                         @Param("limit") Integer limit);

    int countDuplicate(@Param("duplicateKey") String duplicateKey,
                       @Param("excludeClueId") Long excludeClueId);

    List<Map<String, Object>> countBySubject();

    List<Map<String, Object>> countByPlatform();

    List<Map<String, Object>> countBySentiment();

    List<Map<String, Object>> countByMediaType(@Param("keyword") String keyword,
                                               @Param("clueSource") String clueSource,
                                               @Param("sourcePlatform") String sourcePlatform,
                                               @Param("sourceSubPlatform") String sourceSubPlatform,
                                               @Param("riskLevel") String riskLevel,
                                               @Param("clueStatus") String clueStatus,
                                               @Param("language") String language,
                                               @Param("sentiment") String sentiment,
                                               @Param("articleStatus") String articleStatus,
                                               @Param("startTime") Date startTime,
                                               @Param("endTime") Date endTime,
                                               @Param("publishTimeStart") Date publishTimeStart,
                                               @Param("publishTimeEnd") Date publishTimeEnd,
                                               @Param("collectTimeStart") Date collectTimeStart,
                                               @Param("collectTimeEnd") Date collectTimeEnd,
                                               @Param("matchScope") String matchScope,
                                               @Param("similarDedup") Boolean similarDedup);

    List<Map<String, Object>> countBySubPlatform(@Param("keyword") String keyword,
                                                 @Param("clueSource") String clueSource,
                                                 @Param("sourcePlatform") String sourcePlatform,
                                                 @Param("sourceSubPlatform") String sourceSubPlatform,
                                                 @Param("riskLevel") String riskLevel,
                                                 @Param("clueStatus") String clueStatus,
                                                 @Param("language") String language,
                                                 @Param("sentiment") String sentiment,
                                                 @Param("articleStatus") String articleStatus,
                                                 @Param("startTime") Date startTime,
                                                 @Param("endTime") Date endTime,
                                                 @Param("publishTimeStart") Date publishTimeStart,
                                                 @Param("publishTimeEnd") Date publishTimeEnd,
                                                 @Param("collectTimeStart") Date collectTimeStart,
                                                 @Param("collectTimeEnd") Date collectTimeEnd,
                                                 @Param("matchScope") String matchScope,
                                                 @Param("similarDedup") Boolean similarDedup);

    List<String> getAllKeywords();

    List<String> listRecentWordCloudTexts(@Param("limit") Integer limit);

    List<Map<String, Object>> getDailyTrend(@Param("days") int days);

    List<String> suggestKeywords(@Param("keyword") String keyword);

    /**
     * 获取最近 N 天内各平台的热门关键词排名（按出现频次降序）
     * 返回每行包含 platform、keyword、cnt
     */
    List<Map<String, Object>> getHotRankKeywords(@Param("days") int days);

    List<Map<String, Object>> getDailyTrendByKeyword(@Param("days") int days, @Param("keyword") String keyword);

    List<Map<String, Object>> countBySentimentByKeyword(@Param("keyword") String keyword);

    List<Map<String, Object>> countByPlatformByKeyword(@Param("keyword") String keyword);

    int countByKeyword(@Param("keyword") String keyword);

    int updateAfterJudgment(CampusClue clue);
}
