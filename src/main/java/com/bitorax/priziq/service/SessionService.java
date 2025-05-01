package com.bitorax.priziq.service;

import com.bitorax.priziq.dto.request.session.CreateSessionRequest;
import com.bitorax.priziq.dto.request.session.UpdateSessionRequest;
import com.bitorax.priziq.dto.response.session.SessionResponse;

public interface SessionService {
    SessionResponse createSession(CreateSessionRequest createSessionRequest);

    SessionResponse updateSessionById(String sessionId, UpdateSessionRequest updateSessionRequest);
}
