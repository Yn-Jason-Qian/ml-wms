# ── WMS 后端（运行时镜像）──
# 与 wms-server/Dockerfile 的区别：不在容器内跑 Maven，
# 直接使用 Jenkins 构建好的 fat jar，部署更快、也不依赖构建机网络。
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
COPY app.jar app.jar

EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=prod \
    TZ=Asia/Shanghai

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
