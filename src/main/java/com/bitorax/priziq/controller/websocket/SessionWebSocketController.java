package com.bitorax.priziq.controller.websocket;

import com.bitorax.priziq.dto.request.session.EndSessionRequest;
import com.bitorax.priziq.dto.request.session.activity_submission.CreateActivitySubmissionRequest;
import com.bitorax.priziq.dto.request.session.session_participant.JoinSessionRequest;
import com.bitorax.priziq.dto.request.session.session_participant.LeaveSessionRequest;
import com.bitorax.priziq.dto.response.session.*;
import com.bitorax.priziq.exception.ApplicationException;
import com.bitorax.priziq.exception.ErrorCode;
import com.bitorax.priziq.service.ActivitySubmissionService;
import com.bitorax.priziq.service.SessionParticipantService;
import com.bitorax.priziq.service.SessionService;
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
    SessionService sessionService;
    ActivitySubmissionService activitySubmissionService;
    SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/session/join")
    public void handleJoinSession(@Valid @Payload JoinSessionRequest request, SimpMessageHeaderAccessor headerAccessor) {
        String clientSessionId = headerAccessor.getSessionId();
        if (clientSessionId == null) {
            throw new ApplicationException(ErrorCode.CLIENT_SESSION_ID_NOT_FOUND);
        }

        List<SessionParticipantResponse> responses = sessionParticipantService.joinSession(request, clientSessionId);

        String destination = "/public/session/" + request.getSessionCode() + "/participants";
        messagingTemplate.convertAndSend(destination, responses);
    }

    @MessageMapping("/session/leave")
    public void handleLeaveSession(@Valid @Payload LeaveSessionRequest request, SimpMessageHeaderAccessor headerAccessor) {
        String clientSessionId = headerAccessor.getSessionId();
        if (clientSessionId == null) {
            throw new ApplicationException(ErrorCode.CLIENT_SESSION_ID_NOT_FOUND);
        }

        List<SessionParticipantResponse> responses = sessionParticipantService.leaveSession(request, clientSessionId);

        if (responses.isEmpty()) {
            throw new ApplicationException(ErrorCode.SESSION_NOT_FOUND);
        }

        String destination = "/public/session/" + request.getSessionCode() + "/participants";
        messagingTemplate.convertAndSend(destination, responses);
    }

    @MessageMapping("/session/submit")
    public void handleSubmitActivity(@Valid @Payload CreateActivitySubmissionRequest request, SimpMessageHeaderAccessor headerAccessor) {
        String websocketSessionId = headerAccessor.getSessionId();
        if (websocketSessionId == null) {
            throw new ApplicationException(ErrorCode.CLIENT_SESSION_ID_NOT_FOUND);
        }

        // Create ActivitySubmission and get responseScore
        ActivitySubmissionResponse submissionResponse = activitySubmissionService.createActivitySubmission(request, websocketSessionId);

        // Update realtimeScore and realtimeRanking
        List<SessionParticipantResponse> responses = sessionParticipantService.updateRealtimeScoreAndRanking(
                request.getSessionId(),
                websocketSessionId,
                submissionResponse.getResponseScore()
        );

        if (responses.isEmpty()) {
            throw new ApplicationException(ErrorCode.SESSION_NOT_FOUND);
        }

        // Broadcast updated participants list
        String destination = "/public/session/" + responses.getFirst().getSession().getSessionCode() + "/participants";
        messagingTemplate.convertAndSend(destination, responses);
    }

    @MessageMapping("/session/complete")
    public void handleEndSession(@Valid @Payload EndSessionRequest request, SimpMessageHeaderAccessor headerAccessor) {
        String websocketSessionId = headerAccessor.getSessionId();
        if (websocketSessionId == null) {
            throw new ApplicationException(ErrorCode.CLIENT_SESSION_ID_NOT_FOUND);
        }

        // End session
        SessionSummaryResponse endSessionResponse = sessionService.endSession(request);
        String endDestination = "/public/session/" + endSessionResponse.getSessionCode() + "/end";
        messagingTemplate.convertAndSend(endDestination, endSessionResponse);

        // Calculate summary information
        List<EndSessionSummaryResponse> summaries = sessionService.calculateSessionSummary(request.getSessionId());
        String summaryDestination = "/public/session/" + endSessionResponse.getSessionCode() + "/summary";
        messagingTemplate.convertAndSend(summaryDestination, summaries);
    }
}