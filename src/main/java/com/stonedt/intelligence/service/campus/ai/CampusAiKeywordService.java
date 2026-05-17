package com.stonedt.intelligence.service.campus.ai;

import java.util.List;
import java.util.Map;

public interface CampusAiKeywordService {

    List<Map<String, Object>> extractWordCloud(List<String> texts,
                                               List<Map<String, Object>> fallback,
                                               int limit);
}
