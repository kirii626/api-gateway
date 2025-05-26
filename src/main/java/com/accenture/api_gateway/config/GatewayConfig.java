package com.accenture.api_gateway.config;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration
public class GatewayConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final String USERURL = "lb://user-microservice";
    private static final String SALEPOINTURL = "lb://sale-point-service";
    private static final String ACCREDITATIONURL = "lb://accreditation-service";

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder routeLocatorBuilder) {
        return routeLocatorBuilder.routes()
                .route("admin-sale-point-service", r -> r.path("/api/admin-sale-point/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter))
                        .uri(SALEPOINTURL))
                .route("admin-cost-service", r -> r.path("/api/admin-cost/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter))
                        .uri(SALEPOINTURL))
                .route("admin-accreditation-service", r -> r.path("/api/admin-accreditation/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter))
                        .uri(ACCREDITATIONURL))
                .route("user-accreditation-service", r -> r.path("/api/user-accreditation/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter))
                        .uri(ACCREDITATIONURL))
                .route("user-service", r -> r.path("/api/user/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter))
                        .uri(USERURL))
                .route("admin-service", r -> r.path("/api/admin/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter))
                        .uri(USERURL))
                .route("auth-service", r -> r.path("/api/auth/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter))
                        .uri(USERURL))
                .build();
    }
}
