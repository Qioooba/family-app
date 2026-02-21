FROM eclipse-temurin:17-jdk-alpine as builder

WORKDIR /app

# 安装 Maven
RUN apk add --no-cache maven

# 复制父pom和common-core
COPY backend/pom.xml .
COPY backend/family-common/common-core ./family-common/common-core

# 构建common-core
RUN mvn install -pl family-common/common-core -N -DskipTests || true

# 复制ai-service
COPY backend/family-service/ai-service ./family-service/ai-service

# 构建ai-service
RUN mvn clean package -pl family-service/ai-service -am -DskipTests

# 运行阶段
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY --from=builder /app/family-service/ai-service/target/ai-service-1.0.0.jar app.jar

EXPOSE 8090

ENTRYPOINT ["java", "-jar", "app.jar"]
