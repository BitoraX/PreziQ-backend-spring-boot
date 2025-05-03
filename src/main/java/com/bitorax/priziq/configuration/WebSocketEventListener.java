package com.bitorax.priziq.configuration;

import com.bitorax.priziq.dto.response.session.SessionParticipantResponse;
import com.bitorax.priziq.repository.SessionParticipantRepository;
import com.bitorax.priziq.service.SessionParticipantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.context.event.EventListener;

import java.util.List;
import java.util.Objects;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebSocketEventListener {
    private final SimpMessagingTemplate messagingTemplate;
    private final SessionParticipantService sessionParticipantService;
    private final SessionParticipantRepository sessionParticipantRepository;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.wrap(event.getMessage());
        String clientSessionId = headerAccessor.getSessionId();

        // Save clientSessionId to sessionAttributes
        Objects.requireNonNull(headerAccessor.getSessionAttributes()).put("clientSessionId", clientSessionId);

        if (clientSessionId != null) {
            log.info("Client connected with clientSessionId: {}", clientSessionId);
        } else {
            log.warn("clientSessionId is null on connect");
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.wrap(event.getMessage());
        String clientSessionId = headerAccessor.getSessionId();
        if (clientSessionId == null) {
            log.warn("clientSessionId is null on disconnect");
            return;
        }

        log.info("Client disconnected with clientSessionId: {}", clientSessionId);

        // Find and remove SessionParticipant by clientSessionId
        sessionParticipantRepository.findByClientSessionId(clientSessionId)
                .ifPresent(participant -> {
                    String sessionCode = participant.getSession().getSessionCode();
                    sessionParticipantRepository.delete(participant);

                    // Broadcast updated participants list
                    List<SessionParticipantResponse> responses = sessionParticipantService.findParticipantsBySessionCode(sessionCode);
                    String destination = "/public/session/" + sessionCode + "/participants";
                    messagingTemplate.convertAndSend(destination, responses);
                });
    }
}