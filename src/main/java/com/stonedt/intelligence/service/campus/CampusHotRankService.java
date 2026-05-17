package com.stonedt.intelligence.service.campus;

import java.util.List;
import java.util.Map;

public interface CampusHotRankService {
    Map<String, List<Map<String, Object>>> getHotRank();
}
