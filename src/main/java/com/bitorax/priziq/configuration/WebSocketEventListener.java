package com.bitorax.priziq.configuration;

import com.bitorax.priziq.dto.request.session.session_participant.LeaveSessionRequest;
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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.context.event.EventListener;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.bitorax.priziq.utils.MetaUtils.buildWebSocketMetaInfo;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class WebSocketEventListener {
    SimpMessagingTemplate messagingTemplate;
    SessionParticipantService sessionParticipantService;
    SessionParticipantRepository sessionParticipantRepository;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.wrap(event.getMessage());

        String websocketSessionId = headerAccessor.getSessionId();
        Principal principal = headerAccessor.getUser();
        String stompClientId = (principal != null) ? principal.getName() : null;

        if (stompClientId == null) {
            log.warn("StompClientId is null for websocketSessionId: {}", websocketSessionId);
            return;
        }

        Map<String, Object> sessionAttributes = Objects.requireNonNull(headerAccessor.getSessionAttributes());
        sessionAttributes.put("websocketSessionId", websocketSessionId);
        sessionAttributes.put("stompClientId", stompClientId);
    }

    @EventListener
    @Async("asyncTaskExecutor")
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.wrap(event.getMessage());
        String websocketSessionId = headerAccessor.getSessionId();

        if (websocketSessionId == null) {
            log.warn("websocketSessionId is null on disconnect");
            return;
        }

        log.info("Client disconnected with websocketSessionId: {}", websocketSessionId);

        sessionParticipantRepository.findByWebsocketSessionId(websocketSessionId)
                .ifPresent(participant -> {
                    String sessionCode = participant.getSession().getSessionCode();
                    LeaveSessionRequest leaveRequest = LeaveSessionRequest.builder()
                            .sessionCode(sessionCode)
                            .build();

                    List<SessionParticipantSummaryResponse> participants = sessionParticipantService.leaveSession(leaveRequest, websocketSessionId);

                    ApiResponse<List<SessionParticipantSummaryResponse>> apiResponse = ApiResponse.<List<SessionParticipantSummaryResponse>>builder()
                            .message(String.format("A participant left session with code: %s due to disconnect", sessionCode))
                            .data(participants)
                            .meta(buildWebSocketMetaInfo(headerAccessor))
                            .build();

                    String destination = "/public/session/" + sessionCode + "/participants";
                    messagingTemplate.convertAndSend(destination, apiResponse);
                });
    }
}