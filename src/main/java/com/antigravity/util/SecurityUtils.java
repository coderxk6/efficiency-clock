package com.antigravity.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

/**
 * 获取当前登录用户的工具类
 */
@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final JwtUtil jwtUtil;

    /**
     * 获取当前登录用户的 ID
     */
    public Long getCurrentUserId() {
        // 从请求头中获取 Token
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String authHeader = request.getHeader("Authorization");
            
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    // 直接从 Token 中获取 userId
                    Long userId = jwtUtil.getUserIdFromToken(token);
                    System.out.println("Token: " + token.substring(0, Math.min(20, token.length())) + "... → userId: " + userId);
                    return userId;
                } catch (Exception e) {
                    System.out.println("Invalid token: " + e.getMessage());
                    throw new RuntimeException("无效的 Token");
                }
            }
        }
        
        throw new RuntimeException("未登录");
    }

    /**
     * 获取当前登录用户的用户名
     */
    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        throw new RuntimeException("未登录");
    }
}
