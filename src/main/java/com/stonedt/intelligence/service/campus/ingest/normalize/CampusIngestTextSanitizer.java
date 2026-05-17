package com.stonedt.intelligence.service.campus.ingest.normalize;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;

public final class CampusIngestTextSanitizer {

    private CampusIngestTextSanitizer() {
    }

    public static String cleanPlainText(String value) {
        String text = StringUtils.trimToNull(value);
        if (text == null) {
            return null;
        }
        String decoded = Jsoup.parse(text).text();
        decoded = decoded.replace('\u00A0', ' ');
        decoded = decoded.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "");
        return StringUtils.trimToNull(decoded);
    }
}
