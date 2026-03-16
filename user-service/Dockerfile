# Dockerfile for user-service
FROM openjdk:21-slim
LABEL maintainer="distributed-system-work"

WORKDIR /app

COPY target/user-service.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
