package com.stonedt.intelligence.interceptor;

import com.stonedt.intelligence.entity.User;
import com.stonedt.intelligence.service.campus.CampusPermissionService;
import com.stonedt.intelligence.util.UserUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class CampusPermissionInterceptor implements HandlerInterceptor {

    private final CampusPermissionService campusPermissionService;
    private final UserUtil userUtil;

    public CampusPermissionInterceptor(CampusPermissionService campusPermissionService,
                                       UserUtil userUtil) {
        this.campusPermissionService = campusPermissionService;
        this.userUtil = userUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        if (isSelfPermissionEndpoint(path)) {
            return true;
        }
        User user = userUtil.getuser(request);
        boolean allowed = campusPermissionService.hasApiPermission(user.getUser_id(), request.getMethod(), path);
        if (allowed) {
            return true;
        }
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=utf-8");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.getWriter().write("{\"code\":403,\"msg\":\"无校园权限，请联系管理员授权\"}");
        return false;
    }

    private boolean isSelfPermissionEndpoint(String path) {
        return "/campus/system/current-user".equals(path)
                || "/campus/system/menu-tree".equals(path);
    }
}
