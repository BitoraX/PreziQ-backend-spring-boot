package com.bitorax.priziq.configuration;

import com.bitorax.priziq.dto.response.common.ApiResponse;
import com.bitorax.priziq.dto.response.common.MetaInfo;
import com.bitorax.priziq.exception.ErrorCode;
import com.bitorax.priziq.exception.ErrorDetail;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    // Handle 401 error (Authentication fail, throw error in Spring Filter)
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

        MetaInfo meta = MetaInfo.builder()
                .timestamp(Instant.now().toString())
                .instance(request.getRequestURI())
                .build();

        ErrorDetail errorDetail = ErrorDetail.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();

        ApiResponse<Object> apiResponse = ApiResponse.builder()
                .success(false)
                .errors(List.of(errorDetail))
                .meta(meta)
                .build();

        ObjectMapper objectMapper = new ObjectMapper(); // Tool convert JAVA object to JSON

        // ObjectMapper understands how to handle modern Java time types (like Instant, used in MetaInfo)
        objectMapper.registerModule(new JavaTimeModule());

        // Turn off Jackson's default mode, ensuring times (as Instant) are written as ISO-8601 strings
        // (e.g. "2025-04-21T10:00:00Z") instead of timestamp numbers (like 1742544000000)
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        response.setStatus(errorCode.getStatusCode().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // Convert object apiResponse to JSON string and write that string to the response
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));

        // Push all response data (JSON, status code, headers) to the client immediately
        response.flushBuffer();
    }
}