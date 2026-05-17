package com.stonedt.intelligence.service.campus.ingest.normalize;

import com.alibaba.fastjson.JSON;
import com.stonedt.intelligence.entity.campus.CampusIngestRecord;
import com.stonedt.intelligence.entity.campus.CampusIngestSource;
import com.stonedt.intelligence.entity.campus.CampusIngestTask;
import com.stonedt.intelligence.service.campus.ingest.CampusIngestItem;
import com.stonedt.intelligence.util.SnowflakeUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class CampusIngestRecordNormalizer {

    private static final String STATUS_PENDING = "pending";
    private static final String RISK_NORMAL = "normal";
    private static final String CONTENT_TYPE_ARTICLE = "article";

    private final CampusIngestRawDataSanitizer rawDataSanitizer;

    public CampusIngestRecordNormalizer(CampusIngestRawDataSanitizer rawDataSanitizer) {
        this.rawDataSanitizer = rawDataSanitizer;
    }

    public CampusIngestRecord normalize(Long runId,
                                        CampusIngestTask task,
                                        CampusIngestSource source,
                                        CampusIngestItem item,
                                        Long operatorUserId) {
        if (item == null) {
            throw new IllegalArgumentException("接入记录不能为空");
        }
        CampusIngestRecord record = new CampusIngestRecord();
        record.setRecordId(SnowflakeUtil.getId());
        record.setRunId(runId);
        record.setSourceId(source.getSourceId());
        record.setTaskId(task.getTaskId());
        record.setExternalId(left(trimToNull(item.getExternalId()), 255));
        record.setPlatform(left(lower(StringUtils.defaultIfBlank(item.getPlatform(), source.getPlatform())), 64));
        record.setContentType(left(StringUtils.defaultIfBlank(trimToNull(item.getContentType()), CONTENT_TYPE_ARTICLE), 64));
        record.setContent(CampusIngestTextSanitizer.cleanPlainText(item.getContent()));
        record.setTitle(left(defaultTitle(CampusIngestTextSanitizer.cleanPlainText(item.getTitle()), record.getContent()), 512));
        record.setOriginalUrl(left(trimToNull(item.getOriginalUrl()), 1024));
        record.setPublishTime(item.getPublishTime());
        record.setAuthorName(left(CampusIngestTextSanitizer.cleanPlainText(item.getAuthorName()), 255));
        record.setAccountId(item.getAccountId());
        record.setAccountTaskId(item.getAccountTaskId());
        record.setKeywords(left(CampusIngestTextSanitizer.cleanPlainText(item.getKeywords()), 512));
        record.setRiskLevel(left(StringUtils.defaultIfBlank(trimToNull(item.getRiskLevel()), RISK_NORMAL), 32));
        record.setSentiment(left(trimToNull(item.getSentiment()), 32));
        record.setLikeCount(item.getLikeCount());
        record.setCommentCount(item.getCommentCount());
        record.setShareCount(item.getShareCount());
        record.setCollectCount(item.getCollectCount());
        record.setViewCount(item.getViewCount());
        record.setRawData(rawData(item));
        record.setContentHash(left(StringUtils.defaultIfBlank(trimToNull(item.getContentHash()), buildContentHash(record)), 128));
        record.setNormalizedStatus(STATUS_PENDING);
        record.setDeleted(0);
        record.setCreateUserId(operatorUserId);
        record.setUpdateUserId(operatorUserId);
        return record;
    }

    public boolean isInvalid(CampusIngestRecord record) {
        return record == null
                || (StringUtils.isBlank(record.getExternalId())
                && StringUtils.isBlank(record.getTitle())
                && StringUtils.isBlank(record.getContent())
                && StringUtils.isBlank(record.getOriginalUrl()));
    }

    private String rawData(CampusIngestItem item) {
        if (StringUtils.isNotBlank(item.getRawData())) {
            return rawDataSanitizer.sanitizeToString(item.getRawData());
        }
        return rawDataSanitizer.sanitizeToString(JSON.toJSONString(item));
    }

    private String buildContentHash(CampusIngestRecord record) {
        String raw = CampusIngestHashUtil.normalizeHashPart(record.getPlatform()) + "|"
                + CampusIngestHashUtil.normalizeHashPart(record.getContentType()) + "|"
                + CampusIngestHashUtil.normalizeHashPart(record.getExternalId()) + "|"
                + CampusIngestHashUtil.normalizeHashPart(record.getOriginalUrl()) + "|"
                + CampusIngestHashUtil.normalizeHashPart(record.getTitle()) + "|"
                + CampusIngestHashUtil.normalizeHashPart(record.getContent()) + "|"
                + (record.getPublishTime() == null ? "" : record.getPublishTime().getTime());
        if (StringUtils.isBlank(raw.replace("|", ""))) {
            return null;
        }
        return CampusIngestHashUtil.sha256(raw);
    }

    private String defaultTitle(String title, String content) {
        String resolved = trimToNull(title);
        if (resolved != null) {
            return resolved;
        }
        String text = trimToNull(content);
        if (text == null) {
            return null;
        }
        return text.length() <= 80 ? text : text.substring(0, 80);
    }

    private String lower(String value) {
        String trimmed = trimToNull(value);
        return trimmed == null ? null : trimmed.toLowerCase();
    }

    private String trimToNull(String value) {
        return StringUtils.trimToNull(value);
    }

    private String left(String value, int maxLength) {
        return value == null ? null : StringUtils.left(value, maxLength);
    }
}
