package com.bitorax.priziq.service;

import com.bitorax.priziq.dto.request.session.CreateSessionRequest;
import com.bitorax.priziq.dto.request.session.EndSessionRequest;
import com.bitorax.priziq.dto.response.session.EndSessionSummaryResponse;
import com.bitorax.priziq.dto.response.session.SessionHistoryResponse;
import com.bitorax.priziq.dto.response.session.SessionDetailResponse;
import com.bitorax.priziq.dto.response.session.SessionSummaryResponse;

import java.util.List;
import java.util.Optional;

public interface SessionService {
    SessionDetailResponse createSession(CreateSessionRequest createSessionRequest);

    SessionHistoryResponse getSessionHistory(String sessionId);

    SessionSummaryResponse endSession(EndSessionRequest endSessionRequest);

    List<EndSessionSummaryResponse> calculateSessionSummary(String sessionId);

    String findSessionCodeBySessionId(String sessionId);
}
