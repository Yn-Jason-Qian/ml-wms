# ── WMS 后端镜像（运行时）──
#
# 使用 CI 构建好的 fat jar（放到本目录的 app.jar），不在容器内跑 Maven：
# 部署更快，也不依赖目标服务器访问 Maven 中央仓库。
#
# 本地想构建镜像时，先生成 jar 再放到本目录：
#   cd wms-server && mvn -DskipTests package
#   cp wms-web/target/wms-web-*.jar ../deploy/server/app.jar
#   docker build -t wms-server ../deploy/server
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
COPY app.jar app.jar

EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=prod \
    TZ=Asia/Shanghai

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
