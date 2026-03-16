package com.example.apigateway;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 网关路由配置
 */
@Configuration
public class GatewayConfig {
    
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("user-service", r -> r
                        .path("/api/users/**")
                        .uri("http://localhost:8081"))
                .route("product-service", r -> r
                        .path("/api/products/**")
                        .uri("http://localhost:8083"))
                .route("inventory-service", r -> r
                        .path("/api/inventory/**")
                        .uri("http://localhost:8084"))
                .route("order-service", r -> r
                        .path("/api/orders/**")
                        .uri("http://localhost:8085"))
                .build();
    }
}
