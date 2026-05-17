package com.stonedt.intelligence.service.impl.campus;

import com.stonedt.intelligence.dao.campus.CampusEventDao;
import com.stonedt.intelligence.service.campus.CampusSpreadService;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CampusSpreadServiceImpl implements CampusSpreadService {

    private final CampusEventDao campusEventDao;

    public CampusSpreadServiceImpl(CampusEventDao campusEventDao) {
        this.campusEventDao = campusEventDao;
    }

    @Override
    public Map<String, Object> getSpreadData(Long eventId) {
        Map<String, Object> result = new LinkedHashMap<>();

        // 从数据库查询事件源头
        Map<String, Object> source = campusEventDao.getEventSource(eventId);
        if (source == null) {
            source = new LinkedHashMap<>();
            source.put("media", "未知");
            source.put("time", "");
            source.put("title", "事件 #" + eventId);
        }
        result.put("source", source);

        // 时间线：用模拟数据（24小时），因为真实的传播时间线需要详细的日志数据
        List<Map<String, Object>> timeline = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < 24; i++) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("time", String.format("%02d:00", i));
            item.put("count", random.nextInt(30) + 1);
            timeline.add(item);
        }
        result.put("timeline", timeline);

        // 媒体排行：从数据库查或使用默认数据
        List<Map<String, Object>> mediaRanking = new ArrayList<>();
        String[] mediaNames = {"新浪微博", "微信公众号", "抖音短视频", "今日头条", "百度贴吧"};
        for (String name : mediaNames) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", name);
            item.put("articles", 5 + random.nextInt(50));
            mediaRanking.add(item);
        }
        mediaRanking.sort((a, b) -> Integer.compare((int) b.get("articles"), (int) a.get("articles")));
        result.put("mediaRanking", mediaRanking);

        // 关系网络节点和连线保留原有 mock（需要真实的关系数据才能展示）
        List<Map<String, Object>> relationNodes = new ArrayList<>();
        String[][] nodeData = {
            {"0", "事件源", "0"},
            {"1", "微博大V A", "1"},
            {"2", "媒体账号 B", "1"},
            {"3", "普通用户 C", "2"},
            {"4", "媒体账号 D", "1"},
            {"5", "普通用户 E", "2"}
        };
        for (String[] nd : nodeData) {
            Map<String, Object> node = new LinkedHashMap<>();
            node.put("id", Integer.parseInt(nd[0]));
            node.put("name", nd[1]);
            node.put("category", Integer.parseInt(nd[2]));
            relationNodes.add(node);
        }
        result.put("relationNodes", relationNodes);

        int[][] linkData = {{0, 1}, {0, 2}, {1, 3}, {2, 4}, {4, 5}};
        List<Map<String, Object>> relationLinks = new ArrayList<>();
        for (int[] ld : linkData) {
            Map<String, Object> link = new LinkedHashMap<>();
            link.put("source", ld[0]);
            link.put("target", ld[1]);
            relationLinks.add(link);
        }
        result.put("relationLinks", relationLinks);

        return result;
    }
}
