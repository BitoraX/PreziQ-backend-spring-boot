package com.bitorax.priziq.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.context.event.EventListener;

import java.util.Objects;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebSocketEventListener {
    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.wrap(event.getMessage());
        String clientSessionId = headerAccessor.getSessionId();

        // Save clientSessionId to sessionAttributes (use WebSocketExceptionHandler)
        Objects.requireNonNull(headerAccessor.getSessionAttributes()).put("clientSessionId", clientSessionId);

        assert clientSessionId != null;
        messagingTemplate.convertAndSendToUser(clientSessionId, "/private/sessionId", clientSessionId);
        log.info("Client connected with clientSessionId: {}", clientSessionId);
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.wrap(event.getMessage());
        String clientSessionId = headerAccessor.getSessionId();
        if (clientSessionId != null) {
            log.info("Client disconnected with clientSessionId: {}", clientSessionId);
        }
    }
}