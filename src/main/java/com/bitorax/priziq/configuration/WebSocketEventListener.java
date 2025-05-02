package com.bitorax.priziq.configuration;

import com.bitorax.priziq.dto.response.common.ConnectionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.context.event.EventListener;

import java.util.Objects;

@Configuration
@Slf4j
public class WebSocketEventListener {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketEventListener(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.wrap(event.getMessage());
        String clientSessionId = headerAccessor.getSessionId();
        Objects.requireNonNull(headerAccessor.getSessionAttributes()).put("clientSessionId", clientSessionId);
        log.info("Client connected with clientSessionId: {}", clientSessionId);

        assert clientSessionId != null;
        messagingTemplate.convertAndSendToUser(
                clientSessionId,
                "/queue/connection",
                new ConnectionResponse(clientSessionId, "Connected successfully")
        );
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.wrap(event.getMessage());
        String clientSessionId = (String) Objects.requireNonNull(headerAccessor.getSessionAttributes()).get("clientSessionId");
        if (clientSessionId != null) {
            log.info("Client disconnected with clientSessionId: {}", clientSessionId);
        }
    }
}