package com.stonedt.intelligence.controller.campus;

import com.stonedt.intelligence.service.campus.CampusCompareService;
import com.stonedt.intelligence.vo.ResultVO;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/campus/compare")
public class CampusCompareController {

    private final CampusCompareService campusCompareService;

    public CampusCompareController(CampusCompareService campusCompareService) {
        this.campusCompareService = campusCompareService;
    }

    @GetMapping("/data")
    public ResultVO<Map<String, Object>> compareData(
            @RequestParam String selfSubject,
            @RequestParam String competitorSubject) {
        Map<String, Object> data = campusCompareService.compare(selfSubject, competitorSubject);
        return ResultVO.success(data);
    }
}
