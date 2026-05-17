package com.stonedt.intelligence.service.minority.search.impl;

import com.stonedt.intelligence.service.minority.model.MinoritySearchResult;
import com.stonedt.intelligence.service.minority.search.SearchEngineAdapter;
import com.stonedt.intelligence.util.HotWordsUtil;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLContext;
import java.net.URI;
import java.net.URLEncoder;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

/**
 * 必应搜索引擎适配器
 * 基于 Bing 网页搜索 HTML 解析实现
 */
@Component
public class BingSearchAdapter implements SearchEngineAdapter {

    private static final Logger logger = LoggerFactory.getLogger(BingSearchAdapter.class);

    private static final int CONNECT_TIMEOUT = 10000;
    private static final int SOCKET_TIMEOUT = 10000;

    @Override
    public String getEngineName() {
        return "bing";
    }

    @Override
    public List<MinoritySearchResult> search(String keyword, int page) {
        List<MinoritySearchResult> results = new ArrayList<>();
        String html;
        try {
            String encodedKeyword = URLEncoder.encode(keyword, "UTF-8");
            int first = (page - 1) * 10 + 1;
            String url = "https://www.bing.com/search?q=" + encodedKeyword + "&first=" + first;
            html = doGet(url);
        } catch (Exception e) {
            logger.error("Bing search HTTP request failed, keyword={}, page={}", keyword, page, e);
            return results;
        }

        if (html == null || html.isEmpty()) {
            return results;
        }

        try {
            Document doc = Jsoup.parse(html);
            Elements resultItems = doc.select("#b_results > li.b_algo");

            for (Element item : resultItems) {
                try {
                    Element titleEl = item.select("h2 > a").first();
                    if (titleEl == null) {
                        continue;
                    }

                    String title = titleEl.text().trim();
                    String href = titleEl.attr("href");

                    Element snippetEl = item.select(".b_caption p").first();
                    String snippet = snippetEl != null ? snippetEl.text().trim() : "";

                    MinoritySearchResult result = new MinoritySearchResult();
                    result.setTitle(title);
                    result.setSnippet(snippet);
                    result.setUrl(href);
                    result.setSource(extractSource(href));
                    result.setEngine("bing");
                    results.add(result);
                } catch (Exception e) {
                    logger.warn("Failed to parse a single Bing search result item", e);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to parse Bing search results HTML, keyword={}", keyword, e);
        }

        return results;
    }

    /**
     * 发送 HTTP GET 请求，处理 SSL 证书信任
     */
    private String doGet(String url) {
        SSLContext sslContext;
        try {
            sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
                @Override
                public boolean isTrusted(X509Certificate[] chain, String authType) {
                    return true;
                }
            }).build();
        } catch (Exception e) {
            logger.error("Failed to create SSL context for Bing request", e);
            return null;
        }

        SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(
                sslContext, NoopHostnameVerifier.INSTANCE);

        try (CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(sslSocketFactory)
                .build()) {

            HttpGet httpGet = new HttpGet(url);
            RequestConfig config = RequestConfig.custom()
                    .setConnectTimeout(CONNECT_TIMEOUT)
                    .setSocketTimeout(SOCKET_TIMEOUT)
                    .build();
            httpGet.setConfig(config);
            httpGet.setHeader("User-Agent", HotWordsUtil.getRandomAgent());

            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                if (response.getEntity() != null) {
                    return EntityUtils.toString(response.getEntity(), "UTF-8");
                }
            }
        } catch (Exception e) {
            logger.error("HTTP GET failed for url: {}", url, e);
        }

        return null;
    }

    /**
     * 从 URL 中提取域名作为来源
     */
    private String extractSource(String urlStr) {
        try {
            URI uri = new URI(urlStr);
            String host = uri.getHost();
            return host != null ? host : "";
        } catch (Exception e) {
            return "";
        }
    }
}
