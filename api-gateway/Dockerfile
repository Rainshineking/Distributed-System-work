# Dockerfile for api-gateway
FROM eclipse-temurin:21-jre
LABEL maintainer="distributed-system-work"

WORKDIR /app

COPY target/api-gateway-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
