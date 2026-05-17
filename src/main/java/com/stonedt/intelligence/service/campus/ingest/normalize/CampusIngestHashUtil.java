package com.stonedt.intelligence.service.campus.ingest.normalize;

import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class CampusIngestHashUtil {

    private CampusIngestHashUtil() {
    }

    public static String sha256(String raw) {
        if (StringUtils.isBlank(raw)) {
            return null;
        }
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] digest = messageDigest.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte b : digest) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("内容哈希计算失败", e);
        }
    }

    public static String normalizeHashPart(Object value) {
        if (value == null) {
            return "";
        }
        return StringUtils.normalizeSpace(String.valueOf(value)).toLowerCase();
    }
}
