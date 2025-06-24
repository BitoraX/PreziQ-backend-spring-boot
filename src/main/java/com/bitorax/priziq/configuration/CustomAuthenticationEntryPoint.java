package com.bitorax.priziq.configuration;

import com.bitorax.priziq.exception.ErrorCode;
import com.bitorax.priziq.utils.ResponseUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper;

    public CustomAuthenticationEntryPoint() {
        this.objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    // Handle 401 error (Authentication fails, throw error in Spring Filter)
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        ErrorCode errorCode;

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || authHeader.trim().isEmpty()) {
            errorCode = ErrorCode.MISSING_TOKEN;
        }
        else if (authException.getMessage() != null && authException.getMessage().toLowerCase().contains("expired")) {
            errorCode = ErrorCode.TOKEN_EXPIRED;
        }
        else {
            errorCode = ErrorCode.INVALID_TOKEN;
        }

        ResponseUtils.sendErrorResponse(request, response, objectMapper, errorCode);
    }
}