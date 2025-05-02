package com.bitorax.priziq.controller.websocket;

import com.bitorax.priziq.dto.request.session.session_participant.CreateSessionParticipantRequest;
import com.bitorax.priziq.dto.response.session.SessionParticipantResponse;
import com.bitorax.priziq.exception.ApplicationException;
import com.bitorax.priziq.exception.ErrorCode;
import com.bitorax.priziq.service.SessionParticipantService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class SessionWebSocketController {
    SessionParticipantService sessionParticipantService;
    SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/session/join")
    public SessionParticipantResponse handleJoinSession(CreateSessionParticipantRequest request) {
        SessionParticipantResponse response = sessionParticipantService.joinSession(request);

        if (response.getSession() == null) {
            throw new ApplicationException(ErrorCode.SESSION_NOT_FOUND);
        }

        String destination = "/topic/session/" + response.getSession().getSessionId() + "/participants";
        messagingTemplate.convertAndSend(destination, response);

        return response;
    }
}