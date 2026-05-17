package com.stonedt.intelligence.service.minority.controller;

import com.alibaba.fastjson.JSONObject;
import com.stonedt.intelligence.aop.SystemControllerLog;
import com.stonedt.intelligence.service.minority.model.MinoritySearchParam;
import com.stonedt.intelligence.service.minority.service.MinoritySearchService;
import com.stonedt.intelligence.util.UserUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 蒙语/维语搜索 REST API
 */
@RestController
@RequestMapping("/api/minority")
public class MinoritySearchController {

    private static final Logger log = LoggerFactory.getLogger(MinoritySearchController.class);

    @Autowired
    private MinoritySearchService minoritySearchService;

    @Autowired
    private UserUtil userUtil;

    /**
     * 蒙语/维语搜索接口
     *
     * @param keyword 搜索关键词（必填）
     * @param engine  搜索引擎：baidu / bing / all（可选，默认 all）
     * @param page    页码（可选，默认 1）
     * @param analyze 是否启用 LLM 分析（可选，默认 true）
     * @param request HttpServletRequest（用于鉴权）
     * @return JSON 响应，格式：{code: 0, message: "success", data: { ... }}
     */
    @GetMapping("/search")
    @SystemControllerLog(module = "少数民族舆情", submodule = "蒙语/维语搜索", type = "查询", operation = "search")
    public JSONObject search(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "all") String engine,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "true") Boolean analyze,
            HttpServletRequest request) {

        JSONObject response = new JSONObject();

        try {
            // 鉴权：获取当前登录用户（会抛出异常若未登录）
            userUtil.getuser(request);
        } catch (Exception e) {
            response.put("code", 401);
            response.put("message", "未登录或登录已过期");
            response.put("data", null);
            return response;
        }

        // 参数校验：keyword 必填
        if (StringUtils.isBlank(keyword)) {
            response.put("code", 400);
            response.put("message", "keyword 不能为空");
            response.put("data", null);
            return response;
        }

        try {
            // 构建请求参数
            MinoritySearchParam param = new MinoritySearchParam();
            param.setKeyword(keyword.trim());
            param.setEngine(engine);
            param.setPage(page);
            param.setAnalyze(analyze);

            // 执行搜索
            Map<String, Object> data = minoritySearchService.search(param);

            response.put("code", 0);
            response.put("message", "success");
            response.put("data", data);
        } catch (Exception e) {
            log.error("少数民族搜索 API 异常: {}", e.getMessage(), e);
            response.put("code", 500);
            response.put("message", "服务器内部错误: " + e.getMessage());
            response.put("data", null);
        }

        return response;
    }
}
