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
                .route("admin-sale-point-service", r -> r.path("/api/sale-point/admin/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter))
                        .uri(SALEPOINTURL))
                .route("admin-cost-service", r -> r.path("/api/sale-point/cost/admin/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter))
                        .uri(SALEPOINTURL))
                .route("internal-sale-point", r -> r.path("/api/sale-point/admin/internal-use/**")
                        .filters(f -> f.addRequestHeader("X-Internal-Token", "${internal.secret.token}"))
                        .uri(SALEPOINTURL))
                .route("admin-accreditation-service", r -> r.path("/api/accreditation/admin/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter))
                        .uri(ACCREDITATIONURL))
                .route("user-accreditation-service", r -> r.path("/api/accreditation/user/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter))
                        .uri(ACCREDITATIONURL))
                .route("admin-service", r -> r.path("/api/user/admin/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter))
                        .uri(USERURL))
                .route("auth-service", r -> r.path("/api/user/auth/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter))
                        .uri(USERURL))
                .route("internal-user", r -> r.path("/api/user/admin/internal-use/**")
                        .filters(f -> f.addRequestHeader("X-Internal-Token", "${internal.secret.token}"))
                        .uri(USERURL))
                .build();
    }
}
