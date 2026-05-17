package com.stonedt.intelligence.dao.campus;

import com.stonedt.intelligence.entity.campus.CampusIngestRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface CampusIngestRecordDao {

    int insert(CampusIngestRecord record);

    CampusIngestRecord selectDuplicate(@Param("sourceId") Long sourceId,
                                        @Param("externalId") String externalId,
                                        @Param("contentHash") String contentHash);

    CampusIngestRecord selectDuplicateByExternalId(@Param("sourceId") Long sourceId,
                                                   @Param("externalId") String externalId);

    CampusIngestRecord selectDuplicateByContentHash(@Param("sourceId") Long sourceId,
                                                    @Param("contentHash") String contentHash);

    CampusIngestRecord selectDuplicateByPlatformTitle(@Param("sourceId") Long sourceId,
                                                      @Param("platform") String platform,
                                                      @Param("title") String title);

    int updateStatus(@Param("recordId") Long recordId,
                     @Param("normalizedStatus") String normalizedStatus,
                     @Param("targetType") String targetType,
                     @Param("targetId") Long targetId,
                     @Param("errorMessage") String errorMessage,
                     @Param("updateUserId") Long updateUserId);

    int updateEnhancement(@Param("recordId") Long recordId,
                          @Param("incoming") CampusIngestRecord incoming,
                          @Param("updateUserId") Long updateUserId);

    CampusIngestRecord selectByRecordId(@Param("recordId") Long recordId);

    CampusIngestRecord selectByTarget(@Param("targetType") String targetType,
                                      @Param("targetId") Long targetId);

    List<CampusIngestRecord> list(@Param("keyword") String keyword,
                                  @Param("sourceId") Long sourceId,
                                  @Param("taskId") Long taskId,
                                  @Param("normalizedStatus") String normalizedStatus,
                                  @Param("targetType") String targetType,
                                  @Param("startTime") Date startTime,
                                  @Param("endTime") Date endTime);

    List<CampusIngestRecord> listForDetection(@Param("startTime") Date startTime,
                                              @Param("endTime") Date endTime);

    List<CampusIngestRecord> listForDetectionByTaskIds(@Param("startTime") Date startTime,
                                                       @Param("endTime") Date endTime,
                                                       @Param("taskIds") List<Long> taskIds);

    List<CampusIngestRecord> listForDetectionByRunId(@Param("runId") Long runId);

    int logicalDeleteByTaskRetention(@Param("limit") Integer limit,
                                     @Param("updateUserId") Long updateUserId);

    int logicalDeleteBefore(@Param("expireBefore") Date expireBefore,
                            @Param("limit") Integer limit,
                            @Param("updateUserId") Long updateUserId);

    int updateLanguage(@Param("recordId") Long recordId,
                       @Param("language") String language);
}
