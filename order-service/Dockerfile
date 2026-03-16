# Dockerfile for order-service
FROM openjdk:21-slim
LABEL maintainer="distributed-system-work"

COPY target/order-service.jar app.jar

EXPOSE 8085

ENTRYPOINT ["java", "-jar", "/app.jar"]
