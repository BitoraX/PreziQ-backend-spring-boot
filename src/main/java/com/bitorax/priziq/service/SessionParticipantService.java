package com.bitorax.priziq.service;

import com.bitorax.priziq.dto.request.session.session_participant.JoinSessionRequest;
import com.bitorax.priziq.dto.request.session.session_participant.LeaveSessionRequest;
import com.bitorax.priziq.dto.response.session.SessionParticipantResponse;

import java.util.List;

public interface SessionParticipantService {
    List<SessionParticipantResponse> joinSession(JoinSessionRequest request);

    List<SessionParticipantResponse> leaveSession(LeaveSessionRequest request);

    List<SessionParticipantResponse> findParticipantsBySessionCode(String sessionCode);
}