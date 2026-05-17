package com.stonedt.intelligence.service.impl.campus;

import com.stonedt.intelligence.dao.campus.CampusClueDao;
import com.stonedt.intelligence.service.campus.CampusHotRankService;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CampusHotRankServiceImpl implements CampusHotRankService {

    private final CampusClueDao campusClueDao;

    public CampusHotRankServiceImpl(CampusClueDao campusClueDao) {
        this.campusClueDao = campusClueDao;
    }

    @Override
    public Map<String, List<Map<String, Object>>> getHotRank() {
        Map<String, List<Map<String, Object>>> result = new LinkedHashMap<>();

        List<Map<String, Object>> weiboList = new ArrayList<>();
        List<Map<String, Object>> douyinList = new ArrayList<>();
        List<Map<String, Object>> toutiaoList = new ArrayList<>();

        result.put("weibo", weiboList);
        result.put("douyin", douyinList);
        result.put("toutiao", toutiaoList);

        List<Map<String, Object>> keywordRows = campusClueDao.getHotRankKeywords(1);
        if (keywordRows == null || keywordRows.isEmpty()) {
            return result;
        }

        for (Map<String, Object> row : keywordRows) {
            String platform = row.get("platform") != null
                    ? row.get("platform").toString().trim().toLowerCase() : null;
            if (platform == null || platform.isEmpty()) {
                continue;
            }

            List<Map<String, Object>> targetList;
            if (platform.contains("weibo") || platform.contains("微博")) {
                targetList = weiboList;
            } else if (platform.contains("douyin") || platform.contains("抖音")) {
                targetList = douyinList;
            } else if (platform.contains("toutiao") || platform.contains("头条")) {
                targetList = toutiaoList;
            } else {
                continue;
            }

            if (targetList.size() >= 10) {
                continue;
            }

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("rank", targetList.size() + 1);
            item.put("title", row.get("keyword"));
            item.put("hot", row.get("cnt"));
            targetList.add(item);
        }

        return result;
    }
}
