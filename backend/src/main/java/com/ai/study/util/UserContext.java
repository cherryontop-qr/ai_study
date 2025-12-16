package com.ai.study.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class UserContext {
    private static final String USER_ID_HEADER = "X-User-Id";

    public static Long getUserId() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String userIdStr = request.getHeader(USER_ID_HEADER);
            if (userIdStr != null && !userIdStr.isEmpty()) {
                try {
                    return Long.parseLong(userIdStr);
                } catch (NumberFormatException e) {
                    // 如果解析失败，尝试从 Authorization header 中解析
                    String authHeader = request.getHeader("Authorization");
                    if (authHeader != null && authHeader.startsWith("Bearer jwt-token-")) {
                        // 简化处理：从 token 中提取 userId（实际应该用 JWT 解析）
                        String token = authHeader.substring(7);
                        String[] parts = token.split("-");
                        if (parts.length >= 3) {
                            try {
                                return Long.parseLong(parts[2]);
                            } catch (NumberFormatException ignored) {
                            }
                        }
                    }
                }
            }
        }
        // 默认返回 1（测试用，实际应该抛出异常）
        return 1L;
    }
}

