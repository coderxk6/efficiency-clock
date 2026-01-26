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
# 复制 Maven 配置文件
COPY pom.xml .
# 下载依赖（利用 Docker 缓存）
RUN mvn dependency:go-offline

# 复制后端源代码
COPY src ./src
# 将阶段一构建好的静态文件复制到后端的静态资源目录
COPY --from=frontend-builder /app/src/main/resources/static ./src/main/resources/static

# 打包后端
RUN mvn clean package -DskipTests

# --- 阶段三：运行环境 ---
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# 从阶段二复制打好的 JAR 包
COPY --from=backend-builder /app/target/*.jar app.jar

# 创建数据目录（用于 H2 数据库持久化）
RUN mkdir -p /app/data

# 暴露端口
EXPOSE 8080

# 启动命令
ENTRYPOINT ["java", "-jar", "app.jar"]
