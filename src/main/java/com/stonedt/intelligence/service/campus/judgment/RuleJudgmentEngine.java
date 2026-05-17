package com.stonedt.intelligence.service.campus.judgment;

import com.stonedt.intelligence.entity.campus.CampusClue;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Chinese content rule-based judgment engine.
 * <p>
 * Scores a clue's risk level based on keyword hits, negative word hits,
 * and sentiment in the title + content text. No external service calls --
 * pure in-memory text matching.
 * </p>
 */
@Component
public class RuleJudgmentEngine {

    private static final Logger log = LoggerFactory.getLogger(RuleJudgmentEngine.class);

    /**
     * Default negative word dictionary when no external config is provided.
     */
    private static final String DEFAULT_NEGATIVE_WORDS =
            "投诉,举报,曝光,维权,抗议,示威,丑闻,腐败,贪污,欺诈,造假,劣质,不合格," +
            "安全隐患,事故,伤亡,死亡,暴力,欺凌,霸凌,歧视,辱骂,攻击,威胁,泄露,隐私," +
            "违规,违法,犯罪,判刑,罚款,停业,倒闭,破产,裁员,拖欠,克扣,虚假,欺骗," +
            "陷阱,传销,网贷,高利贷,裸贷,性侵,骚扰,猥亵,强奸,杀人,自杀,跳楼," +
            "坠楼,火灾,爆炸,中毒,污染,辐射,致癌,有毒,有害,过期,变质,召回";

    @Value("${campus.judgment.negative-words:}")
    private String configNegativeWords;

    /**
     * Judge a single CampusClue by keyword + negative-word text matching.
     * <p>
     * Sets {@code riskLevel}, {@code sentiment}, and {@code judgeOpinion}
     * directly on the clue entity. Never throws -- all exceptions are
     * caught by the caller ({@link ClueJudgmentServiceImpl}).
     * </p>
     *
     * @param clue the clue to judge (must not be null; already persisted)
     */
    public void judge(CampusClue clue) {
        // 1. Load keywords from clue
        Set<String> keywords = splitTokens(clue.getKeywords());

        // 2. Load negative words (config overrides defaults)
        Set<String> negativeWords = loadNegativeWords();

        // 3. Build full text for matching
        String text = joinText(clue.getClueTitle(), clue.getClueContent());

        // 4. Match keywords against text
        Set<String> matchedKeywords = matchTokens(text, keywords);

        // 5. Match negative words against text
        Set<String> matchedNegWords = matchTokens(text, negativeWords);

        if (matchedKeywords.isEmpty() && matchedNegWords.isEmpty()) {
            log.debug("No keywords or negative words matched for clue {}", clue.getClueId());
        }

        // 6. Calculate risk score (0-100)
        //    keyword match: +10 per match, max 50
        //    negative word match: +20 per match, max 50
        //    sentiment: null -> +0, negative -> +10
        int keywordScore = Math.min(50, matchedKeywords.size() * 10);
        int negWordScore = Math.min(50, matchedNegWords.size() * 20);
        int sentimentScore = 0;
        String sentiment = clue.getSentiment();
        if (sentiment != null && ("negative".equalsIgnoreCase(sentiment) || "负面".equals(sentiment))) {
            sentimentScore = 10;
        }
        int riskScore = Math.min(100, keywordScore + negWordScore + sentimentScore);

        // 7. Map score to risk level
        String riskLevel;
        if (riskScore >= 90) {
            riskLevel = "urgent";
        } else if (riskScore >= 70) {
            riskLevel = "major";
        } else if (riskScore >= 45) {
            riskLevel = "concern";
        } else {
            riskLevel = "normal";
        }

        // 8. Apply judgment to clue entity
        clue.setRiskLevel(riskLevel);

        // If negative words matched, override sentiment to "疑似负面";
        // otherwise preserve the original sentiment.
        if (!matchedNegWords.isEmpty()) {
            clue.setSentiment("疑似负面");
        }

        clue.setJudgeOpinion(buildOpinion(matchedKeywords, matchedNegWords, riskScore));

        log.debug("Rule judgment for clue {}: riskLevel={}, score={}, keywords={}, negWords={}",
                clue.getClueId(), riskLevel, riskScore,
                matchedKeywords.size(), matchedNegWords.size());
    }

    // ==================== Helper methods ====================

    /**
     * Build a short judgment opinion string summarizing the match results.
     */
    private String buildOpinion(Set<String> matchedKeywords,
                                Set<String> matchedNegWords,
                                int riskScore) {
        StringBuilder sb = new StringBuilder();
        sb.append("命中关键词: ");
        if (matchedKeywords.isEmpty()) {
            sb.append("无");
        } else {
            sb.append(String.join(", ", matchedKeywords));
        }
        sb.append("; 负面词: ");
        if (matchedNegWords.isEmpty()) {
            sb.append("无");
        } else {
            sb.append(String.join(", ", matchedNegWords));
        }
        sb.append("; 风险评分: ").append(riskScore).append("/100");
        return sb.toString();
    }

    /**
     * Load negative words: config value wins if non-blank, else use defaults.
     */
    private Set<String> loadNegativeWords() {
        if (StringUtils.isNotBlank(configNegativeWords)) {
            return splitTokens(configNegativeWords);
        }
        return splitTokens(DEFAULT_NEGATIVE_WORDS);
    }

    /**
     * Split a comma/semicolon/newline separated string into a set of trimmed tokens.
     * Duplicates are removed, order is preserved (LinkedHashSet).
     */
    private Set<String> splitTokens(String raw) {
        Set<String> tokens = new LinkedHashSet<>();
        if (StringUtils.isBlank(raw)) {
            return tokens;
        }
        String[] parts = raw.split("[,;，；\\n\\r]+");
        for (String part : parts) {
            if (StringUtils.isNotBlank(part)) {
                tokens.add(part.trim());
            }
        }
        return tokens;
    }

    /**
     * Case-insensitive substring matching. Returns the subset of tokens
     * that appear anywhere in the text.
     */
    private Set<String> matchTokens(String text, Set<String> tokens) {
        Set<String> matched = new LinkedHashSet<>();
        String safeText = StringUtils.defaultString(text);
        String lowerText = safeText.toLowerCase();
        for (String token : tokens) {
            if (StringUtils.isBlank(token)) {
                continue;
            }
            if (lowerText.contains(token.toLowerCase())) {
                matched.add(token);
            }
        }
        return matched;
    }

    /**
     * Join title and content into a single string for matching.
     */
    private String joinText(String title, String content) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotBlank(title)) {
            sb.append(title);
        }
        if (StringUtils.isNotBlank(content)) {
            if (sb.length() > 0) {
                sb.append('\n');
            }
            sb.append(content);
        }
        return sb.toString();
    }
}
