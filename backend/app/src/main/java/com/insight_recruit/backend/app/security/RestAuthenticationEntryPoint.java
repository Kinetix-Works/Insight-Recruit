package com.insight_recruit.backend.app.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException authException
    ) throws IOException {
        Throwable cause = authException.getCause();
        String message = "Authentication required";
        if (cause instanceof ExpiredJwtException) {
            message = "Token expired";
        } else if (cause instanceof MalformedJwtException) {
            message = "Malformed token";
        }
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        objectMapper.writeValue(response.getOutputStream(), Map.of(
            "timestamp", LocalDateTime.now().toString(),
            "status", HttpStatus.UNAUTHORIZED.value(),
            "error", HttpStatus.UNAUTHORIZED.getReasonPhrase(),
            "message", message
        ));
    }
}
