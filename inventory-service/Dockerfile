# Dockerfile for inventory-service
FROM openjdk:21-slim
LABEL maintainer="distributed-system-work"

COPY target/inventory-service.jar app.jar

EXPOSE 8084

ENTRYPOINT ["java", "-jar", "/app.jar"]
