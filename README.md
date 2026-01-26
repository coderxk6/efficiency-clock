# 🌌 Antigravity - Efficiency Clock
> **极致美学的效率利器：打破平庸，掌控时间。**

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.2-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Vue.js](https://img.shields.io/badge/Vue.js-3.4-blue.svg)](https://vuejs.org/)
[![Docker](https://img.shields.io/badge/Docker-Supported-blue)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

**Antigravity** 是一款融合了 **番茄钟**、**任务管理** 与 **游戏化激励系统** 的全栈效率应用。它不仅是一个工具，更通过深蓝与极光的视觉设计、沉浸式的动画交互，带给你前所未有的专注体验。

---

## ✨ 核心特性

- ⏳ **沉浸专注模式**：精美的呼吸灯效果时钟，支持番茄钟倒计时，助力深度工作。
- 🎮 **游戏化等级系统**：每一次专注都会转化为经验值，升级解锁成就，让进步清晰可见。
- 📝 **极简任务管理**：快速创建、多任务并行处理，完成后一键勾选，数据自动同步。
- 🎆 **视觉激励反馈**：达成里程碑时触发炫酷的粒子礼花动画，仪式感拉满。
- 📱 **多端全兼容**：自适应移动端与桌面端，支持通过 Docker 快速部署并实现手机远程访问。

---

## 🛠️ 技术架构

### 后端 (Java Ecosystem)
- **Framework**: Spring Boot 3.2.2 (Java 17)
- **ORM**: MyBatis
- **Database**: H2 Database (单文件持久化，零配置启动)
- **API**: RESTful 风格接口

### 前端 (Modern Web)
- **Framework**: Vue 3 (Composition API)
- **Build Tool**: Vite
- **UI Components**: Vant 4 (高度定制化主题)
- **Animations**: CSS3 Keyframes + Canvas-confetti

---

## 🚀 快速开始

### 方案一：Docker 一键部署 (推荐 ⚡)
如果你有 Docker 环境，这是最快捷的方式。

1. **克隆项目**
   ```bash
   git clone https://github.com/你的用户名/efficiency-clock.git
   cd efficiency-clock
   ```
2. **启动服务**
   ```bash
   docker-compose up -d
   ```
3. **访问**
   在浏览器打开 `http://localhost` 即可。

### 方案二：本地开发调试
#### 1. 后端启动
- 确保已安装 JDK 17。
- 使用 IDE 打开根目录，运行 `AntigravityApplication.java`。
- 默认端口：`8080`

#### 2. 前端启动
```bash
cd frontend
npm install
npm run dev
```
- 访问地址：`http://localhost:9999`

---

## 📂 项目结构

```text
efficiency-clock/
├── frontend/              # Vue 3 前端工程
│   ├── src/               # 源代码 (App.vue, main.js, style.css)
│   └── vite.config.js     # 前端构建配置
├── src/                   # Spring Boot 后端工程
│   ├── main/java/         # 业务逻辑 (Controller, Service, Mapper)
│   └── main/resources/    # 配置与 SQL 脚本
├── Dockerfile             # 多阶段构建镜像定义
├── docker-compose.yml     # 容器编排定义
└── pom.xml                # Maven 依赖管理
```

---

## 📜 开源协议
本项目基于 [MIT License](LICENSE) 协议。

---

**Antigravity** - *Stay focused, stay wild.*  
如果你喜欢这个项目，欢迎点个 **⭐ Star** 支持一下！
