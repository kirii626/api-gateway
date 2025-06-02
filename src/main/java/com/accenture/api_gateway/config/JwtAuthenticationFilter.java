package com.accenture.api_gateway.config;

import com.accenture.api_gateway.exceptions.ErrorResponseBuilder;
import com.accenture.api_gateway.exceptions.JwtAuthenticationException;
import io.jsonwebtoken.Claims;
import jakarta.ws.rs.core.HttpHeaders;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


@RequiredArgsConstructor
@Component
@Slf4j
public class JwtAuthenticationFilter implements GatewayFilter {

    private final JwtRequestProcessor jwtProcessor;
    private final ErrorResponseBuilder errorResponseBuilder;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String token = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        String path = exchange.getRequest().getPath().toString();
        HttpMethod method = exchange.getRequest().getMethod();

        log.info("Incoming request: {} {}", method, path);

        if (!path.startsWith("/api/user/auth")) {
            try {
                Claims claims = jwtProcessor.validateAndParse(token).orElseThrow();
                String username = claims.getSubject();
                String role = claims.get("roleType", String.class);

                if (path.contains("/admin")  && !"ADMIN".equals(role)) {
                    return errorResponseBuilder.build(exchange, "Access Denied", HttpStatus.FORBIDDEN);
                }

                ServerWebExchange modified = exchange.mutate()
                        .request(builder -> builder
                                .header("X-Username", username)
                                .header("X-Role", role))
                        .build();

                return chain.filter(modified);

            } catch (JwtAuthenticationException ex) {
                return errorResponseBuilder.build(exchange, ex.getMessage(), ex.getStatus());
            } catch (Exception ex) {
                log.error("Unexpected error parsing JWT", ex);
                return errorResponseBuilder.build(exchange, "Invalid authentication token", HttpStatus.UNAUTHORIZED);
            }
        }

        return chain.filter(exchange);
    }
}

