# --- 阶段一：构建前端 ---
FROM node:20-alpine AS frontend-builder
WORKDIR /app/frontend
# 复制前端配置文件
COPY frontend/package*.json ./
RUN npm install
# 复制前端源代码并构建
COPY frontend/ ./
RUN npm run build

# --- 阶段二：构建后端 ---
FROM maven:3.9.6-eclipse-temurin-17-alpine AS backend-builder
WORKDIR /app

# 配置阿里云 Maven 镜像以加速并解决网络连接问题
RUN mkdir -p /root/.m2 && \
    echo '<?xml version="1.0" encoding="UTF-8"?><settings xmlns="http://maven.apache.org/SETTINGS/1.2.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.2.0 https://maven.apache.org/xsd/settings-1.2.0.xsd"><mirrors><mirror><id>aliyunmaven</id><mirrorOf>central</mirrorOf><name>aliyun maven</name><url>https://maven.aliyun.com/repository/public</url></mirror></mirrors></settings>' > /root/.m2/settings.xml

# 复制 Maven 配置文件
COPY pom.xml .
# 下载依赖（利用 Docker 缓存）
RUN mvn dependency:go-offline

# 复制后端源代码
COPY src ./src
# 将阶段一构建好的静态文件复制到后端的静态资源目录
COPY --from=frontend-builder /app/src/main/resources/static ./src/main/resources/static

# 打包后端 (增加 -U 强制刷新依赖)
RUN mvn clean package -DskipTests -U

# --- 阶段三：运行环境 ---
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 安装 netcat 用于网络检测
RUN apk add --no-cache netcat-openbsd

# 显式排除 .original 的普通包，只复制包含依赖的胖包
COPY --from=backend-builder /app/target/antigravity-0.0.1-SNAPSHOT.jar app.jar

# 复制等待脚本
COPY wait-for-mysql.sh /wait-for-mysql.sh
RUN chmod +x /wait-for-mysql.sh

# 暴露端口
EXPOSE 8080

# 启动命令：先等待 MySQL，再启动应用
ENTRYPOINT ["/wait-for-mysql.sh", "java", "-Dfile.encoding=UTF-8", "-jar", "app.jar"]
