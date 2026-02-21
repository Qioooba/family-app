FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/*.jar app.jar

ENV JAVA_OPTS="-Xms256m -Xmx512m"
ENV SPRING_PROFILES_ACTIVE=prod
ENV MYSQL_HOST=mysql
ENV REDIS_HOST=redis
ENV ES_HOST=elasticsearch

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
