package com.stonedt.intelligence.controller.campus;

import com.stonedt.intelligence.service.campus.CampusDashboardService;
import com.stonedt.intelligence.vo.ResultVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/campus/dashboard")
public class CampusDashboardController {

    private final CampusDashboardService campusDashboardService;

    public CampusDashboardController(CampusDashboardService campusDashboardService) {
        this.campusDashboardService = campusDashboardService;
    }

    @GetMapping("/overview")
    public ResultVO<Map<String, Object>> overview() {
        return ResultVO.success(campusDashboardService.overview());
    }

    @GetMapping("/statistics")
    public ResultVO<Map<String, Object>> statistics() {
        return ResultVO.success(campusDashboardService.statistics());
    }

    @GetMapping("/word-cloud")
    public ResultVO<List<Map<String, Object>>> wordCloud() {
        return ResultVO.success(campusDashboardService.getWordCloud());
    }

    @GetMapping("/trend")
    public ResultVO<List<Map<String, Object>>> trend(@RequestParam(defaultValue = "7") int days) {
        return ResultVO.success(campusDashboardService.getTrend(days));
    }
}
