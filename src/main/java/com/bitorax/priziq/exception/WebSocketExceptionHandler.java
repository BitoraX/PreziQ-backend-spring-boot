package com.bitorax.priziq.exception;

import com.bitorax.priziq.dto.response.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class WebSocketExceptionHandler {

    private final SimpMessagingTemplate messagingTemplate;

    private ApiResponse<?> buildErrorResponse(ErrorCode errorCode, Optional<String> customMessage, List<ErrorDetail> errorDetails) {
        List<ErrorDetail> details = (errorDetails == null || errorDetails.isEmpty())
                ? List.of(ErrorDetail.builder()
                .code(errorCode.getCode())
                .message(customMessage.orElse(errorCode.getMessage()))
                .build())
                : errorDetails;

        return ApiResponse.builder()
                .success(false)
                .errors(details)
                .meta(null)
                .build();
    }

    @MessageExceptionHandler(ApplicationException.class)
    public void handleApplicationException(ApplicationException ex, SimpMessageHeaderAccessor headerAccessor) {
        Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
        String clientUuid = getClientUuid(sessionAttributes);

        if (clientUuid == null) {
            log.warn("Cannot send error: clientUuid is null for ApplicationException: {}", ex.getMessage());
            return;
        }

        log.error("WebSocket ApplicationException: {}", ex.getMessage(), ex);
        String message = ex.getCustomMessage() != null ? ex.getCustomMessage() : ex.getErrorCode().getMessage();
        ApiResponse<?> response = buildErrorResponse(ex.getErrorCode(), Optional.of(message), null);

        sendErrorToClient(clientUuid, response);
    }

    @MessageExceptionHandler(MethodArgumentNotValidException.class)
    public void handleValidationException(MethodArgumentNotValidException ex, SimpMessageHeaderAccessor headerAccessor) {
        Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
        String clientUuid = getClientUuid(sessionAttributes);

        if (clientUuid == null) {
            log.warn("Cannot send error: clientUuid is null for MethodArgumentNotValidException");
            return;
        }

        log.error("WebSocket ValidationException: {}", ex.getMessage(), ex);
        List<ErrorDetail> errorDetails = ErrorDetailMapper.mapValidationErrors(ex);
        ApiResponse<?> response = buildErrorResponse(ErrorCode.INVALID_REQUEST_DATA, Optional.empty(), errorDetails);

        sendErrorToClient(clientUuid, response);
    }

    @MessageExceptionHandler(Exception.class)
    public void handleGenericException(Exception ex, SimpMessageHeaderAccessor headerAccessor) {
        Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
        String clientUuid = getClientUuid(sessionAttributes);

        if (clientUuid == null) {
            log.warn("Cannot send error: clientUuid is null for Exception: {}", ex.getMessage());
            return;
        }

        log.error("WebSocket Unexpected error: {}", ex.getMessage(), ex);
        ApiResponse<?> response = buildErrorResponse(ErrorCode.UNCATEGORIZED_EXCEPTION, Optional.of("An unexpected error occurred"), null);

        sendErrorToClient(clientUuid, response);
    }

    private String getClientUuid(Map<String, Object> sessionAttributes) {
        if (sessionAttributes == null) {
            log.warn("Session attributes are null");
            return null;
        }
        return (String) sessionAttributes.get("clientUuid");
    }

    private void sendErrorToClient(String clientUuid, ApiResponse<?> response) {
        log.info("Sending error to /client/{}/private/errors", clientUuid);
        String destination = "/client/" + clientUuid + "/private/errors";
        messagingTemplate.convertAndSend(destination, response);
    }
}