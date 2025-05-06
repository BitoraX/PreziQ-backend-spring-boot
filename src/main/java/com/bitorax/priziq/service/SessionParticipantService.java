package com.bitorax.priziq.service;

import com.bitorax.priziq.dto.request.session.session_participant.GetParticipantsRequest;
import com.bitorax.priziq.dto.request.session.session_participant.JoinSessionRequest;
import com.bitorax.priziq.dto.request.session.session_participant.LeaveSessionRequest;
import com.bitorax.priziq.dto.response.session.SessionParticipantSummaryResponse;

import java.util.List;

public interface SessionParticipantService {
    List<SessionParticipantSummaryResponse> joinSession(JoinSessionRequest request, String websocketSessionId, String stompClientId);

    List<SessionParticipantSummaryResponse> leaveSession(LeaveSessionRequest request, String websocketSessionId);

    List<SessionParticipantSummaryResponse> findParticipantsBySessionCode(GetParticipantsRequest request);

    List<SessionParticipantSummaryResponse> updateRealtimeScoreAndRanking(String sessionId, String websocketSessionId, int responseScore);
}