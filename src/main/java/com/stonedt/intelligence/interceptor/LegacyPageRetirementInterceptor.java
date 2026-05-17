package com.stonedt.intelligence.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Component
public class LegacyPageRetirementInterceptor implements HandlerInterceptor {

    private static final String RETIRED_HTML = "<!doctype html><html lang=\"zh-CN\"><head>"
            + "<meta charset=\"utf-8\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">"
            + "<title>旧页面已下线</title></head><body>"
            + "<main style=\"max-width:560px;margin:120px auto;padding:32px;border:1px solid #d8dee8;"
            + "font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',sans-serif;color:#1f2937;\">"
            + "<h1 style=\"margin:0 0 12px;font-size:24px;\">旧页面已下线</h1>"
            + "<p style=\"margin:0;line-height:1.8;color:#5b6472;\">旧版舆情页面和静态资源已经移除。"
            + "请从校园舆情综合研判平台的新前端进入业务功能。</p>"
            + "</main></body></html>";

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private final List<String> retiredPagePatterns = Arrays.asList(
            "/",
            "/login",
            "/forgotpwd",
            "/jumpLogin",
            "/wechatJumpLogin",
            "/analysis",
            "/displayboard",
            "/hot/hotpage",
            "/mobile/monitor",
            "/mobile/monitor/detail",
            "/mobile/warning",
            "/monitor",
            "/monitor/detail/*",
            "/monitor/wxGroup",
            "/project",
            "/project/addproject",
            "/project/detail",
            "/project/editproject",
            "/publicoption",
            "/publicoption/backanalysis",
            "/publicoption/eventContext",
            "/publicoption/eventTrace",
            "/publicoption/hotAnalysis",
            "/publicoption/netizensAnalysis",
            "/publicoption/popular_feelings_analys",
            "/publicoption/propagationAnalysis",
            "/publicoption/reportdetail/*",
            "/publicoption/reportlist",
            "/publicoption/statistics",
            "/publicoption/thematicAnalysis",
            "/publicoption/unscrambleContent",
            "/report",
            "/report/*",
            "/search",
            "/system/feedback",
            "/system/favorite",
            "/system/preference",
            "/system/productmanual/online",
            "/system/warning",
            "/system/warningedit",
            "/system/warningmsg",
            "/timelysearch",
            "/timelysearch/index",
            "/timelysearch/result",
            "/volume",
            "/fullsearch",
            "/fullsearch/result",
            "/fullsearch/biddingdetail/*",
            "/fullsearch/companyDetail/*",
            "/fullsearch/detail/*",
            "/fullsearch/doctorDetail/*",
            "/fullsearch/executionPersonDetail/*",
            "/fullsearch/investmentDetail/*",
            "/fullsearch/inviteDetails/*",
            "/fullsearch/judgmentDetail/*",
            "/fullsearch/knowLedgeDetail/*",
            "/fullsearch/lawyerDetail/*",
            "/fullsearch/professorDetail/*",
            "/fullsearch/reportdetail/*/*",
            "/fullsearch/thesisnDetail/*"
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String path = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }
        for (String pattern : retiredPagePatterns) {
            if (pathMatcher.match(pattern, path)) {
                return retire(response);
            }
        }
        if (pathMatcher.match("/user/*", path) && !"/user/getwechatqrcode".equals(path)) {
            return retire(response);
        }
        return true;
    }

    private boolean retire(HttpServletResponse response) throws Exception {
        response.setStatus(HttpServletResponse.SC_GONE);
        response.setContentType("text/html;charset=UTF-8");
        response.setContentLength(RETIRED_HTML.getBytes(StandardCharsets.UTF_8).length);
        response.getWriter().write(RETIRED_HTML);
        return false;
    }
}
