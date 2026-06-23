# ── WMS Backend Dockerfile ──
# Multi-stage: Build with Maven → Run with JRE 21
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /build
COPY pom.xml .
COPY wms-common/pom.xml wms-common/
COPY wms-masterdata/pom.xml wms-masterdata/
COPY wms-inbound/pom.xml wms-inbound/
COPY wms-outbound/pom.xml wms-outbound/
COPY wms-inventory/pom.xml wms-inventory/
COPY wms-strategy/pom.xml wms-strategy/
COPY wms-task/pom.xml wms-task/
COPY wms-print/pom.xml wms-print/
COPY wms-web/pom.xml wms-web/
# Download dependencies (cache layer)
RUN mvn dependency:go-offline -q -B
COPY . .
RUN mvn clean package -pl wms-web -DskipTests -q

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /build/wms-web/target/*.jar app.jar
EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=prod
ENTRYPOINT ["java", "-jar", "app.jar"]
