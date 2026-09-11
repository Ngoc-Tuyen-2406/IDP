package com.idp.idpapi.common.util;

import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;

public final class RequestUtils {

    private static final int DEVICE_NAME_LIMIT = 100;

    private RequestUtils() {
    }

    public static String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwardedFor)) {
            return forwardedFor.split(",")[0].trim();
        }

        String realIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(realIp)) {
            return realIp.trim();
        }

        return request.getRemoteAddr();
    }

    public static String resolveDeviceName(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (!StringUtils.hasText(userAgent)) {
            return "unknown-device";
        }
        return userAgent.length() > DEVICE_NAME_LIMIT
                ? userAgent.substring(0, DEVICE_NAME_LIMIT)
                : userAgent;
    }
}
