package com.stonedt.intelligence.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Token cookie helper for servlet containers that do not expose SameSite directly.
 */
public final class TokenCookieUtil {

    private static final String TOKEN_COOKIE_NAME = "token";
    private static final String SAME_SITE = "Lax";

    private TokenCookieUtil() {
    }

    public static void addTokenCookie(HttpServletRequest request,
                                      HttpServletResponse response,
                                      String token,
                                      int maxAgeSeconds) {
        response.addHeader("Set-Cookie", buildCookieHeader(request, token, maxAgeSeconds));
        response.setHeader("token", token);
    }

    public static void clearTokenCookie(HttpServletRequest request,
                                        HttpServletResponse response) {
        response.addHeader("Set-Cookie", buildCookieHeader(request, "", 0));
    }

    private static String buildCookieHeader(HttpServletRequest request, String value, int maxAgeSeconds) {
        StringBuilder header = new StringBuilder();
        header.append(TOKEN_COOKIE_NAME).append("=").append(value == null ? "" : value)
                .append("; Path=/")
                .append("; Max-Age=").append(Math.max(maxAgeSeconds, 0))
                .append("; HttpOnly")
                .append("; SameSite=").append(SAME_SITE);
        if (isSecureRequest(request)) {
            header.append("; Secure");
        }
        return header.toString();
    }

    private static boolean isSecureRequest(HttpServletRequest request) {
        if (request == null) {
            return false;
        }
        if (request.isSecure()) {
            return true;
        }
        String forwardedProto = request.getHeader("X-Forwarded-Proto");
        return "https".equalsIgnoreCase(forwardedProto);
    }
}
