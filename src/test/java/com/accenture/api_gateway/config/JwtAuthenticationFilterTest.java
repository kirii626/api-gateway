package com.accenture.api_gateway.config;

import com.accenture.api_gateway.exceptions.ErrorResponseBuilder;
import com.accenture.api_gateway.exceptions.JwtAuthenticationException;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;


import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtRequestProcessor jwtProcessor;

    @Mock
    private ErrorResponseBuilder errorResponseBuilder;

    @Mock
    private GatewayFilterChain filterChain;

    @Mock
    private Claims claims;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(jwtProcessor, errorResponseBuilder);
    }

    @Test
    void allowsAuthPathWithoutValidation() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/user/auth/login").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        when(filterChain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();

        verify(jwtProcessor, never()).validateAndParse(any());
        verify(filterChain).filter(exchange);
    }

    @Test
    void allowsAccessWithValidTokenAndRole() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/secure/data")
                .header(HttpHeaders.AUTHORIZATION, "Bearer token")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        when(jwtProcessor.validateAndParse("Bearer token")).thenReturn(Optional.of(claims));
        when(claims.getSubject()).thenReturn("user1");
        when(claims.get("roleType", String.class)).thenReturn("USER");
        when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();

        verify(filterChain).filter(any(ServerWebExchange.class));
    }

    @Test
    void deniesAccessToAdminPathForNonAdminRole() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/admin/dashboard")
                .header(HttpHeaders.AUTHORIZATION, "Bearer token")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        when(jwtProcessor.validateAndParse("Bearer token")).thenReturn(Optional.of(claims));
        when(claims.getSubject()).thenReturn("user1");
        when(claims.get("roleType", String.class)).thenReturn("USER");
        when(errorResponseBuilder.build(exchange, "Access Denied", HttpStatus.FORBIDDEN)).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();

        verify(errorResponseBuilder).build(exchange, "Access Denied", HttpStatus.FORBIDDEN);
    }

    @Test
    void returnsUnauthorizedOnJwtAuthenticationException() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/secure/data")
                .header(HttpHeaders.AUTHORIZATION, "Bearer token")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        JwtAuthenticationException ex = new JwtAuthenticationException("Invalid", HttpStatus.UNAUTHORIZED);
        when(jwtProcessor.validateAndParse("Bearer token")).thenThrow(ex);
        when(errorResponseBuilder.build(exchange, "Invalid", HttpStatus.UNAUTHORIZED)).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();

        verify(errorResponseBuilder).build(exchange, "Invalid", HttpStatus.UNAUTHORIZED);
    }

    @Test
    void returnsUnauthorizedOnUnexpectedException() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/secure/data")
                .header(HttpHeaders.AUTHORIZATION, "Bearer token")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        when(jwtProcessor.validateAndParse("Bearer token")).thenThrow(new RuntimeException("Unexpected"));
        when(errorResponseBuilder.build(exchange, "Invalid authentication token", HttpStatus.UNAUTHORIZED)).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();

        verify(errorResponseBuilder).build(exchange, "Invalid authentication token", HttpStatus.UNAUTHORIZED);
    }
}
