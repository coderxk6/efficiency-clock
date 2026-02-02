# 登录功能实现文档

## 实现概述

为 Efficiency Clock 项目增加了完整的用户登录功能，支持：
- ✅ 用户注册和登录
- ✅ 游客模式（无需注册即可使用）
- ✅ 基于 JWT 的无状态认证
- ✅ 多用户数据隔离

## 技术栈选择

### 🔐 安全框架：Spring Security

**选择理由：**

1. **官方支持**：Spring Boot 官方推荐的安全解决方案，与 Spring 生态无缝集成
2. **功能完善**：支持多种认证方式（表单登录、JWT、OAuth2 等）
3. **游客模式友好**：通过 `permitAll()` 可以轻松配置匿名访问
4. **成熟稳定**：社区活跃，文档丰富，问题容易解决
5. **易于扩展**：未来如需添加第三方登录（如 OAuth2、微信登录）非常方便

### 🔑 认证方式：JWT (JSON Web Token)

**选择理由：**
- 无状态：不需要在服务器端存储 session
- 跨域友好：适合前后端分离架构
- 扩展性强：可携带用户信息，减少数据库查询

## 架构设计

### 后端部分

#### 1. 依赖添加 (pom.xml)
```xml
<!-- Spring Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- JWT Support -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.3</version>
</dependency>
```

#### 2. 数据库设计

**用户表 (user)**
```sql
CREATE TABLE user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '加密密码',
    nickname VARCHAR(100) COMMENT '昵称',
    is_guest BOOLEAN DEFAULT FALSE COMMENT '是否为游客',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

**更新现有表**
- `focus_task` 表增加 `user_id` 字段，关联用户
- `user_level` 表增加 `user_id` 字段，每个用户独立的等级数据

#### 3. 核心组件

| 组件 | 作用 |
|------|------|
| `SecurityConfig` | Spring Security 配置，定义公开端点、JWT 过滤器 |
| `JwtAuthenticationFilter` | JWT 认证过滤器，从请求头提取并验证 token |
| `JwtUtil` | JWT 工具类，生成、解析和验证 token |
| `UserService` | 用户服务，处理注册、登录、游客登录逻辑 |
| `AuthController` | 认证控制器，提供注册、登录、游客登录接口 |
| `SecurityUtils` | 获取当前登录用户信息的工具类 |

#### 4. API 端点

**公开接口（无需认证）：**
- `POST /api/auth/register` - 用户注册
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/guest` - 游客登录

**受保护接口（需要认证）：**
- `POST /api/focus/start` - 开始专注任务
- `PUT /api/focus/{taskId}/complete` - 完成任务
- `DELETE /api/focus/{taskId}` - 放弃任务
- `GET /api/focus/tasks` - 获取当前用户的运行中任务
- `GET /api/focus/history` - 获取当前用户的历史记录

### 前端部分

#### 1. 登录界面组件 (Login.vue)

**功能特性：**
- 美观的玻璃态设计
- 渐变色和动画效果
- 支持登录/注册切换
- 一键游客登录
- 表单验证

**设计亮点：**
- 背景动态光球效果
- 现代化的输入框设计（带图标）
- 平滑的过渡动画
- 响应式布局

#### 2. 主应用集成 (App.vue)

**更新内容：**
- 登录状态管理
- Token 自动注入到 HTTP 请求
- 用户信息展示
- 登出功能
- 条件渲染（未登录显示登录界面，登录后显示主应用）

## 用户体验

### 登录流程
1. 用户访问应用 → 显示登录界面
2. 用户选择：
   - **注册新账号**：填写用户名、密码、昵称（可选）
   - **登录已有账号**：输入用户名和密码
   - **游客模式**：点击"以游客身份继续"，系统自动创建临时账号
3. 登录成功 → Token 存储到 localStorage → 显示主应用
4. 后续请求自动携带 Token → 访问用户专属数据

### 数据隔离
- 每个用户只能看到自己的任务
- 每个用户有独立的等级和经验值
- 游客数据在会话期间保留（除非清除浏览器缓存）

## 安全特性

1. **密码加密**：使用 BCrypt 加密存储
2. **JWT Token**：
   - 有效期 24 小时（可配置）
   - 签名验证防止篡改
   - 包含用户 ID 和游客标识
3. **CORS 配置**：支持跨域请求
4. **用户隔离**：所有操作都验证用户身份

## 配置说明

### application.yml
```yaml
jwt:
  secret: ${JWT_SECRET:antigravity-efficiency-clock-secret-key-must-be-at-least-256-bits-long}
  expiration: ${JWT_EXPIRATION:86400000} # 24小时（毫秒）
```

**生产环境建议：**
- 使用环境变量设置 `JWT_SECRET`
- 定期更换密钥
- 根据需求调整 token 有效期

## 扩展建议

### 未来可增强功能：
1. **邮箱验证**：注册时发送验证邮件
2. **忘记密码**：通过邮箱重置密码
3. **第三方登录**：集成 OAuth2（Google、GitHub 等）
4. **记住我**：长期有效的 Token
5. **用户头像**：上传和显示头像
6. **多设备管理**：查看和管理登录设备
7. **权限分级**：管理员、普通用户等角色

## 常见问题

### Q: 游客模式的数据会保留吗？
A: 游客数据存储在数据库中，只要不清除浏览器的 localStorage（存储了 token），数据就会保留。

### Q: Token 过期后怎么办？
A: Token 过期后，前端请求会返回 401，用户需要重新登录。可以实现 Token 刷新机制来优化体验。

### Q: 如何将游客账号转为正式账号？
A: 当前未实现，可以添加"绑定账号"功能，允许游客设置用户名和密码。

## 总结

本次实现完整地为 Efficiency Clock 增加了用户系统，采用业界标准的 **Spring Security + JWT** 方案，确保了安全性和可扩展性。同时提供了友好的游客模式，降低了用户使用门槛，提升了用户体验。
