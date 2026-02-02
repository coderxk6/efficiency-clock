package com.antigravity.service;

import com.antigravity.dto.AuthResponse;
import com.antigravity.dto.LoginRequest;
import com.antigravity.dto.RegisterRequest;
import com.antigravity.entity.User;
import com.antigravity.mapper.UserMapper;
import com.antigravity.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户服务
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * 用户注册
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // 检查用户名是否已存在
        if (userMapper.existsByUsername(request.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        // 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setIsGuest(false);

        userMapper.insert(user);

        // 生成 Token
        String token = jwtUtil.generateToken(user.getUsername(), user.getId(), false);
        return new AuthResponse(token, user.getUsername(), user.getNickname(), false);
    }

    /**
     * 用户登录
     */
    public AuthResponse login(LoginRequest request) {
        User user = userMapper.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("用户名或密码错误"));

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 生成 Token
        String token = jwtUtil.generateToken(user.getUsername(), user.getId(), user.getIsGuest());
        return new AuthResponse(token, user.getUsername(), user.getNickname(), user.getIsGuest());
    }

    /**
     * 游客登录
     */
    @Transactional
    public AuthResponse loginAsGuest() {
        // 生成唯一的游客用户名
        String guestUsername = "guest_" + System.currentTimeMillis();

        User user = new User();
        user.setUsername(guestUsername);
        user.setPassword(passwordEncoder.encode("guest"));
        user.setNickname("游客");
        user.setIsGuest(true);

        userMapper.insert(user);

        // 生成 Token
        String token = jwtUtil.generateToken(user.getUsername(), user.getId(), true);
        return new AuthResponse(token, user.getUsername(), user.getNickname(), true);
    }

    /**
     * 根据用户名获取用户
     */
    public User getUserByUsername(String username) {
        return userMapper.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    /**
     * 根据 ID 获取用户
     */
    public User getUserById(Long id) {
        return userMapper.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }
}
