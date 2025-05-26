package com.accenture.api_gateway.config;

import com.accenture.api_gateway.exceptions.JwtAuthenticationException;
import io.jsonwebtoken.Claims;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class JwtRequestProcessor {

    private final JwtUtils jwtUtils;

    public JwtRequestProcessor(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    public Optional<Claims> validateAndParse(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new JwtAuthenticationException("Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
        }

        token = token.substring(7);

        if (jwtUtils.isTokenExpired(token)) {
            throw new JwtAuthenticationException("Expired JWT token", HttpStatus.UNAUTHORIZED);
        }

        return Optional.of(jwtUtils.parseClaims(token));
    }
}
