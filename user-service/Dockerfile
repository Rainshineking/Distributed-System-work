# Dockerfile for user-service
FROM openjdk:21-slim
LABEL maintainer="distributed-system-work"

COPY target/user-service.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "/app.jar"]
