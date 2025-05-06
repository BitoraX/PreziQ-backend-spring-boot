package com.bitorax.priziq.configuration;

import com.bitorax.priziq.dto.request.session.session_participant.GetParticipantsRequest;
import com.bitorax.priziq.dto.response.common.ApiResponse;
import com.bitorax.priziq.dto.response.session.SessionParticipantSummaryResponse;
import com.bitorax.priziq.repository.SessionParticipantRepository;
import com.bitorax.priziq.service.SessionParticipantService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.context.event.EventListener;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class WebSocketEventListener {
    SimpMessagingTemplate messagingTemplate;
    SessionParticipantService sessionParticipantService;
    SessionParticipantRepository sessionParticipantRepository;

    private record CustomPrincipal(String name) implements Principal {
        @Override
        public String getName() {
            return name;
        }
    }

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.wrap(event.getMessage());
        String websocketSessionId = headerAccessor.getSessionId();
        String clientUuid = headerAccessor.getFirstNativeHeader("clientUuid");

        // Validate clientUuid
        if (clientUuid == null || clientUuid.trim().isEmpty()) {
            clientUuid = websocketSessionId;
        }

        headerAccessor.setUser(new CustomPrincipal(clientUuid));

        // Save clientUuid to sessionAttributes
        Map<String, Object> sessionAttributes = Objects.requireNonNull(headerAccessor.getSessionAttributes());
        sessionAttributes.put("clientUuid", clientUuid);
        sessionAttributes.put("websocketSessionId", websocketSessionId);

        log.info("Client connected with clientUuid: {} (websocketSessionId: {})", clientUuid, websocketSessionId);

        // Send confirmation back to client
        Map<String, String> responseData = new HashMap<>();
        responseData.put("clientUuid", clientUuid);
        ApiResponse<Map<String, String>> response = ApiResponse.<Map<String, String>>builder()
                .success(true)
                .data(responseData)
                .message("Connected successfully")
                .build();

        if (clientUuid != null) {
            messagingTemplate.convertAndSendToUser(clientUuid, "/private/connect", response);
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.wrap(event.getMessage());
        String websocketSessionId = headerAccessor.getSessionId();

        if (websocketSessionId == null) {
            log.warn("websocketSessionId is null on disconnect");
            return;
        }

        log.info("Client disconnected with websocketSessionId: {}", websocketSessionId);

        // Find and remove SessionParticipant by websocketSessionId
        sessionParticipantRepository.findByWebsocketSessionId(websocketSessionId)
                .ifPresent(participant -> {
                    String sessionCode = participant.getSession().getSessionCode();
                    sessionParticipantRepository.delete(participant);

                    // Broadcast updated participants list
                    List<SessionParticipantSummaryResponse> responses = sessionParticipantService
                            .findParticipantsBySessionCode(
                                    GetParticipantsRequest.builder()
                                            .sessionCode(sessionCode)
                                            .build()
                            );

                    String destination = "/public/session/" + sessionCode + "/participants";
                    messagingTemplate.convertAndSend(destination, responses);
                });
    }
}