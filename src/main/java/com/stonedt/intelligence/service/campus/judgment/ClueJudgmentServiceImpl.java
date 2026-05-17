package com.stonedt.intelligence.service.campus.judgment;

import com.stonedt.intelligence.dao.campus.CampusClueDao;
import com.stonedt.intelligence.entity.campus.CampusClue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * Unified judgment engine implementation.
 * <p>
 * Routes judgment by clue language:
 * <ul>
 *   <li>Chinese (zh / null) -- {@link RuleJudgmentEngine}</li>
 *   <li>Minority languages (mongolian / uyghur) -- {@link AiJudgmentEngine}
 *       with fallback to rule engine if AI is unavailable</li>
 *   <li>Unknown languages -- rule engine</li>
 * </ul>
 * Judgment is failure-tolerant: errors are logged but never rethrown,
 * so a judgment failure does not block clue creation.
 * </p>
 */
@Service
public class ClueJudgmentServiceImpl implements ClueJudgmentService {

    private static final Logger log = LoggerFactory.getLogger(ClueJudgmentServiceImpl.class);

    @Autowired
    private RuleJudgmentEngine ruleEngine;

    @Autowired(required = false)
    private AiJudgmentEngine aiEngine;

    @Autowired
    private CampusClueDao campusClueDao;

    @Override
    public void autoJudge(CampusClue clue) {
        if (clue == null) {
            log.warn("autoJudge called with null clue, skipping");
            return;
        }

        try {
            // Route by language
            String language = clue.getLanguage();
            if ("zh".equals(language) || language == null) {
                // Chinese -> rule engine
                ruleEngine.judge(clue);
            } else if ("mongolian".equals(language) || "uyghur".equals(language)) {
                // Minority language -> AI engine (if available, else rule fallback)
                if (aiEngine != null) {
                    aiEngine.judge(clue);
                } else {
                    log.warn("AI engine not available, falling back to rule engine for language={}",
                            clue.getLanguage());
                    ruleEngine.judge(clue);
                }
            } else {
                // Unknown language -> rule engine
                ruleEngine.judge(clue);
            }

            // Update clue: set judgeTime, judgeUserId = 0 (system), mark as judged
            clue.setJudgeTime(new Date());
            clue.setJudgeUserId(0L);
            clue.setClueStatus("judged");
            campusClueDao.update(clue);

        } catch (Exception e) {
            log.warn("Auto-judgment failed for clue {}: {}", clue.getClueId(), e.getMessage());
            // Don't rethrow -- judgment failure should not block clue creation
        }
    }
}
