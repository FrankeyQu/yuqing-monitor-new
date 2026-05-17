package com.stonedt.intelligence.service.campus.ingest.publicweb;

import com.stonedt.intelligence.entity.campus.CampusPublicWebWhitelist;
import com.stonedt.intelligence.service.campus.CampusPublicWebWhitelistService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.Locale;
import java.util.regex.Pattern;

@Component
public class PublicWebWhitelistValidator {

    private static final Pattern IPV4_PATTERN = Pattern.compile("\\d{1,3}(\\.\\d{1,3}){3}");
    private static final Pattern FORBIDDEN_URL_TOKEN_PATTERN = Pattern.compile(
            "(?i)(api[_-]?key|access[_-]?token|refresh[_-]?token|authorization|cookie|cookies|password|session|secret|token|sign|signature|xBogus|x_bogus|aBogus|a_bogus)");

    private final CampusPublicWebWhitelistService campusPublicWebWhitelistService;

    public PublicWebWhitelistValidator(CampusPublicWebWhitelistService campusPublicWebWhitelistService) {
        this.campusPublicWebWhitelistService = campusPublicWebWhitelistService;
    }

    public CampusPublicWebWhitelist validate(PublicWebFetchConfig fetchConfig) {
        if (fetchConfig == null) {
            throw new PublicWebIngestException("公开网页配置不能为空");
        }
        CampusPublicWebWhitelist whitelist = campusPublicWebWhitelistService.requireEnabled(fetchConfig.getWhitelistId());
        validateUrlInWhitelist(fetchConfig.getUrl(), whitelist);
        return whitelist;
    }

    public static void validateDomainFormat(String siteDomain) {
        if (StringUtils.isBlank(siteDomain)) {
            throw new IllegalArgumentException("站点域名不能为空");
        }
        String domain = siteDomain.trim().toLowerCase();
        if (domain.contains("://") || domain.contains("/") || domain.contains("?") || domain.contains("#") || domain.contains(":")) {
            throw new IllegalArgumentException("站点域名不能包含协议、端口、路径或查询参数");
        }
        if (!domain.matches("[a-z0-9.-]+") || domain.startsWith(".") || domain.endsWith(".")
                || domain.contains("..") || !domain.contains(".") || isBlockedHost(domain)) {
            throw new IllegalArgumentException("站点域名格式不合法");
        }
    }

    public static void validateHttpUrl(String url) {
        parseHttpUri(url, "URL必须是HTTP或HTTPS地址");
    }

    public static void validateUrlInWhitelist(String url, CampusPublicWebWhitelist whitelist) {
        URI targetUri = parseHttpUri(url, "公开网页URL格式不合法");
        validateTargetInWhitelist(targetUri, whitelist);
    }

    private static void validateTargetInWhitelist(URI targetUri, CampusPublicWebWhitelist whitelist) {
        if (whitelist == null) {
            throw new PublicWebIngestException("公开网页白名单不存在");
        }
        String host = StringUtils.defaultString(targetUri.getHost()).toLowerCase();
        String domain = StringUtils.defaultString(whitelist.getSiteDomain()).toLowerCase();
        if (!host.equals(domain) && !host.endsWith("." + domain)) {
            throw new PublicWebIngestException("公开网页URL未命中白名单域名");
        }
        String path = StringUtils.defaultIfBlank(targetUri.getPath(), "/");
        String allowedPathPrefix = StringUtils.defaultIfBlank(whitelist.getAllowedPathPrefix(), "/");
        if (!allowedPathPrefix.startsWith("/")) {
            allowedPathPrefix = "/" + allowedPathPrefix;
        }
        boolean pathAllowed = "/".equals(allowedPathPrefix)
                || path.equals(allowedPathPrefix)
                || path.startsWith(allowedPathPrefix.endsWith("/") ? allowedPathPrefix : allowedPathPrefix + "/");
        if (!pathAllowed) {
            throw new PublicWebIngestException("公开网页URL未命中白名单路径范围");
        }
    }

    private static URI parseHttpUri(String url, String errorMessage) {
        if (StringUtils.isBlank(url)) {
            throw new IllegalArgumentException(errorMessage);
        }
        try {
            URI uri = new URI(url.trim());
            String scheme = uri.getScheme();
            if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
                throw new IllegalArgumentException(errorMessage);
            }
            if (StringUtils.isBlank(uri.getHost())) {
                throw new IllegalArgumentException(errorMessage);
            }
            if (StringUtils.isNotBlank(uri.getUserInfo())) {
                throw new IllegalArgumentException("URL不能包含用户名或密码");
            }
            validatePublicHost(uri.getHost());
            validateSafePathAndQuery(uri);
            return uri;
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private static void validatePublicHost(String host) {
        String normalizedHost = StringUtils.defaultString(host).toLowerCase(Locale.ROOT);
        if (isBlockedHost(normalizedHost)) {
            throw new IllegalArgumentException("URL不能指向本机、内网或IP地址");
        }
    }

    private static boolean isBlockedHost(String host) {
        String normalizedHost = StringUtils.defaultString(host).toLowerCase(Locale.ROOT);
        return StringUtils.isBlank(normalizedHost)
                || "localhost".equals(normalizedHost)
                || normalizedHost.endsWith(".localhost")
                || normalizedHost.contains(":")
                || IPV4_PATTERN.matcher(normalizedHost).matches();
    }

    private static void validateSafePathAndQuery(URI uri) {
        String rawPath = StringUtils.defaultString(uri.getRawPath());
        String decodedPath = decode(rawPath);
        if (rawPath.contains("..") || decodedPath.contains("..")) {
            throw new IllegalArgumentException("URL路径不能包含上级目录");
        }
        String rawQuery = StringUtils.defaultString(uri.getRawQuery());
        String fragment = StringUtils.defaultString(uri.getRawFragment());
        if (FORBIDDEN_URL_TOKEN_PATTERN.matcher(rawQuery).find()
                || FORBIDDEN_URL_TOKEN_PATTERN.matcher(fragment).find()) {
            throw new IllegalArgumentException("URL不能包含密钥、Cookie、Token或签名参数");
        }
    }

    private static String decode(String value) {
        try {
            return URLDecoder.decode(StringUtils.defaultString(value), "UTF-8");
        } catch (Exception ex) {
            return StringUtils.defaultString(value);
        }
    }
}
