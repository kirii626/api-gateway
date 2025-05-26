package com.accenture.api_gateway.exceptions;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class ErrorResponseBuilder {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Mono<Void> build(ServerWebExchange exchange, String message, HttpStatus status) {
        Map<String, Object> response = Map.of(
                "error", status.getReasonPhrase(),
                "message", message,
                "status", status.value()
        );

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(response);
            exchange.getResponse().setStatusCode(status);
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

            return exchange.getResponse().writeWith(
                    Mono.just(exchange.getResponse().bufferFactory().wrap(bytes))
            );
        } catch (Exception e) {
            exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            return exchange.getResponse().setComplete();
        }
    }
}
