package com.accenture.api_gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder routeLocatorBuilder) {
        return routeLocatorBuilder.routes()
                .route("sale-point-service", r -> r.path("/api/sale-point/**")
                        .uri("lb://sale-point-service"))
                .route("products-service", r -> r.path("/api/costs/**")
                        .uri("lb://sale-point-service"))
                .route("accreditations-service", r -> r.path("/api/accreditations/**")
                        .uri("lb://accreditation-service"))
                .build();
    }
}
