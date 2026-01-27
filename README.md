# 🌌 Antigravity - Efficiency Clock
> **极致美学的效率利器：打破平庸，掌控时间。**

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.2-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Vue.js](https://img.shields.io/badge/Vue.js-3.4-blue.svg)](https://vuejs.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![Docker](https://img.shields.io/badge/Docker-Supported-blue)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

**Antigravity** 是一款融合了 **修仙主题**、**番茄钟**、**任务管理** 与 **游戏化激励系统** 的全栈效率应用。它不仅是一个工具，更通过深蓝与极光的视觉设计、沉浸式的动画交互，带给你前所未有的专注体验。

---

## ✨ 核心特性

- ⏳ **沉浸专注模式**：精美的呼吸灯效果时钟，支持番茄钟倒计时，助力深度工作。
- 🧘 **修仙境界系统**：每一次专注都会转化为“天地灵气”（经验值），助你渡劫破境，从“炼气期”一路攀升至“创世神”。
- 📝 **多任务并行管理**：支持创建多个专注任务，独立计时，数据实时同步至数据库。
- 🎆 **视觉激励反馈**：达成任务或破境成功时触发炫酷的粒子礼花动画，仪式感拉满。
- 🐳 **Docker 化部署**：支持 Docker Compose 一键启动，集成 MySQL 8.0 环境，数据持久化存储。

---

## 🛠️ 技术架构

### 后端 (Java Ecosystem)
- **Framework**: Spring Boot 3.2.2 (Java 17)
- **ORM**: MyBatis
- **Database**: **MySQL 8.0** (支持 UTF-8mb4，完美兼容中文字符)
- **Init**: 启动自动执行 `schema.sql` 与 `data.sql`
- **Config**: 支持环境变量动态配置数据库连接

### 前端 (Modern Web)
- **Framework**: Vue 3 (Composition API)
- **Build Tool**: Vite
- **UI Components**: Vant 4 (高度定制化主题)
- **Animations**: CSS3 Keyframes + Canvas-confetti

---

## 🚀 快速开始

### 方案一：Docker 一键部署 (推荐 ⚡)
如果你有 Docker 环境，这是最快捷的方式。项目已预设好应用与数据库的关联。

1. **克隆项目**
   ```bash
   git clone <your-repository-url>
   cd efficiency-clock
   ```
2. **启动服务**
   ```bash
   # 首次启动或配置变更建议重新构建
   docker-compose up -d --build
   ```
3. **访问**
   在浏览器打开 `http://localhost` 即可。手机端可通过 `http://服务器IP` 访问。

### 方案二：本地开发调试
#### 1. 准备数据库
- 请确保本地已安装 MySQL 8.0+。
- 创建数据库 `antigravity`。
- 修改 `src/main/resources/application.yml` 中的数据库用户名和密码（默认为 `root/123456`）。

#### 2. 后端启动
- 确保已安装 JDK 17。
- 运行 `AntigravityApplication.java`。
- 默认端口：`8080`

#### 3. 前端启动
```bash
cd frontend
npm install
npm run dev
```
- 访问地址：`http://localhost:5173` (Vite 默认)

---

## 📂 项目结构

```text
efficiency-clock/
├── frontend/              # Vue 3 前端工程
├── src/                   # Spring Boot 后端工程
│   ├── main/java/         # 业务逻辑
│   └── main/resources/    # 配置与 SQL 脚本 (schema.sql, data.sql)
├── data/mysql             # [Docker映射] MySQL 数据持久化目录
├── Dockerfile             # 镜像构建文件
└── docker-compose.yml     # 容器编排 (App + MySQL)
```

---

## 📜 关键配置说明
- **数据库密码**: 默认设置为 `123456`，可在 `docker-compose.yml` 或 `application.yml` 中修改。
- **中文支持**: 已全链路开启 `utf8mb4`，支持在任务名或境界名中使用中文。

---

**Antigravity** - *Stay focused, stay wild.*  
如果你喜欢这个项目，欢迎点个 **⭐ Star** 支持一下！
