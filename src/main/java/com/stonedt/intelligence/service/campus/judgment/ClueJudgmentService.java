package com.stonedt.intelligence.service.campus.judgment;

import com.stonedt.intelligence.entity.campus.CampusClue;

/**
 * Clue judgment service — shared contract between auto-convert (Phase 3) and judgment engine (Phase 4).
 * Routes judgment by language: Chinese → rule engine, minority languages → AI engine.
 */
public interface ClueJudgmentService {

    /**
     * Auto-judge a clue after it is created from an ingest record.
     * Sets riskLevel, sentiment, judgeOpinion, judgeTime on the clue entity.
     * This is non-blocking and failure-tolerant — errors are logged but do not prevent clue creation.
     *
     * @param clue the newly created CampusClue (already persisted, with language set)
     */
    void autoJudge(CampusClue clue);
}
