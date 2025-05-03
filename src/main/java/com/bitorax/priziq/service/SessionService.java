package com.bitorax.priziq.service;

import com.bitorax.priziq.dto.request.session.CreateSessionRequest;
import com.bitorax.priziq.dto.request.session.EndSessionRequest;
import com.bitorax.priziq.dto.response.session.SessionResponse;

public interface SessionService {
    SessionResponse createSession(CreateSessionRequest createSessionRequest);

    SessionResponse endSession(EndSessionRequest endSessionRequest);
}
