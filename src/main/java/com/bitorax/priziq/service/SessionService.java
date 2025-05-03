package com.bitorax.priziq.service;

import com.bitorax.priziq.dto.request.session.CreateSessionRequest;
import com.bitorax.priziq.dto.request.session.EndSessionRequest;
import com.bitorax.priziq.dto.response.session.EndSessionSummaryResponse;
import com.bitorax.priziq.dto.response.session.SessionResponse;
import com.bitorax.priziq.dto.response.session.SessionSummaryResponse;

import java.util.List;

public interface SessionService {
    SessionResponse createSession(CreateSessionRequest createSessionRequest);

    SessionSummaryResponse endSession(EndSessionRequest endSessionRequest);

    List<EndSessionSummaryResponse> calculateSessionSummary(String sessionId);
}
