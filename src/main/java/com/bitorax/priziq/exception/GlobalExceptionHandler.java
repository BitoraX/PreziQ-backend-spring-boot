package com.bitorax.priziq.exception;

import com.bitorax.priziq.dto.response.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private ResponseEntity<ApiResponse<String>> buildErrorResponse(ErrorCode errorCode, Optional<String> customMessage,
            List<ErrorDetail> errorDetails, String path) {
        ApiResponse<String> apiResponse = ApiResponse.<String>builder()
                .code(errorCode.getCode())
                .statusCode(errorCode.getStatusCode().value())
                .message(customMessage.orElse(errorCode.getMessage()))
                .errors(errorDetails)
                .timestamp(Instant.now())
                .path(path)
                .build();
        return ResponseEntity.status(errorCode.getStatusCode()).body(apiResponse);
    }

    @ExceptionHandler(value = Exception.class)
    ResponseEntity<ApiResponse<String>> handleRuntimeException(RuntimeException exception, HttpServletRequest request) {
        log.error("Exception: ", exception);
        return buildErrorResponse(ErrorCode.UNCATEGORIZED_EXCEPTION, Optional.empty(), null, request.getRequestURI());
    }

    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ApiResponse<String>> handleAppException(AppException exception, HttpServletRequest request) {
        String message = exception.getCustomMessage() != null ? exception.getCustomMessage()
                : exception.getErrorCode().getMessage();
        return buildErrorResponse(exception.getErrorCode(), Optional.of(message), null, request.getRequestURI());
    }

    @ExceptionHandler(value = AccessDeniedException.class)
    ResponseEntity<ApiResponse<String>> handleAccessDeniedException(AccessDeniedException exception, HttpServletRequest request) {
        return buildErrorResponse(ErrorCode.UNAUTHORIZED, Optional.empty(), null, request.getRequestURI());
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse<String>> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception,
            HttpServletRequest request) {
        List<ErrorDetail> errors = exception.getBindingResult().getAllErrors().stream()
                .map(fieldError -> {
                    String defaultMessage = fieldError.getDefaultMessage();
                    ErrorCode errorCode;

                    try {
                        errorCode = ErrorCode.valueOf(defaultMessage);
                    } catch (IllegalArgumentException e) {
                        errorCode = ErrorCode.INVALID_KEY;
                        log.error("Invalid error code: {}", defaultMessage);
                    }

                    return ErrorDetail.builder()
                            .code(errorCode.getCode())
                            .message(errorCode.getMessage())
                            .build();
                })
                .toList();

        return buildErrorResponse(ErrorCode.INVALID_REQUEST_DATA, Optional.empty(), errors, request.getRequestURI());
    }
}
