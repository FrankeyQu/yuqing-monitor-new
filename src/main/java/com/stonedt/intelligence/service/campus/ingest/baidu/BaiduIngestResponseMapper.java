package com.stonedt.intelligence.service.campus.ingest.baidu;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.stonedt.intelligence.service.campus.ingest.CampusIngestItem;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class BaiduIngestResponseMapper {

    private static final Logger logger = LoggerFactory.getLogger(BaiduIngestResponseMapper.class);

    private static final String[] DATE_FORMATS = {
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd",
            "yyyy/MM/dd HH:mm:ss",
            "yyyy/MM/dd",
            "yyyy.MM.dd",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ssXXX",
            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
            "yyyy年MM月dd日",
            "MMM dd, yyyy",
            "EEE, dd MMM yyyy HH:mm:ss z",
            "EEE MMM dd HH:mm:ss z yyyy",
            "dd MMM yyyy HH:mm:ss z",
            "yyyyMMddHHmmss",
    };

    public List<CampusIngestItem> map(BaiduIngestFetchConfig fetchConfig, String responseBody) {
        JSONObject root;
        try {
            root = JSON.parseObject(responseBody);
        } catch (RuntimeException ex) {
            throw new BaiduIngestException("Baidu Qianfan response JSON parse failed: "
                    + BaiduIngestSanitizer.sanitizeError(ex.getMessage()));
        }

        String code = root.getString("code");
        if (StringUtils.isNotBlank(code) && !"0".equals(code) && !"200".equals(code)) {
            String message = root.getString("message");
            throw new BaiduIngestException("Baidu Qianfan API business error: code=" + code
                    + ", message=" + BaiduIngestSanitizer.sanitizeText(message));
        }

        JSONArray references = root.getJSONArray("references");
        if (references == null || references.isEmpty()) {
            return new ArrayList<>();
        }

        List<CampusIngestItem> items = new ArrayList<>();
        Set<String> externalIds = new HashSet<>();
        for (int i = 0; i < references.size(); i++) {
            try {
                JSONObject ref = references.getJSONObject(i);
                CampusIngestItem item = toCampusIngestItem(ref, fetchConfig);
                if (item == null) {
                    continue;
                }
                if (item.getExternalId() != null && !externalIds.add(item.getExternalId())) {
                    continue;
                }
                items.add(item);
                if (items.size() >= fetchConfig.getTopK()) {
                    break;
                }
            } catch (RuntimeException ex) {
                logger.warn("Failed to parse Baidu Qianfan reference item at index {}", i, ex);
            }
        }

        return items;
    }

    private CampusIngestItem toCampusIngestItem(JSONObject ref, BaiduIngestFetchConfig fetchConfig) {
        String title = trimToNull(ref.getString("title"));
        if (title == null) {
            return null;
        }

        String url = trimToNull(ref.getString("url"));
        if (url == null) {
            return null;
        }

        String content = trimToNull(ref.getString("content"));
        if (content == null) {
            content = title;
        }

        String externalId = DigestUtils.md5Hex(url);

        CampusIngestItem item = new CampusIngestItem();
        item.setExternalId(externalId);
        item.setPlatform("news");
        item.setContentType("article");
        item.setTitle(shortValue(title, 200));
        item.setContent(shortValue(content, 2000));
        item.setOriginalUrl(url);
        item.setPublishTime(parseDate(ref.getString("date")));
        item.setKeywords(fetchConfig.getQuery());
        item.setRiskLevel("normal");
        item.setSentiment(null);
        item.setRawData(ref.toJSONString());
        return item;
    }

    private Date parseDate(String dateStr) {
        if (StringUtils.isBlank(dateStr)) {
            return null;
        }
        String trimmed = dateStr.trim();
        for (String format : DATE_FORMATS) {
            try {
                return new SimpleDateFormat(format).parse(trimmed);
            } catch (ParseException ignored) {
            }
        }
        try {
            long timestamp = Long.parseLong(trimmed);
            if (timestamp > 0) {
                if (timestamp > 100000000000L) {
                    return new Date(timestamp);
                }
                return new Date(timestamp * 1000L);
            }
        } catch (NumberFormatException ignored) {
        }
        logger.debug("Unable to parse date from Baidu reference: {}", trimmed);
        return null;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.length() == 0 ? null : trimmed;
    }

    private String shortValue(String value, int maxLen) {
        if (value == null) {
            return null;
        }
        return value.length() <= maxLen ? value : value.substring(0, maxLen);
    }
}
