package com.stonedt.intelligence.service.minority.util;

import java.util.HashMap;
import java.util.Map;

/**
 * 少数民族语言检测工具类
 * 基于 Unicode 码点范围快速检测语言
 */
public class MinorityLanguageUtil {

    private static final String MONGOLIAN = "mongolian";
    private static final String UYGHUR = "uyghur";
    private static final String UNKNOWN = "unknown";

    // 蒙语常用西里尔字母
    private static final String CYRILLIC_MONGOL_CHARS = "ӨөҮү";

    /**
     * 快速检测文本语言
     */
    public static String detect(String text) {
        if (text == null || text.isEmpty()) return UNKNOWN;

        int mongolianScriptCount = 0;
        int arabicCount = 0;
        int cyrillicMongolCount = 0;
        int total = 0;

        for (int codePoint : text.codePoints().toArray()) {
            total++;
            // 传统蒙古文 U+1800–U+18AF
            if (codePoint >= 0x1800 && codePoint <= 0x18AF) {
                mongolianScriptCount++;
            }
            // 阿拉伯文区块（维语使用）
            if ((codePoint >= 0x0600 && codePoint <= 0x06FF)
                    || (codePoint >= 0xFB50 && codePoint <= 0xFDFF)
                    || (codePoint >= 0xFE70 && codePoint <= 0xFEFF)) {
                arabicCount++;
            }
            // 蒙语西里尔字母 ӨөҮү
            String ch = new String(Character.toChars(codePoint));
            if (CYRILLIC_MONGOL_CHARS.contains(ch)) {
                cyrillicMongolCount++;
            }
        }

        if (arabicCount > total * 0.3) return UYGHUR;
        if (mongolianScriptCount > total * 0.3) return MONGOLIAN;
        // 西里尔蒙文：西里尔字母 + ӨөҮү 高频出现
        if (cyrillicMongolCount > 0) return MONGOLIAN;

        return UNKNOWN;
    }

    /**
     * 从搜索结果列表中聚合语言分布
     * @param texts 文本列表
     * @return {language: count} 映射
     */
    public static Map<String, Integer> detectDistribution(java.util.List<String> texts) {
        Map<String, Integer> dist = new HashMap<>();
        for (String text : texts) {
            String lang = detect(text);
            dist.put(lang, dist.getOrDefault(lang, 0) + 1);
        }
        return dist;
    }

    /**
     * 对空格分隔的文本提取高频词
     * @param texts 文本列表
     * @param topN  返回前 N 个
     * @return 高频词列表
     */
    public static java.util.List<String> extractTopWords(java.util.List<String> texts, int topN) {
        Map<String, Integer> freq = new HashMap<>();
        for (String text : texts) {
            if (text == null) continue;
            String[] words = text.split("\\s+");
            for (String word : words) {
                word = word.replaceAll("[\\p{P}\\p{S}]", "").trim();
                if (word.length() > 0) {
                    freq.put(word, freq.getOrDefault(word, 0) + 1);
                }
            }
        }
        // 按频率排序取 topN
        return freq.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(topN)
                .map(Map.Entry::getKey)
                .collect(java.util.ArrayList::new, java.util.ArrayList::add, java.util.ArrayList::addAll);
    }
}
