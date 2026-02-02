package com.antigravity.util;

import com.antigravity.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * 获取当前登录用户的工具类
 */
@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    /**
     * 获取当前登录用户的 ID
     */
    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            // 从数据库中获取用户
            return userService.getUserByUsername(username).getId();
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
