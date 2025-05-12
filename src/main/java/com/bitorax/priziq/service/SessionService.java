package com.bitorax.priziq.service;

import com.bitorax.priziq.domain.session.ActivitySubmission;
import com.bitorax.priziq.domain.session.Session;
import com.bitorax.priziq.domain.session.SessionParticipant;
import com.bitorax.priziq.dto.request.session.CreateSessionRequest;
import com.bitorax.priziq.dto.request.session.EndSessionRequest;
import com.bitorax.priziq.dto.request.session.NextActivityRequest;
import com.bitorax.priziq.dto.request.session.StartSessionRequest;
import com.bitorax.priziq.dto.response.activity.ActivityDetailResponse;
import com.bitorax.priziq.dto.response.common.PaginationResponse;
import com.bitorax.priziq.dto.response.session.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;

public interface SessionService {
    SessionDetailResponse createSession(CreateSessionRequest createSessionRequest);

    PaginationResponse getMySessions(Specification<Session> spec, Pageable pageable);

    SessionSummaryResponse startSession(StartSessionRequest request);

    ActivityDetailResponse nextActivity(NextActivityRequest request);

    SessionEndResultResponse endSession(EndSessionRequest endSessionRequest);

    List<SessionEndSummaryResponse> calculateSessionSummary(String sessionId);

    String findSessionCodeBySessionId(String sessionId);

    List<Map.Entry<String, Object>> getSessionSummaryDetails(String sessionId);

    PaginationResponse getAllParticipantHistoryWithQuery(String sessionId, Specification<SessionParticipant> spec, Pageable pageable);

    PaginationResponse getAllActivitySubmissionHistoryWithQuery(String sessionId, String participantId, Specification<ActivitySubmission> spec, Pageable pageable);
}
