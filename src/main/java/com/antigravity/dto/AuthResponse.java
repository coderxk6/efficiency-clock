package com.antigravity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 认证响应
 */
@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String username;
    private String nickname;
    private Boolean isGuest;
}
