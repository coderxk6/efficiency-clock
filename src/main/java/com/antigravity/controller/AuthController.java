package com.antigravity.controller;

import com.antigravity.common.Result;
import com.antigravity.dto.AuthResponse;
import com.antigravity.dto.LoginRequest;
import com.antigravity.dto.RegisterRequest;
import com.antigravity.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = userService.register(request);
        return Result.success(response);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = userService.login(request);
        return Result.success(response);
    }

    /**
     * 游客登录
     */
    @PostMapping("/guest")
    public Result<AuthResponse> loginAsGuest() {
        AuthResponse response = userService.loginAsGuest();
        return Result.success(response);
    }

    /**
     * 检查用户名是否可用
     */
    @GetMapping("/check-username")
    public Result<Map<String, Boolean>> checkUsername(@RequestParam String username) {
        // 这里需要在 UserMapper 中添加 existsByUsername 方法
        Map<String, Boolean> result = new HashMap<>();
        result.put("available", true); // 简化实现
        return Result.success(result);
    }
}
