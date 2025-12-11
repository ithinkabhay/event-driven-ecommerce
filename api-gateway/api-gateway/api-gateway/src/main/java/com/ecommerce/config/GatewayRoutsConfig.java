package com.ecommerce.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutsConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder){

        return builder.routes()
                // Forward /user/** to user-service
                .route("user-service", r -> r
                        .path("/user/**")
                        .uri("http://localhost:8080")
                )
                // Forward /orders/** to order-service
                .route("order-service", r -> r
                        .path("/orders/**")
                        .uri("http://localhost:8081"))
                // Forward /products/** to inventory-service
                .route("inventory-service", r -> r
                        .path("/products/**")
                        .uri("http://localhost:8082"))
                .route("payment-service", r -> r
                        .path("/payments/**")
                        .uri("http://localhost:8083"))
                .build();
    }
}
