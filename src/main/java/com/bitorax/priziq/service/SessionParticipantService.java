package com.bitorax.priziq.service;

import com.bitorax.priziq.dto.request.session.session_participant.CreateSessionParticipantRequest;
import com.bitorax.priziq.dto.request.session.session_participant.UpdateSessionParticipantRequest;
import com.bitorax.priziq.dto.response.session.SessionParticipantResponse;

public interface SessionParticipantService {
    SessionParticipantResponse joinSession(CreateSessionParticipantRequest request);

    SessionParticipantResponse updateSessionParticipantById(String sessionParticipantId, UpdateSessionParticipantRequest request);
}