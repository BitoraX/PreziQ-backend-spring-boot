package com.bitorax.priziq.configuration;

import com.bitorax.priziq.constant.SessionStatus;
import com.bitorax.priziq.domain.session.SessionParticipant;
import com.bitorax.priziq.dto.request.session.EndSessionRequest;
import com.bitorax.priziq.dto.request.session.session_participant.LeaveSessionRequest;
import com.bitorax.priziq.dto.response.common.ApiResponse;
import com.bitorax.priziq.dto.response.session.SessionParticipantSummaryResponse;
import com.bitorax.priziq.mapper.SessionParticipantMapper;
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
import java.util.stream.Collectors;

import static com.bitorax.priziq.utils.MetaUtils.buildWebSocketMetaInfo;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class    WebSocketEventListener {
    SimpMessagingTemplate messagingTemplate;
    SessionParticipantRepository sessionParticipantRepository;
    SessionParticipantMapper sessionParticipantMapper;

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

                    if (isHost) {
                        if (sessionStatus == SessionStatus.PENDING) {
                            // All participants leave the session
                            List<SessionParticipant> participants = sessionParticipantRepository.findBySession_SessionCode(sessionCode);
                            for (SessionParticipant p : participants) {
                                LeaveSessionRequest leaveRequest = LeaveSessionRequest.builder()
                                        .sessionCode(sessionCode)
                                        .build();
                                messagingTemplate.convertAndSend("/server/session/leave", leaveRequest, headerAccessor.getMessageHeaders());
                            }
                        } else if (sessionStatus == SessionStatus.STARTED) {
                            // Only host triggers session complete event
                            EndSessionRequest endSessionRequest = EndSessionRequest.builder()
                                    .sessionId(participant.getSession().getSessionId())
                                    .build();
                            messagingTemplate.convertAndSend("/server/session/complete", endSessionRequest, headerAccessor.getMessageHeaders());
                        }
                    } else {
                        // Non-host participant disconnect
                        if (sessionStatus == SessionStatus.PENDING) {
                            // Leave session and delete data
                            LeaveSessionRequest leaveRequest = LeaveSessionRequest.builder()
                                    .sessionCode(sessionCode)
                                    .build();
                            messagingTemplate.convertAndSend("/server/session/leave", leaveRequest, headerAccessor.getMessageHeaders());
                        } else if (sessionStatus == SessionStatus.STARTED) {
                            // Mark participant as inactive
                            participant.setIsConnected(false);
                            sessionParticipantRepository.save(participant);

                            // Send updated participant list (only active participants)
                            List<SessionParticipantSummaryResponse> activeParticipants = sessionParticipantRepository
                                    .findBySession_SessionCodeAndIsConnectedTrue(sessionCode)
                                    .stream()
                                    .map(sessionParticipantMapper::sessionParticipantToSummaryResponse)
                                    .collect(Collectors.toList());

                            ApiResponse<List<SessionParticipantSummaryResponse>> apiResponse = ApiResponse.<List<SessionParticipantSummaryResponse>>builder()
                                    .message(String.format("Participant disconnected from session with code: %s", sessionCode))
                                    .data(activeParticipants)
                                    .meta(buildWebSocketMetaInfo(headerAccessor))
                                    .build();

                            String destination = "/public/session/" + sessionCode + "/participants";
                            messagingTemplate.convertAndSend(destination, apiResponse);
                        }
                    }
                });
    }
}