package com.bitorax.priziq.controller.websocket;

import com.bitorax.priziq.dto.request.session.EndSessionRequest;
import com.bitorax.priziq.dto.request.session.activity_submission.CreateActivitySubmissionRequest;
import com.bitorax.priziq.dto.request.session.session_participant.GetParticipantsRequest;
import com.bitorax.priziq.dto.request.session.session_participant.JoinSessionRequest;
import com.bitorax.priziq.dto.request.session.session_participant.LeaveSessionRequest;
import com.bitorax.priziq.dto.response.common.ApiResponse;
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

import static com.bitorax.priziq.utils.MetaUtils.buildWebSocketMetaInfo;

@Controller
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class SessionWebSocketController {
    SessionParticipantService sessionParticipantService;
    SessionService sessionService;
    ActivitySubmissionService activitySubmissionService;
    SimpMessagingTemplate messagingTemplate;

    // Utility method to create ApiResponse with sessionCode for message formatting
    private <T> ApiResponse<T> createApiResponse(String message, T data, String sessionCode, SimpMessageHeaderAccessor headerAccessor) {
        return ApiResponse.<T>builder()
                .message(String.format(message, sessionCode))
                .data(data)
                .meta(buildWebSocketMetaInfo(headerAccessor))
                .build();
    }

    @MessageMapping("/session/join")
    public void handleJoinSession(@Valid @Payload JoinSessionRequest request, SimpMessageHeaderAccessor headerAccessor) {
        String clientSessionId = headerAccessor.getSessionId();
        if (clientSessionId == null) {
            throw new ApplicationException(ErrorCode.CLIENT_SESSION_ID_NOT_FOUND);
        }

        List<SessionParticipantResponse> responses = sessionParticipantService.joinSession(request, clientSessionId);

        ApiResponse<List<SessionParticipantResponse>> apiResponse = createApiResponse(
                "A participant successfully joined session with code: %s",
                        responses, request.getSessionCode(), headerAccessor);

        String destination = "/public/session/" + request.getSessionCode() + "/participants";
        messagingTemplate.convertAndSend(destination, apiResponse);
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

        ApiResponse<List<SessionParticipantResponse>> apiResponse = createApiResponse(
                "A participant successfully left session with code: %s",
                        responses, request.getSessionCode(), headerAccessor);

        String destination = "/public/session/" + request.getSessionCode() + "/participants";
        messagingTemplate.convertAndSend(destination, apiResponse);
    }

    @MessageMapping("/session/participants")
    public void handleGetParticipants(@Valid @Payload GetParticipantsRequest request, SimpMessageHeaderAccessor headerAccessor) {
        String websocketSessionId = headerAccessor.getSessionId();
        if (websocketSessionId == null) {
            throw new ApplicationException(ErrorCode.CLIENT_SESSION_ID_NOT_FOUND);
        }

        List<SessionParticipantResponse> participants = sessionParticipantService.findParticipantsBySessionCode(request);

        ApiResponse<List<SessionParticipantResponse>> apiResponse = createApiResponse(
                "List of participants retrieved for session with code: %s",
                        participants, request.getSessionCode(), headerAccessor);

        String destination = "/public/session/" + request.getSessionCode() + "/participants";
        messagingTemplate.convertAndSend(destination, apiResponse);
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

        ApiResponse<List<SessionParticipantResponse>> apiResponse = createApiResponse(
                "Activity submission processed and scores updated for session with code: %s",
                        responses, responses.getFirst().getSession().getSessionCode(), headerAccessor);

        // Broadcast updated participants list
        String destination = "/public/session/" + responses.getFirst().getSession().getSessionCode() + "/participants";
        messagingTemplate.convertAndSend(destination, apiResponse);
    }

    @MessageMapping("/session/complete")
    public void handleEndSession(@Valid @Payload EndSessionRequest request, SimpMessageHeaderAccessor headerAccessor) {
        String websocketSessionId = headerAccessor.getSessionId();
        if (websocketSessionId == null) {
            throw new ApplicationException(ErrorCode.CLIENT_SESSION_ID_NOT_FOUND);
        }

        // End session
        SessionSummaryResponse endSessionResponse = sessionService.endSession(request);

        ApiResponse<SessionSummaryResponse> endApiResponse = createApiResponse(
                "Session with code: %s has been successfully ended",
                        endSessionResponse, endSessionResponse.getSessionCode(), headerAccessor);

        String endDestination = "/public/session/" + endSessionResponse.getSessionCode() + "/end";
        messagingTemplate.convertAndSend(endDestination, endApiResponse);

        // Calculate summary information
        List<EndSessionSummaryResponse> summaries = sessionService.calculateSessionSummary(request.getSessionId());

        ApiResponse<List<EndSessionSummaryResponse>> summaryApiResponse = createApiResponse(
                "Final summary generated for session with code: %s",
                        summaries, endSessionResponse.getSessionCode(), headerAccessor);

        String summaryDestination = "/public/session/" + endSessionResponse.getSessionCode() + "/summary";
        messagingTemplate.convertAndSend(summaryDestination, summaryApiResponse);
    }
}