package com.bitorax.priziq.service;

import com.bitorax.priziq.dto.request.session.session_participant.GetParticipantsRequest;
import com.bitorax.priziq.dto.request.session.session_participant.JoinSessionRequest;
import com.bitorax.priziq.dto.request.session.session_participant.LeaveSessionRequest;
import com.bitorax.priziq.dto.response.session.SessionParticipantResponse;

import java.util.List;

public interface SessionParticipantService {
    List<SessionParticipantResponse> joinSession(JoinSessionRequest request, String clientSessionId);

    List<SessionParticipantResponse> leaveSession(LeaveSessionRequest request, String clientSessionId);

    List<SessionParticipantResponse> findParticipantsBySessionCode(GetParticipantsRequest request);

    List<SessionParticipantResponse> updateRealtimeScoreAndRanking(String sessionId, String websocketSessionId, int responseScore);
}