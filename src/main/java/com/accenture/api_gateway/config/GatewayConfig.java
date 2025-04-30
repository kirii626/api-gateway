package com.accenture.api_gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public GatewayConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder routeLocatorBuilder) {
        return routeLocatorBuilder.routes()
                .route("admin-sale-point-service", r -> r.path("/api/admin-sale-point/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter))
                        .uri("lb://sale-point-service"))
                .route("admin-cost-service", r -> r.path("/api/admin-cost/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter))
                        .uri("lb://sale-point-service"))
                .route("admin-accreditation-service", r -> r.path("/api/admin-accreditation/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter))
                        .uri("lb://accreditation-service"))
                .route("user-accreditation-service", r -> r.path("/api/user-accreditation/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter))
                        .uri("lb://accreditation-service"))
                .route("user-service", r -> r.path("/api/user/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter))
                        .uri("lb://user-microservice"))
                .route("admin-service", r -> r.path("/api/admin/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter))
                        .uri("lb://user-microservice"))
                .route("auth-service", r -> r.path("/api/auth/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter))
                        .uri("lb://user-microservice"))
                .build();
    }
}
