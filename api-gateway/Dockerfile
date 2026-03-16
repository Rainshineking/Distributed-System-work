# Dockerfile for api-gateway
FROM openjdk:21-slim
LABEL maintainer="distributed-system-work"

COPY target/api-gateway.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app.jar"]
