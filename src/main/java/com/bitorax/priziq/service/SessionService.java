package com.bitorax.priziq.service;

import com.bitorax.priziq.dto.request.session.CreateSessionRequest;
import com.bitorax.priziq.dto.request.session.EndSessionRequest;
import com.bitorax.priziq.dto.request.session.NextActivityRequest;
import com.bitorax.priziq.dto.request.session.StartSessionRequest;
import com.bitorax.priziq.dto.response.activity.ActivitySummaryResponse;
import com.bitorax.priziq.dto.response.session.EndSessionSummaryResponse;
import com.bitorax.priziq.dto.response.session.SessionHistoryResponse;
import com.bitorax.priziq.dto.response.session.SessionDetailResponse;
import com.bitorax.priziq.dto.response.session.SessionSummaryResponse;

import java.util.List;

public interface SessionService {
    SessionDetailResponse createSession(CreateSessionRequest createSessionRequest);

    SessionSummaryResponse startSession(StartSessionRequest request, String websocketSessionId);

    ActivitySummaryResponse nextActivity(NextActivityRequest request, String websocketSessionId);

    SessionSummaryResponse endSession(EndSessionRequest endSessionRequest, String websocketSessionId);

    SessionHistoryResponse getSessionHistory(String sessionId);

    List<EndSessionSummaryResponse> calculateSessionSummary(String sessionId);

    String findSessionCodeBySessionId(String sessionId);
}
