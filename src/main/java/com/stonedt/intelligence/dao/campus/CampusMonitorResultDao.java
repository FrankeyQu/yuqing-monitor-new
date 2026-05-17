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

    int updateClue(@Param("monitorResultId") Long monitorResultId,
                   @Param("resultStatus") String resultStatus,
                   @Param("clueId") Long clueId,
                   @Param("updateUserId") Long updateUserId);

    int updateSnapshot(@Param("monitorResultId") Long monitorResultId,
                       @Param("record") CampusIngestRecord record,
                       @Param("updateUserId") Long updateUserId);

    CampusMonitorResult selectByResultId(@Param("monitorResultId") Long monitorResultId);

    CampusMonitorResult selectByTaskAndRecord(@Param("monitorTaskId") Long monitorTaskId,
                                              @Param("ingestRecordId") Long ingestRecordId);

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

    int countToday();

    int logicalDeleteBefore(@Param("expireBefore") Date expireBefore,
                            @Param("limit") Integer limit,
                            @Param("updateUserId") Long updateUserId);
}
