package com.accenture.api_gateway.config;

import com.accenture.api_gateway.exceptions.JwtAuthenticationException;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtRequestProcessorTest {

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private JwtRequestProcessor jwtRequestProcessor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        jwtRequestProcessor = new JwtRequestProcessor(jwtUtils);
    }

    @Test
    void shouldThrowExceptionWhenTokenIsNull() {
        JwtAuthenticationException exception = assertThrows(
                JwtAuthenticationException.class,
                () -> jwtRequestProcessor.validateAndParse(null)
        );
        assertEquals("Missing or invalid Authorization header", exception.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
    }

    @Test
    void shouldThrowExceptionWhenTokenDoesNotStartWithBearer() {
        String invalidToken = "invalid.token";

        JwtAuthenticationException exception = assertThrows(
                JwtAuthenticationException.class,
                () -> jwtRequestProcessor.validateAndParse(invalidToken)
        );
        assertEquals("Missing or invalid Authorization header", exception.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
    }

    @Test
    void shouldThrowExceptionWhenTokenIsExpired() {
        String token = "Bearer expired.token";

        when(jwtUtils.isTokenExpired("expired.token")).thenReturn(true);

        JwtAuthenticationException exception = assertThrows(
                JwtAuthenticationException.class,
                () -> jwtRequestProcessor.validateAndParse(token)
        );
        assertEquals("Expired JWT token", exception.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
    }

    @Test
    void shouldReturnClaimsWhenTokenIsValid() {
        String token = "Bearer valid.token";
        Claims claims = mock(Claims.class);

        when(jwtUtils.isTokenExpired("valid.token")).thenReturn(false);
        when(jwtUtils.parseClaims("valid.token")).thenReturn(claims);

        Optional<Claims> result = jwtRequestProcessor.validateAndParse(token);

        assertTrue(result.isPresent());
        assertEquals(claims, result.get());
    }
}
