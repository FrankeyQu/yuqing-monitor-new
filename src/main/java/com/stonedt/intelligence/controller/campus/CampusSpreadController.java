package com.stonedt.intelligence.controller.campus;

import com.stonedt.intelligence.service.campus.CampusSpreadService;
import com.stonedt.intelligence.vo.ResultVO;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/campus/spread")
public class CampusSpreadController {

    private final CampusSpreadService campusSpreadService;

    public CampusSpreadController(CampusSpreadService campusSpreadService) {
        this.campusSpreadService = campusSpreadService;
    }

    @GetMapping("/data")
    public ResultVO<Map<String, Object>> spreadData(@RequestParam Long eventId) {
        Map<String, Object> data = campusSpreadService.getSpreadData(eventId);
        return ResultVO.success(data);
    }
}
