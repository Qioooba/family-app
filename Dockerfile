FROM eclipse-temurin:17-jdk-alpine as builder

WORKDIR /app

# 安装 Maven
RUN apk add --no-cache maven

# 复制父 pom
COPY backend/pom.xml .

# 复制 common-core
COPY backend/family-common/common-core ./family-common/common-core

# 构建 common-core
RUN mvn install -pl family-common/common-core -am -DskipTests -q

# 复制 ai-service
COPY backend/family-service/ai-service ./family-service/ai-service

# 构建 ai-service
RUN mvn clean package -pl family-service/ai-service -am -DskipTests -q

# 复制 family-service
COPY backend/family-service/family-service ./family-service/family-service

# 构建 family-service
RUN mvn clean package -pl family-service/family-service -am -DskipTests -q

# 运行阶段
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# 从 builder 复制 jar
COPY --from=builder /app/family-service/ai-service/target/ai-service-1.0.0.jar /app/ai-service.jar
COPY --from=builder /app/family-service/family-service/target/family-service-1.0.0.jar /app/family-service.jar

EXPOSE 8090 8080

# 默认启动 family-service
CMD ["java", "-jar", "/app/family-service.jar"]
