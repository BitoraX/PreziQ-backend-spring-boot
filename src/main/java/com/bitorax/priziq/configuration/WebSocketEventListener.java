package com.bitorax.priziq.configuration;

import com.bitorax.priziq.constant.SessionStatus;
import com.bitorax.priziq.domain.session.SessionParticipant;
import com.bitorax.priziq.dto.request.session.EndSessionRequest;
import com.bitorax.priziq.dto.request.session.session_participant.LeaveSessionRequest;
import com.bitorax.priziq.repository.SessionParticipantRepository;
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

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class WebSocketEventListener {
    SimpMessagingTemplate messagingTemplate;
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
                    SessionStatus sessionStatus = participant.getSession().getSessionStatus();
                    String hostUserId = participant.getSession().getHostUser().getUserId();
                    boolean isHost = participant.getUser() != null && participant.getUser().getUserId().equals(hostUserId);

                    if (isHost && (sessionStatus == SessionStatus.PENDING || sessionStatus == SessionStatus.STARTED)) {
                        // Handle host disconnection
                        if (sessionStatus == SessionStatus.PENDING) {
                            // All non-host participants leave the session
                            List<SessionParticipant> participants = sessionParticipantRepository.findBySession_SessionCode(sessionCode);
                            for (SessionParticipant p : participants) {
                                if (p.getUser() == null || !p.getUser().getUserId().equals(hostUserId)) {
                                    LeaveSessionRequest leaveRequest = LeaveSessionRequest.builder()
                                            .sessionCode(sessionCode)
                                            .build();
                                    messagingTemplate.convertAndSend("/server/session/leave", leaveRequest, headerAccessor.getMessageHeaders());
                                }
                            }
                            // Host also leaves
                            LeaveSessionRequest hostLeaveRequest = LeaveSessionRequest.builder()
                                    .sessionCode(sessionCode)
                                    .build();
                            messagingTemplate.convertAndSend("/server/session/leave", hostLeaveRequest, headerAccessor.getMessageHeaders());
                        } else { // STARTED
                            // Trigger session complete event
                            EndSessionRequest endSessionRequest = EndSessionRequest.builder()
                                    .sessionId(participant.getSession().getSessionId())
                                    .build();
                            messagingTemplate.convertAndSend("/server/session/complete", endSessionRequest, headerAccessor.getMessageHeaders());
                        }
                    } else {
                        // Handle non-host disconnection
                        LeaveSessionRequest leaveRequest = LeaveSessionRequest.builder()
                                .sessionCode(sessionCode)
                                .build();
                        messagingTemplate.convertAndSend("/server/session/leave", leaveRequest, headerAccessor.getMessageHeaders());
                    }
                });
    }
}