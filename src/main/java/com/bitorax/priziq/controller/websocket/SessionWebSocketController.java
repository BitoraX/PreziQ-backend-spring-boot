package com.bitorax.priziq.controller.websocket;

import com.bitorax.priziq.dto.request.session.EndSessionRequest;
import com.bitorax.priziq.dto.request.session.NextActivityRequest;
import com.bitorax.priziq.dto.request.session.StartSessionRequest;
import com.bitorax.priziq.dto.request.session.activity_submission.CreateActivitySubmissionRequest;
import com.bitorax.priziq.dto.request.session.session_participant.GetParticipantsRequest;
import com.bitorax.priziq.dto.request.session.session_participant.JoinSessionRequest;
import com.bitorax.priziq.dto.request.session.session_participant.LeaveSessionRequest;
import com.bitorax.priziq.dto.response.achievement.AchievementUpdateResponse;
import com.bitorax.priziq.dto.response.activity.ActivitySummaryResponse;
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
import java.util.Map;
import java.util.stream.Collectors;

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

        List<SessionParticipantSummaryResponse> responses = sessionParticipantService.joinSession(request, clientSessionId);

        ApiResponse<List<SessionParticipantSummaryResponse>> apiResponse = createApiResponse(
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

        List<SessionParticipantSummaryResponse> responses = sessionParticipantService.leaveSession(request, clientSessionId);

        if (responses.isEmpty()) {
            throw new ApplicationException(ErrorCode.SESSION_NOT_FOUND);
        }

        ApiResponse<List<SessionParticipantSummaryResponse>> apiResponse = createApiResponse(
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

        List<SessionParticipantSummaryResponse> participants = sessionParticipantService.findParticipantsBySessionCode(request);

        ApiResponse<List<SessionParticipantSummaryResponse>> apiResponse = createApiResponse(
                "List of participants retrieved for session with code: %s",
                        participants, request.getSessionCode(), headerAccessor);

        String destination = "/public/session/" + request.getSessionCode() + "/participants";
        messagingTemplate.convertAndSend(destination, apiResponse);
    }

    @MessageMapping("/session/start")
    public void handleStartSession(@Valid @Payload StartSessionRequest request, SimpMessageHeaderAccessor headerAccessor) {
        String websocketSessionId = headerAccessor.getSessionId();
        if (websocketSessionId == null) {
            throw new ApplicationException(ErrorCode.CLIENT_SESSION_ID_NOT_FOUND);
        }

        SessionSummaryResponse sessionResponse = sessionService.startSession(request, websocketSessionId);

        String sessionCode = sessionResponse.getSessionCode();
        ApiResponse<SessionSummaryResponse> apiResponse = createApiResponse(
                "Session with code %s has started",
                sessionResponse, sessionCode, headerAccessor);

        String destination = "/public/session/" + sessionCode + "/start";
        messagingTemplate.convertAndSend(destination, apiResponse);
    }

    @MessageMapping("/session/submit")
    public void handleSubmitActivity(@Valid @Payload CreateActivitySubmissionRequest request, SimpMessageHeaderAccessor headerAccessor) {
        String websocketSessionId = headerAccessor.getSessionId();
        if (websocketSessionId == null) {
            throw new ApplicationException(ErrorCode.CLIENT_SESSION_ID_NOT_FOUND);
        }

        // Create ActivitySubmission and get responseScore
        ActivitySubmissionSummaryResponse submissionResponse = activitySubmissionService.createActivitySubmission(request, websocketSessionId);

        // Update realtimeScore and realtimeRanking
        List<SessionParticipantSummaryResponse> responses = sessionParticipantService.updateRealtimeScoreAndRanking(
                request.getSessionId(),
                websocketSessionId,
                submissionResponse.getResponseScore()
        );

        if (responses.isEmpty()) {
            throw new ApplicationException(ErrorCode.SESSION_NOT_FOUND);
        }

        String sessionCode = sessionService.findSessionCodeBySessionId(request.getSessionId());
        ApiResponse<List<SessionParticipantSummaryResponse>> apiResponse = createApiResponse(
                "Activity submission processed and scores updated for session with code: %s",
                responses, sessionCode, headerAccessor);

        // Broadcast updated participants list
        String destination = "/public/session/" + sessionCode + "/participants";
        messagingTemplate.convertAndSend(destination, apiResponse);
    }

    @MessageMapping("/session/nextActivity")
    public void handleNextActivity(@Valid @Payload NextActivityRequest request, SimpMessageHeaderAccessor headerAccessor) {
        String websocketSessionId = headerAccessor.getSessionId();
        if (websocketSessionId == null) {
            throw new ApplicationException(ErrorCode.CLIENT_SESSION_ID_NOT_FOUND);
        }

        ActivitySummaryResponse activityResponse = sessionService.nextActivity(request, websocketSessionId);

        String sessionCode = sessionService.findSessionCodeBySessionId(request.getSessionId());
        ApiResponse<ActivitySummaryResponse> apiResponse = createApiResponse(
                activityResponse != null ? "Moved to next activity in session with code %s" : "No more activities in session with code %s",
                activityResponse, sessionCode, headerAccessor);

        String destination = "/public/session/" + sessionCode + "/nextActivity";
        messagingTemplate.convertAndSend(destination, apiResponse);
    }

    @MessageMapping("/session/complete")
    public void handleEndSession(@Valid @Payload EndSessionRequest request, SimpMessageHeaderAccessor headerAccessor) {
        String websocketSessionId = headerAccessor.getSessionId();
        if (websocketSessionId == null) {
            throw new ApplicationException(ErrorCode.CLIENT_SESSION_ID_NOT_FOUND);
        }

        // End session
        SessionEndResultResponse endSessionResult = sessionService.endSession(request, websocketSessionId);
        SessionSummaryResponse endSessionResponse = endSessionResult.getSessionSummary();

        ApiResponse<SessionSummaryResponse> endApiResponse = createApiResponse(
                "Session with code: %s has been successfully ended",
                        endSessionResponse, endSessionResponse.getSessionCode(), headerAccessor);

        String endDestination = "/public/session/" + endSessionResponse.getSessionCode() + "/end";
        messagingTemplate.convertAndSend(endDestination, endApiResponse);

        // Calculate summary information
        List<SessionEndSummaryResponse> summaries = sessionService.calculateSessionSummary(request.getSessionId());

        ApiResponse<List<SessionEndSummaryResponse>> summaryApiResponse = createApiResponse(
                "Final summary generated for session with code: %s",
                        summaries, endSessionResponse.getSessionCode(), headerAccessor);

        String summaryDestination = "/public/session/" + endSessionResponse.getSessionCode() + "/summary";
        messagingTemplate.convertAndSend(summaryDestination, summaryApiResponse);

        // Group achievement updates by userId and send as a list
        List<AchievementUpdateResponse> achievementUpdates = endSessionResult.getAchievementUpdates();
        if (achievementUpdates != null && !achievementUpdates.isEmpty()) {
            Map<String, List<AchievementUpdateResponse>> updatesByUser = achievementUpdates.stream()
                    .collect(Collectors.groupingBy(AchievementUpdateResponse::getUserId));

            // Send list of updates to each user
            updatesByUser.forEach((userId, updates) -> {
                ApiResponse<List<AchievementUpdateResponse>> achievementApiResponse = createApiResponse(
                        "Achievement updates for user in session with code: %s",
                        updates, endSessionResponse.getSessionCode(), headerAccessor);

                messagingTemplate.convertAndSendToUser(userId, "/private/achievement", achievementApiResponse);
            });
        }
    }
}