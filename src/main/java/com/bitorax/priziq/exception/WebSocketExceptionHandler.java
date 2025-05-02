package com.bitorax.priziq.exception;

import com.bitorax.priziq.dto.response.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;

import java.util.List;
import java.util.Optional;

@ControllerAdvice(annotations = org.springframework.stereotype.Controller.class)
@Slf4j
@RequiredArgsConstructor
public class WebSocketExceptionHandler {

    private final SimpMessagingTemplate messagingTemplate;

    private ApiResponse<?> buildErrorResponse(ErrorCode errorCode, Optional<String> customMessage, List<ErrorDetail> errorDetails) {
        if (errorDetails == null || errorDetails.isEmpty()) {
            errorDetails = List.of(ErrorDetail.builder()
                    .code(errorCode.getCode())
                    .message(customMessage.orElse(errorCode.getMessage()))
                    .build());
        }
        return ApiResponse.builder()
                .success(false)
                .errors(errorDetails)
                .meta(null)
                .build();
    }

    @MessageExceptionHandler(ApplicationException.class)
    public void handleApplicationException(ApplicationException ex) {
        log.error("WebSocket ApplicationException: {}", ex.getMessage(), ex);
        String message = ex.getCustomMessage() != null ? ex.getCustomMessage() : ex.getErrorCode().getMessage();
        ApiResponse<?> response = buildErrorResponse(ex.getErrorCode(), Optional.of(message), null);
        messagingTemplate.convertAndSend("/topic/errors", response);
    }

    @MessageExceptionHandler(MethodArgumentNotValidException.class)
    public void handleValidationException(MethodArgumentNotValidException ex) {
        log.error("WebSocket ValidationException: {}", ex.getMessage(), ex);
        List<ErrorDetail> errors = ex.getBindingResult().getAllErrors().stream()
                .map(error -> {
                    ErrorCode errorCode;
                    try {
                        errorCode = ErrorCode.valueOf(error.getDefaultMessage());
                    } catch (IllegalArgumentException e) {
                        errorCode = ErrorCode.INVALID_KEY;
                    }
                    String resource = null, field = null;
                    if (error instanceof FieldError fieldError) {
                        resource = fieldError.getObjectName();
                        field = fieldError.getField();
                    }
                    return ErrorDetail.builder()
                            .resource(resource)
                            .field(field)
                            .code(errorCode.getCode())
                            .message(errorCode.getMessage())
                            .build();
                })
                .toList();
        ApiResponse<?> response = buildErrorResponse(ErrorCode.INVALID_REQUEST_DATA, Optional.empty(), errors);
        messagingTemplate.convertAndSend("/topic/errors", response);
    }

    @MessageExceptionHandler(Exception.class)
    public void handleGenericException(Exception ex) {
        log.error("WebSocket Unexpected error: {}", ex.getMessage(), ex);
        ApiResponse<?> response = buildErrorResponse(ErrorCode.UNCATEGORIZED_EXCEPTION, Optional.empty(), null);
        messagingTemplate.convertAndSend("/topic/errors", response);
    }
}