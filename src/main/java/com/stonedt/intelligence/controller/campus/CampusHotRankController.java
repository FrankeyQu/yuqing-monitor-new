package com.stonedt.intelligence.controller.campus;

import com.stonedt.intelligence.service.campus.CampusHotRankService;
import com.stonedt.intelligence.vo.ResultVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/campus/hot-rank")
public class CampusHotRankController {

    private final CampusHotRankService campusHotRankService;

    public CampusHotRankController(CampusHotRankService campusHotRankService) {
        this.campusHotRankService = campusHotRankService;
    }

    @GetMapping("/list")
    public ResultVO<Map<String, List<Map<String, Object>>>> list() {
        return ResultVO.success(campusHotRankService.getHotRank());
    }
}
