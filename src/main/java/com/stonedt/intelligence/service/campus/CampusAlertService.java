package com.stonedt.intelligence.service.campus;

import com.github.pagehelper.PageInfo;
import com.stonedt.intelligence.entity.campus.CampusAlert;
import com.stonedt.intelligence.entity.campus.CampusAlertRule;
import com.stonedt.intelligence.entity.campus.CampusSensitiveWord;

import java.util.List;

public interface CampusAlertService {

    CampusSensitiveWord saveSensitiveWord(CampusSensitiveWord word, Long operatorUserId);

    void deleteSensitiveWord(Long wordId, Long operatorUserId);

    PageInfo<CampusSensitiveWord> listSensitiveWords(Integer pageNum,
                                                     Integer pageSize,
                                                     String keyword,
                                                     String wordCategory,
                                                     String riskLevel,
                                                     Integer status);

    CampusAlertRule saveRule(CampusAlertRule rule, Long operatorUserId);

    void deleteRule(Long ruleId, Long operatorUserId);

    PageInfo<CampusAlertRule> listRules(Integer pageNum,
                                        Integer pageSize,
                                        String keyword,
                                        String ruleType,
                                        Integer enabled);

    CampusAlert createAlert(CampusAlert alert, Long operatorUserId);

    CampusAlert handleAlert(Long alertId,
                            String alertStatus,
                            String handleOpinion,
                            Long operatorUserId);

    PageInfo<CampusAlert> listAlerts(Integer pageNum,
                                     Integer pageSize,
                                     String keyword,
                                     String alertSource,
                                     String riskLevel,
                                     String alertStatus);

    List<CampusAlert> evaluateClue(Long clueId, Long operatorUserId);

    List<CampusAlert> evaluateAccountContent(Long contentId, Long operatorUserId);

    List<CampusAlert> evaluateText(String alertSource,
                                   Long sourceObjectId,
                                   String title,
                                   String content,
                                   Long operatorUserId);
}
