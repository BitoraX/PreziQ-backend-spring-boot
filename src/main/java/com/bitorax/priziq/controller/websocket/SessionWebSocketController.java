package com.bitorax.priziq.controller.websocket;

import com.bitorax.priziq.dto.request.session.session_participant.CreateSessionParticipantRequest;
import com.bitorax.priziq.dto.response.session.SessionParticipantResponse;
import com.bitorax.priziq.exception.ApplicationException;
import com.bitorax.priziq.exception.ErrorCode;
import com.bitorax.priziq.service.SessionParticipantService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class SessionWebSocketController {
    SessionParticipantService sessionParticipantService;
    SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/session/join")
    public void handleJoinSession(@Valid @Payload CreateSessionParticipantRequest request, SimpMessageHeaderAccessor headerAccessor) {
        List<SessionParticipantResponse> responses = sessionParticipantService.joinSession(request);

        if (responses.isEmpty()) {
            throw new ApplicationException(ErrorCode.SESSION_NOT_FOUND);
        }

        String destination = "/public/session/" + responses.getFirst().getSession().getSessionCode() + "/participants";
        messagingTemplate.convertAndSend(destination, responses);
    }
}