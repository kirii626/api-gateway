package com.accenture.api_gateway.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.ws.rs.core.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.*;



@Component
public class JwtAuthenticationFilter implements GatewayFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtils jwtUtils;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public JwtAuthenticationFilter(JwtUtils jwtUtils){
        this.jwtUtils = jwtUtils;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String token = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        String requestPath = exchange.getRequest().getPath().toString();

        HttpMethod method = exchange.getRequest().getMethod();
        logger.info("Incoming request: {} {}", method, requestPath);

        if (!exchange.getRequest().getPath().toString().startsWith("/api/auth")) {
            if (token == null || !token.startsWith("Bearer ")) {
                return onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
            }

            token = token.substring(7);

            try {
                if (jwtUtils.isTokenExpired(token)) {
                    return onError(exchange, "Expired JWT token", HttpStatus.UNAUTHORIZED);
                }

                Claims claims = jwtUtils.parseClaims(token);
                String username = claims.getSubject();
                String roleType = claims.get("roleType", String.class);

                logger.info("Valid token for user: {} | Role: {}", username, roleType);

                if ((requestPath.startsWith("/api/admin"))
                        && !"ADMIN".equals(roleType)) {
                    return onError(exchange, "Access Denied: ADMIN role required", HttpStatus.FORBIDDEN);
                }

                ServerWebExchange modifiedExchange = exchange.mutate()
                        .request(builder -> builder
                                .header("X-Username", username)
                                .header("X-Role", roleType))
                        .build();

                return chain.filter(modifiedExchange);
            } catch (Exception e) {
                logger.error("JWT token parsing failed ", e);
                return onError(exchange, "Invalid authentication token", HttpStatus.UNAUTHORIZED);
            }
        }

        return chain.filter(exchange);
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        logger.warn("{} - Path: {}", err, exchange.getRequest().getPath());

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", httpStatus.getReasonPhrase());
        errorResponse.put("message", err);
        errorResponse.put("status", httpStatus.value());

        try {
            String jsonResponse = objectMapper.writeValueAsString(errorResponse);

            exchange.getResponse().setStatusCode(httpStatus);
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
            byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);

            return exchange.getResponse().writeWith(Mono.just(exchange.getResponse()
                    .bufferFactory().wrap(responseBytes)));

        } catch (Exception e) {
            logger.warn("Failed to write JSON error response ", e);
            exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            return exchange.getResponse().setComplete();
        }
    }
}
