package com.accenture.api_gateway.config;

import com.accenture.api_gateway.exceptions.ErrorResponseBuilder;
import com.accenture.api_gateway.exceptions.JwtAuthenticationException;
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



import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtRequestProcessor jwtRequestProcessor;

    @Mock
    private ErrorResponseBuilder errorResponseBuilder;

    @Mock
    private GatewayFilterChain filterChain;

    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private MockServerHttpRequest request;
    private ServerWebExchange exchange;

    @BeforeEach
    void setUp() {
        jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtRequestProcessor, errorResponseBuilder);
    }

    @Test
    void shouldAllowRequestsToAuthPathWithoutValidation() {
        request = MockServerHttpRequest.get("/api/auth/login").build();
        exchange = MockServerWebExchange.from(request);

        when(filterChain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(jwtAuthenticationFilter.filter(exchange, filterChain))
                .verifyComplete();

        verify(jwtRequestProcessor, never()).validateAndParse(any());
        verify(filterChain).filter(exchange);
    }

    @Test
    void shouldReturnUnauthorizedIfTokenIsMissing() {
        request = MockServerHttpRequest.get("/api/secure/data").build();
        exchange = MockServerWebExchange.from(request);

        Mono<Void> errorMono = Mono.empty();
        when(errorResponseBuilder.build(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED))
                .thenReturn(errorMono);

        when(jwtRequestProcessor.validateAndParse(null))
                .thenThrow(new JwtAuthenticationException("Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED));

        StepVerifier.create(jwtAuthenticationFilter.filter(exchange, filterChain))
                .verifyComplete();

        verify(errorResponseBuilder).build(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldReturnUnauthorizedOnUnexpectedException() {
        request = MockServerHttpRequest.get("/api/secure/data")
                .header(HttpHeaders.AUTHORIZATION, "Bearer something")
                .build();
        exchange = MockServerWebExchange.from(request);

        when(jwtRequestProcessor.validateAndParse("something"))
                .thenThrow(new RuntimeException("Unexpected failure"));

        when(errorResponseBuilder.build(exchange, "Invalid authentication token", HttpStatus.UNAUTHORIZED))
                .thenReturn(Mono.empty());

        StepVerifier.create(jwtAuthenticationFilter.filter(exchange, filterChain))
                .verifyComplete();

        verify(errorResponseBuilder).build(exchange, "Invalid authentication token", HttpStatus.UNAUTHORIZED);
    }
}
