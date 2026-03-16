# Dockerfile for product-service
FROM openjdk:21-slim
LABEL maintainer="distributed-system-work"

COPY target/product-service.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "/app.jar"]
