# Dockerfile for order-service
FROM eclipse-temurin:21-jre
LABEL maintainer="distributed-system-work"

WORKDIR /app

COPY target/order-service-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
