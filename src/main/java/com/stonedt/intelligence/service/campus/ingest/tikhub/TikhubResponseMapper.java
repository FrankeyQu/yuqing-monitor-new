package com.stonedt.intelligence.service.campus.ingest.tikhub;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.stonedt.intelligence.service.campus.ingest.CampusIngestItem;
import com.stonedt.intelligence.service.campus.ingest.normalize.CampusIngestTextSanitizer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class TikhubResponseMapper {

    private static final Pattern WEIBO_POST_ID_PATTERN = Pattern.compile("(?:mid_|mid=|mblogid=|/detail/|/status/)(\\d{10,})");

    public List<CampusIngestItem> map(TikhubEndpointDefinition endpointDefinition,
                                      TikhubFetchConfig fetchConfig,
                                      String responseBody) {
        JSONObject root;
        try {
            root = JSON.parseObject(responseBody);
        } catch (RuntimeException ex) {
            throw new TikhubIngestException("TikHub response JSON parse failed: " + ex.getMessage());
        }

        if ("douyin_search_video_v2".equals(endpointDefinition.getEndpointKey())) {
            return mapDouyinAwemeResponse(fetchConfig, root);
        }
        return mapGenericSearchResponse(endpointDefinition, fetchConfig, root);
    }

    public CampusIngestItem mapXiaohongshuDetail(TikhubEndpointDefinition endpointDefinition,
                                                 TikhubFetchConfig fetchConfig,
                                                 String responseBody,
                                                 CampusIngestItem fallback) {
        return mapDetail(endpointDefinition, fetchConfig, responseBody, fallback);
    }

    public CampusIngestItem mapDetail(TikhubEndpointDefinition endpointDefinition,
                                      TikhubFetchConfig fetchConfig,
                                      String responseBody,
                                      CampusIngestItem fallback) {
        JSONObject root;
        try {
            root = JSON.parseObject(responseBody);
        } catch (RuntimeException ex) {
            throw new TikhubIngestException("TikHub detail response JSON parse failed: " + ex.getMessage());
        }
        List<JSONObject> candidates = new ArrayList<>();
        collectGenericCandidates(root, candidates, 0);
        JSONObject selected = selectDetailCandidate(candidates, fallback);
        CampusIngestItem detail = selected == null ? null : toGenericCampusIngestItem(selected, endpointDefinition, fetchConfig);
        return mergeDetail(fallback, detail);
    }

    private List<CampusIngestItem> mapDouyinAwemeResponse(TikhubFetchConfig fetchConfig, JSONObject root) {
        List<JSONObject> awemeInfos = new ArrayList<>();
        collectAwemeInfos(root, awemeInfos);

        List<CampusIngestItem> items = new ArrayList<>();
        Set<String> externalIds = new HashSet<>();
        for (JSONObject awemeInfo : awemeInfos) {
            CampusIngestItem item = toCampusIngestItem(awemeInfo, fetchConfig);
            if (item == null) {
                continue;
            }
            if (item.getExternalId() != null && item.getExternalId().trim().length() > 0) {
                if (!externalIds.add(item.getExternalId())) {
                    continue;
                }
            }
            items.add(item);
            if (items.size() >= fetchConfig.getLimit()) {
                break;
            }
        }
        return items;
    }

    private List<CampusIngestItem> mapGenericSearchResponse(TikhubEndpointDefinition endpointDefinition,
                                                            TikhubFetchConfig fetchConfig,
                                                            JSONObject root) {
        List<JSONObject> candidates = new ArrayList<>();
        collectGenericCandidates(root, candidates, 0);

        List<CampusIngestItem> items = new ArrayList<>();
        Set<String> dedupKeys = new HashSet<>();
        for (JSONObject candidate : candidates) {
            CampusIngestItem item = toGenericCampusIngestItem(candidate, endpointDefinition, fetchConfig);
            if (item == null) {
                continue;
            }
            String dedupKey = StringUtils.defaultIfBlank(item.getExternalId(),
                    StringUtils.defaultString(item.getOriginalUrl()) + "|" + StringUtils.defaultString(item.getContent()));
            if (StringUtils.isNotBlank(dedupKey) && !dedupKeys.add(dedupKey)) {
                continue;
            }
            items.add(item);
            if (items.size() >= fetchConfig.getLimit()) {
                break;
            }
        }
        return items;
    }

    private void collectGenericCandidates(Object value, List<JSONObject> candidates, int depth) {
        if (value == null || depth > 8 || candidates.size() >= 200) {
            return;
        }
        if (value instanceof JSONArray) {
            JSONArray array = (JSONArray) value;
            for (Object item : array) {
                collectGenericCandidates(item, candidates, depth + 1);
                if (candidates.size() >= 200) {
                    break;
                }
            }
            return;
        }
        if (!(value instanceof JSONObject)) {
            return;
        }
        JSONObject object = (JSONObject) value;
        if (isGenericContentCandidate(object)) {
            candidates.add(object);
        }
        for (Object child : object.values()) {
            collectGenericCandidates(child, candidates, depth + 1);
            if (candidates.size() >= 200) {
                break;
            }
        }
    }

    private boolean isGenericContentCandidate(JSONObject object) {
        if (object == null) {
            return false;
        }
        return firstText(object, "aweme_id", "id", "mid", "mblogid", "note_id", "noteId",
                "bvid", "aid", "dynamic_id", "rid", "article_id", "answer_id", "question_id",
                "photo_id", "photoId", "video_id", "item_id", "object_id", "content_id",
                "share_url", "url", "uri", "link", "jump_url", "article_url", "content_url",
                "display_url", "target_url", "web_url", "sogou_url", "permalink", "scheme") != null
                && firstText(object, "desc", "title", "text", "content", "raw_text", "description",
                "digest", "summary", "excerpt", "abstract", "text_raw", "textRaw", "long_text",
                "longTextContent", "long_text_content", "full_text", "content_text",
                "display_text", "caption", "article_title", "question_title") != null;
    }

    private CampusIngestItem toGenericCampusIngestItem(JSONObject source,
                                                       TikhubEndpointDefinition endpointDefinition,
                                                       TikhubFetchConfig fetchConfig) {
        String externalId = firstText(source, "aweme_id", "id", "mid", "mblogid", "note_id", "noteId",
                "bvid", "aid", "dynamic_id", "rid", "article_id", "answer_id", "question_id",
                "photo_id", "photoId", "video_id", "item_id", "object_id", "content_id");
        String content = firstText(source, "desc", "text", "content", "raw_text", "description",
                "digest", "summary", "excerpt", "abstract", "text_raw", "textRaw", "long_text",
                "longTextContent", "long_text_content", "full_text", "content_text",
                "display_text", "caption", "title", "article_title", "question_title");
        String title = firstText(source, "title", "article_title", "question_title", "desc", "text",
                "content", "raw_text", "description", "digest", "summary", "excerpt", "abstract");
        if (externalId == null && content == null && title == null) {
            return null;
        }
        String endpointKey = endpointDefinition.getEndpointKey();
        String weiboPostId = null;
        if ("weibo_search_all".equals(endpointKey)) {
            weiboPostId = resolveWeiboPostId(source, externalId);
            if (StringUtils.isBlank(weiboPostId) || isWeiboLowValueCard(source, title, content)) {
                return null;
            }
            externalId = weiboPostId;
        }

        CampusIngestItem item = new CampusIngestItem();
        item.setExternalId(externalId);
        item.setPlatform(platformOf(endpointDefinition, fetchConfig));
        item.setContentType(contentTypeOf(endpointDefinition, fetchConfig));
        item.setTitle(shortTitle(title));
        item.setContent(content == null ? title : content);
        String originalUrl = firstText(source, "share_url", "url", "uri", "link", "jump_url",
                "video_url", "short_url", "original_url", "article_url", "content_url",
                "display_url", "target_url", "web_url", "sogou_url", "permalink", "scheme");
        if ("weibo_search_all".equals(endpointKey) && isInvalidWeiboOriginalUrl(originalUrl)) {
            originalUrl = null;
        }
        item.setOriginalUrl(originalUrl);
        ensureOriginalUrl(item, source);
        item.setPublishTime(firstDate(source, "create_time", "publish_time", "pubdate", "created_at", "time", "ctime",
                "created_time", "updated_time", "publishTime", "date", "datetime"));
        item.setAuthorName(genericAuthorName(source));
        item.setKeywords(fetchConfig.getQuery());
        item.setRiskLevel("normal");
        applyGenericInteractionMetrics(item, source);
        item.setRawData(markedRawData(source, endpointDefinition.getEndpointKey(), "partial"));
        return item;
    }

    private String resolveWeiboPostId(JSONObject source, String externalId) {
        String direct = firstText(source, "mid", "mblogid", "id");
        if (isWeiboNumericId(direct)) {
            return direct;
        }
        if (isWeiboNumericId(externalId)) {
            return externalId;
        }
        String url = firstText(source, "share_url", "url", "uri", "link", "jump_url",
                "short_url", "original_url", "display_url", "target_url", "web_url", "permalink", "scheme");
        return extractWeiboPostId(url);
    }

    private String extractWeiboPostId(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        Matcher matcher = WEIBO_POST_ID_PATTERN.matcher(value);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private boolean isWeiboNumericId(String value) {
        String id = StringUtils.trimToNull(value);
        return id != null && id.matches("\\d{10,}");
    }

    private boolean isWeiboLowValueCard(JSONObject source, String title, String content) {
        String text = StringUtils.defaultString(title) + "\n" + StringUtils.defaultString(content);
        if (text.contains("实时资讯，海量讨论")) {
            return true;
        }
        if (text.matches("(?s).*\\d+(?:\\.\\d+)?万?帖子.*")) {
            return true;
        }
        String url = firstText(source, "share_url", "url", "uri", "link", "jump_url",
                "short_url", "original_url", "display_url", "target_url", "web_url", "permalink", "scheme");
        return isWeiboSearchOrTopicUrl(url);
    }

    private boolean isInvalidWeiboOriginalUrl(String originalUrl) {
        String url = StringUtils.defaultString(originalUrl).trim().toLowerCase(Locale.ROOT);
        return StringUtils.isNotBlank(url) && (!url.startsWith("http")
                || isWeiboSearchOrTopicUrl(url));
    }

    private boolean isWeiboSearchOrTopicUrl(String originalUrl) {
        String url = StringUtils.defaultString(originalUrl).trim().toLowerCase(Locale.ROOT);
        return url.startsWith("http://s.weibo.com/")
                || url.startsWith("https://s.weibo.com/")
                || url.startsWith("sinaweibo://tabbar")
                || url.startsWith("sinaweibo://search");
    }

    private String platformOf(TikhubEndpointDefinition endpointDefinition, TikhubFetchConfig fetchConfig) {
        if (StringUtils.isNotBlank(fetchConfig.getPlatform())) {
            return fetchConfig.getPlatform();
        }
        String endpointKey = endpointDefinition.getEndpointKey();
        if (endpointKey.startsWith("weibo")) {
            return "weibo";
        }
        if (endpointKey.startsWith("xiaohongshu")) {
            return "xiaohongshu";
        }
        if (endpointKey.startsWith("bilibili")) {
            return "bilibili";
        }
        if (endpointKey.startsWith("douyin")) {
            return "douyin";
        }
        if (endpointKey.startsWith("zhihu")) {
            return "zhihu";
        }
        if (endpointKey.startsWith("wechat_mp")) {
            return "wechat_official";
        }
        if (endpointKey.startsWith("kuaishou")) {
            return "kuaishou";
        }
        return "tikhub";
    }

    private String contentTypeOf(TikhubEndpointDefinition endpointDefinition, TikhubFetchConfig fetchConfig) {
        String endpointKey = endpointDefinition.getEndpointKey();
        if ("xiaohongshu_video_note_detail".equals(endpointKey)) {
            return "video";
        }
        if (endpointKey.startsWith("xiaohongshu")) {
            return "note";
        }
        if (StringUtils.isNotBlank(fetchConfig.getContentType()) && !"0".equals(fetchConfig.getContentType())) {
            return fetchConfig.getContentType();
        }
        if (endpointKey.startsWith("weibo")) {
            return "post";
        }
        if (endpointKey.startsWith("bilibili")) {
            return StringUtils.defaultIfBlank(fetchConfig.getSearchType(), "video");
        }
        if (endpointKey.startsWith("zhihu")) {
            return StringUtils.defaultIfBlank(normalizeContentType(fetchConfig.getContentType()), "article");
        }
        if (endpointKey.startsWith("wechat_mp")) {
            return "article";
        }
        if (endpointKey.startsWith("kuaishou")) {
            return "video";
        }
        return "article";
    }

    private String normalizeContentType(String contentType) {
        String value = StringUtils.defaultString(contentType).trim();
        if (value.length() == 0 || "0".equals(value) || "不限".equals(value)) {
            return "";
        }
        return value;
    }

    private Date firstDate(JSONObject source, String... keys) {
        for (String key : keys) {
            Date date = toDate(source.get(key));
            if (date != null) {
                return date;
            }
        }
        return null;
    }

    private String genericAuthorName(JSONObject source) {
        String direct = firstText(source, "nickname", "user_name", "username", "screen_name", "name",
                "author_name", "account_name", "source", "source_name");
        if (direct != null) {
            return direct;
        }
        JSONObject author = getObject(source, "author");
        if (author != null) {
            return firstText(author, "nickname", "name", "user_name", "username", "screen_name");
        }
        JSONObject user = getObject(source, "user");
        if (user != null) {
            return firstText(user, "screen_name", "nickname", "name", "user_name", "username");
        }
        JSONObject owner = getObject(source, "owner");
        if (owner != null) {
            return firstText(owner, "name", "nickname", "mid");
        }
        JSONObject account = getObject(source, "account");
        if (account != null) {
            return firstText(account, "name", "nickname", "account_name", "title");
        }
        return null;
    }

    private String firstText(JSONObject source, String... keys) {
        if (source == null || keys == null) {
            return null;
        }
        for (String key : keys) {
            String value = cleanText(stringValue(source.get(key)));
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private void collectAwemeInfos(JSONObject root, List<JSONObject> awemeInfos) {
        if (root == null) {
            return;
        }
        collectFromContainer(root, awemeInfos);
        Object data = root.get("data");
        if (data instanceof JSONObject) {
            collectFromContainer((JSONObject) data, awemeInfos);
        }
    }

    private void collectFromContainer(JSONObject container, List<JSONObject> awemeInfos) {
        addAwemeInfo(getObject(container, "aweme_info"), awemeInfos);
        collectFromBusinessArray(getArray(container, "business_data"), awemeInfos);
        collectFromDataArray(getArray(container, "data"), awemeInfos);
    }

    private void collectFromBusinessArray(JSONArray businessData, List<JSONObject> awemeInfos) {
        if (businessData == null) {
            return;
        }
        for (Object item : businessData) {
            if (!(item instanceof JSONObject)) {
                continue;
            }
            JSONObject itemObject = (JSONObject) item;
            JSONObject dataObject = getObject(itemObject, "data");
            if (dataObject != null) {
                addAwemeInfo(getObject(dataObject, "aweme_info"), awemeInfos);
            }
            addAwemeInfo(getObject(itemObject, "aweme_info"), awemeInfos);
        }
    }

    private void collectFromDataArray(JSONArray data, List<JSONObject> awemeInfos) {
        if (data == null) {
            return;
        }
        for (Object item : data) {
            if (!(item instanceof JSONObject)) {
                continue;
            }
            JSONObject itemObject = (JSONObject) item;
            addAwemeInfo(getObject(itemObject, "aweme_info"), awemeInfos);
            JSONObject nestedData = getObject(itemObject, "data");
            if (nestedData != null) {
                addAwemeInfo(getObject(nestedData, "aweme_info"), awemeInfos);
            }
        }
    }

    private CampusIngestItem toCampusIngestItem(JSONObject awemeInfo, TikhubFetchConfig fetchConfig) {
        if (awemeInfo == null) {
            return null;
        }
        String externalId = trimToNull(stringValue(awemeInfo.get("aweme_id")));
        String desc = cleanText(stringValue(awemeInfo.get("desc")));
        if (externalId == null && desc == null) {
            return null;
        }

        CampusIngestItem item = new CampusIngestItem();
        item.setExternalId(externalId);
        item.setPlatform("douyin");
        item.setContentType("video");
        item.setTitle(shortTitle(desc));
        item.setContent(desc);
        item.setOriginalUrl(trimToNull(stringValue(awemeInfo.get("share_url"))));
        item.setPublishTime(toDate(awemeInfo.get("create_time")));
        item.setAuthorName(authorName(awemeInfo));
        item.setKeywords(fetchConfig.getQuery());
        item.setRiskLevel("normal");
        JSONObject statistics = getObject(awemeInfo, "statistics");
        if (statistics != null) {
            item.setLikeCount(firstLong(statistics, "digg_count", "like_count"));
            item.setCommentCount(firstLong(statistics, "comment_count"));
            item.setShareCount(firstLong(statistics, "share_count"));
            item.setCollectCount(firstLong(statistics, "collect_count"));
            item.setViewCount(firstLong(statistics, "play_count", "view_count"));
        }
        item.setRawData(markedRawData(awemeInfo, "douyin_search_video_v2", "partial"));
        return item;
    }

    private String authorName(JSONObject awemeInfo) {
        JSONObject author = getObject(awemeInfo, "author");
        if (author == null) {
            return null;
        }
        return trimToNull(author.getString("nickname"));
    }

    private void applyGenericInteractionMetrics(CampusIngestItem item, JSONObject source) {
        item.setLikeCount(firstLongDeep(source, "like_count", "likes", "liked_count", "digg_count", "attitudes_count", "voteup_count"));
        item.setCommentCount(firstLongDeep(source, "comment_count", "comments", "comments_count", "reply_count"));
        item.setShareCount(firstLongDeep(source, "share_count", "shared_count", "repost_count", "reposts_count", "forward_count"));
        item.setCollectCount(firstLongDeep(source, "collect_count", "collected_count", "favorite_count", "fav_count", "favorites_count"));
        item.setViewCount(firstLongDeep(source, "view_count", "viewCount", "play_count", "playCount", "read_count", "reads_count"));
    }

    private JSONObject selectDetailCandidate(List<JSONObject> candidates, CampusIngestItem fallback) {
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }
        String externalId = fallback == null ? null : StringUtils.trimToNull(fallback.getExternalId());
        if (externalId != null) {
            for (JSONObject candidate : candidates) {
                String candidateId = firstText(candidate, "id", "note_id", "noteId", "mid", "mblogid",
                        "bvid", "aid", "article_id", "answer_id", "question_id", "photo_id", "photoId",
                        "video_id", "item_id", "object_id", "content_id");
                if (externalId.equals(candidateId)) {
                    return candidate;
                }
            }
        }
        return candidates.get(0);
    }

    private CampusIngestItem mergeDetail(CampusIngestItem fallback, CampusIngestItem detail) {
        if (fallback == null) {
            return detail;
        }
        if (detail == null) {
            ensureOriginalUrl(fallback, null);
            return fallback;
        }
        detail.setExternalId(StringUtils.defaultIfBlank(detail.getExternalId(), fallback.getExternalId()));
        detail.setPlatform(StringUtils.defaultIfBlank(detail.getPlatform(), fallback.getPlatform()));
        detail.setContentType(StringUtils.defaultIfBlank(detail.getContentType(), fallback.getContentType()));
        detail.setTitle(betterText(detail.getTitle(), fallback.getTitle()));
        detail.setContent(betterText(detail.getContent(), fallback.getContent()));
        detail.setOriginalUrl(StringUtils.defaultIfBlank(detail.getOriginalUrl(), fallback.getOriginalUrl()));
        if (StringUtils.isBlank(detail.getOriginalUrl())) {
            ensureOriginalUrl(detail, null);
        }
        if (detail.getPublishTime() == null) {
            detail.setPublishTime(fallback.getPublishTime());
        }
        detail.setAuthorName(StringUtils.defaultIfBlank(detail.getAuthorName(), fallback.getAuthorName()));
        detail.setKeywords(StringUtils.defaultIfBlank(detail.getKeywords(), fallback.getKeywords()));
        detail.setRiskLevel(StringUtils.defaultIfBlank(detail.getRiskLevel(), fallback.getRiskLevel()));
        detail.setSentiment(StringUtils.defaultIfBlank(detail.getSentiment(), fallback.getSentiment()));
        detail.setLikeCount(detail.getLikeCount() == null ? fallback.getLikeCount() : detail.getLikeCount());
        detail.setCommentCount(detail.getCommentCount() == null ? fallback.getCommentCount() : detail.getCommentCount());
        detail.setShareCount(detail.getShareCount() == null ? fallback.getShareCount() : detail.getShareCount());
        detail.setCollectCount(detail.getCollectCount() == null ? fallback.getCollectCount() : detail.getCollectCount());
        detail.setViewCount(detail.getViewCount() == null ? fallback.getViewCount() : detail.getViewCount());
        detail.setRawData(markDetailRawData(detail.getRawData()));
        detail.setContentHash(fallback.getContentHash());
        return detail;
    }

    private String betterText(String preferred, String fallback) {
        String preferredText = StringUtils.trimToNull(preferred);
        String fallbackText = StringUtils.trimToNull(fallback);
        if (preferredText == null) {
            return fallbackText;
        }
        if (fallbackText == null) {
            return preferredText;
        }
        return preferredText.length() >= fallbackText.length() ? preferredText : fallbackText;
    }

    private void ensureOriginalUrl(CampusIngestItem item, JSONObject source) {
        if (item == null || StringUtils.isNotBlank(item.getOriginalUrl())) {
            return;
        }
        String platform = StringUtils.defaultString(item.getPlatform()).toLowerCase();
        if ("xiaohongshu".equals(platform)) {
            item.setOriginalUrl(buildXiaohongshuUrl(item.getExternalId()));
            return;
        }
        if ("weibo".equals(platform)) {
            item.setOriginalUrl(buildWeiboUrl(item.getExternalId(), source));
            return;
        }
        if ("bilibili".equals(platform)) {
            item.setOriginalUrl(buildBilibiliUrl(item.getExternalId(), source));
            return;
        }
        if ("zhihu".equals(platform)) {
            item.setOriginalUrl(buildZhihuUrl(item.getExternalId(), source));
            return;
        }
        if ("kuaishou".equals(platform)) {
            item.setOriginalUrl(buildKuaishouUrl(item.getExternalId(), source));
        }
    }

    private String buildXiaohongshuUrl(String externalId) {
        String noteId = StringUtils.trimToNull(externalId);
        if (noteId == null) {
            return null;
        }
        return "https://www.xiaohongshu.com/explore/" + noteId;
    }

    private String buildWeiboUrl(String externalId, JSONObject source) {
        String id = StringUtils.trimToNull(externalId);
        if (id == null) {
            return null;
        }
        String userId = firstNestedText(source, "user", "id", "idstr");
        if (StringUtils.isNotBlank(userId)) {
            return "https://weibo.com/" + userId + "/" + id;
        }
        return "https://m.weibo.cn/detail/" + id;
    }

    private String buildBilibiliUrl(String externalId, JSONObject source) {
        String bvid = firstText(source, "bvid", "bv_id");
        if (StringUtils.isBlank(bvid)) {
            String id = StringUtils.trimToNull(externalId);
            if (id != null && id.toUpperCase(Locale.ROOT).startsWith("BV")) {
                bvid = id;
            }
        }
        if (StringUtils.isNotBlank(bvid)) {
            return "https://www.bilibili.com/video/" + bvid;
        }
        String aid = firstText(source, "aid", "av_id");
        if (StringUtils.isBlank(aid)) {
            aid = StringUtils.trimToNull(externalId);
        }
        return StringUtils.isBlank(aid) ? null : "https://www.bilibili.com/video/av" + aid;
    }

    private String buildZhihuUrl(String externalId, JSONObject source) {
        String articleId = firstText(source, "article_id");
        if (StringUtils.isNotBlank(articleId)) {
            return "https://zhuanlan.zhihu.com/p/" + articleId;
        }
        String answerId = firstText(source, "answer_id");
        String questionId = firstText(source, "question_id", "qid");
        if (StringUtils.isNotBlank(answerId) && StringUtils.isNotBlank(questionId)) {
            return "https://www.zhihu.com/question/" + questionId + "/answer/" + answerId;
        }
        String id = StringUtils.trimToNull(externalId);
        return id == null ? null : "https://www.zhihu.com/search?type=content&q=" + id;
    }

    private String buildKuaishouUrl(String externalId, JSONObject source) {
        String photoId = firstText(source, "photo_id", "photoId", "id");
        if (StringUtils.isBlank(photoId)) {
            photoId = StringUtils.trimToNull(externalId);
        }
        return StringUtils.isBlank(photoId) ? null : "https://www.kuaishou.com/short-video/" + photoId;
    }

    private String firstNestedText(JSONObject source, String objectKey, String... keys) {
        JSONObject object = getObject(source, objectKey);
        return object == null ? null : firstText(object, keys);
    }

    private String markedRawData(JSONObject source, String endpointKey, String captureStatus) {
        JSONObject marked = new JSONObject();
        if (source != null) {
            marked.putAll(source);
        }
        marked.put("_tikhub_endpoint", endpointKey);
        marked.put("_content_capture_status", captureStatus);
        return TikhubSanitizer.sanitizeJsonToString(marked);
    }

    private String markDetailRawData(String rawData) {
        try {
            JSONObject raw = JSON.parseObject(rawData);
            if (raw == null) {
                return rawData;
            }
            raw.put("_content_capture_status", "full");
            return TikhubSanitizer.sanitizeJsonToString(raw);
        } catch (RuntimeException ex) {
            return rawData;
        }
    }

    private Long firstLongDeep(JSONObject source, String... keys) {
        Long direct = firstLong(source, keys);
        if (direct != null) {
            return direct;
        }
        JSONObject statistics = getObject(source, "statistics");
        if (statistics != null) {
            Long value = firstLong(statistics, keys);
            if (value != null) {
                return value;
            }
        }
        JSONObject stat = getObject(source, "stat");
        if (stat != null) {
            Long value = firstLong(stat, keys);
            if (value != null) {
                return value;
            }
        }
        JSONObject interactInfo = getObject(source, "interact_info");
        if (interactInfo != null) {
            Long value = firstLong(interactInfo, keys);
            if (value != null) {
                return value;
            }
        }
        JSONObject videoTotalCounter = getObject(source, "video_total_counter");
        if (videoTotalCounter != null) {
            Long value = firstLong(videoTotalCounter, keys);
            if (value != null) {
                return value;
            }
            value = firstLong(videoTotalCounter, "play_cnt");
            if (value != null) {
                return value;
            }
        }
        JSONObject stats = getObject(source, "stats");
        if (stats != null) {
            Long value = firstLong(stats, keys);
            if (value != null) {
                return value;
            }
        }
        JSONObject counter = getObject(source, "counter");
        if (counter != null) {
            Long value = firstLong(counter, keys);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private Long firstLong(JSONObject source, String... keys) {
        if (source == null || keys == null) {
            return null;
        }
        for (String key : keys) {
            Long value = longValue(source.get(key));
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private Date toDate(Object value) {
        Long seconds = longValue(value);
        if (seconds != null && seconds > 0) {
            if (seconds > 100000000000L) {
                return new Date(seconds);
            }
            return new Date(seconds * 1000L);
        }
        String text = trimToNull(stringValue(value));
        if (text == null) {
            return null;
        }
        String[] patterns = new String[]{
                "EEE MMM dd HH:mm:ss Z yyyy",
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd'T'HH:mm:ssXXX",
                "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"
        };
        for (String pattern : patterns) {
            try {
                return new SimpleDateFormat(pattern, Locale.ENGLISH).parse(text);
            } catch (ParseException ignored) {
                // Try the next known platform date format.
            }
        }
        return null;
    }

    private Long longValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String shortTitle(String value) {
        if (value == null) {
            return null;
        }
        return value.length() <= 80 ? value : value.substring(0, 80);
    }

    private void addAwemeInfo(JSONObject awemeInfo, List<JSONObject> awemeInfos) {
        if (awemeInfo != null) {
            awemeInfos.add(awemeInfo);
        }
    }

    private JSONObject getObject(JSONObject object, String key) {
        if (object == null) {
            return null;
        }
        Object value = object.get(key);
        return value instanceof JSONObject ? (JSONObject) value : null;
    }

    private JSONArray getArray(JSONObject object, String key) {
        if (object == null) {
            return null;
        }
        Object value = object.get(key);
        return value instanceof JSONArray ? (JSONArray) value : null;
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private String cleanText(String value) {
        return CampusIngestTextSanitizer.cleanPlainText(value);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.length() == 0 ? null : trimmed;
    }
}
