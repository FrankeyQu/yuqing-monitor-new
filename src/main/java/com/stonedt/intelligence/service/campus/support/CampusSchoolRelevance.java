package com.stonedt.intelligence.service.campus.support;

public class CampusSchoolRelevance {

    private final Integer score;
    private final String reason;
    private final String matchedSchoolTerms;
    private final String excludedReason;

    public CampusSchoolRelevance(Integer score,
                                 String reason,
                                 String matchedSchoolTerms,
                                 String excludedReason) {
        this.score = score;
        this.reason = reason;
        this.matchedSchoolTerms = matchedSchoolTerms;
        this.excludedReason = excludedReason;
    }

    public Integer getScore() {
        return score;
    }

    public String getReason() {
        return reason;
    }

    public String getMatchedSchoolTerms() {
        return matchedSchoolTerms;
    }

    public String getExcludedReason() {
        return excludedReason;
    }
}
