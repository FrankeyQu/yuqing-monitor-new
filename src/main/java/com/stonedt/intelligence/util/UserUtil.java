package com.stonedt.intelligence.util;

import com.stonedt.intelligence.entity.User;
import com.stonedt.intelligence.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * description 用户操作工具 <br>
 * date: 2020/4/14 15:01 <br>
 * author: huajiancheng <br>
 * version: 1.0 <br>
 */
@Component
public class UserUtil {

    @Value("${token.expire-time}")
    private Integer expireTime;

    private final UserService userService;

    public UserUtil(UserService userService) {
        this.userService = userService;
    }

    public User getuser(HttpServletRequest request) {
        // 1. 优先从请求头中获取 token（API 调用）
        String token = request.getHeader("token");
        if (token != null && !token.isEmpty()) {
            try {
                return JWTUtils.getEntity(token, User.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // 2. 从 cookie 中获取 token（Web 页面调用）
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("token".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }
        if (token != null && !token.isEmpty()) {
            try {
                return JWTUtils.getEntity(token, User.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // 3. 未找到任何有效 token
        throw new RuntimeException("token不存在！");
    }

    public long getUserId(HttpServletRequest request) {
        // 1. 从请求头中获取 token（API 调用）
        String token = request.getHeader("token");
        if (token != null && !token.isEmpty()) {
            try {
                return JWTUtils.getEntity(token, User.class).getUser_id();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // 2. 从 cookie 中获取 token（Web 页面调用）
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("token".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }
        if (token != null && !token.isEmpty()) {
            try {
                return JWTUtils.getEntity(token, User.class).getUser_id();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // 3. 未找到任何有效 token
        throw new RuntimeException("token不存在！");
    }


    public void setUser(HttpServletRequest request,
                        HttpServletResponse response,
                        User user) throws Exception {
        // 生成token
        String newToken = userService.getToken(user);

        TokenCookieUtil.addTokenCookie(request, response, newToken, expireTime);
    }

    /**
     * 移除对象
     */
    public void removeUser(HttpServletRequest request,
                           HttpServletResponse response) {
        TokenCookieUtil.clearTokenCookie(request, response);
    }
}
