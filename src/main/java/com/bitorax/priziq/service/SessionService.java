package com.bitorax.priziq.service;

import com.bitorax.priziq.dto.request.session.CreateSessionRequest;
import com.bitorax.priziq.dto.request.session.EndSessionRequest;
import com.bitorax.priziq.dto.request.session.NextActivityRequest;
import com.bitorax.priziq.dto.request.session.StartSessionRequest;
import com.bitorax.priziq.dto.response.achievement.AchievementUpdateResponse;
import com.bitorax.priziq.dto.response.activity.ActivityDetailResponse;
import com.bitorax.priziq.dto.response.session.*;

import java.util.List;
import java.util.Map;

public interface SessionService {
    SessionDetailResponse createSession(CreateSessionRequest createSessionRequest);

    SessionSummaryResponse startSession(StartSessionRequest request);

    ActivityDetailResponse nextActivity(NextActivityRequest request);

    SessionEndResultResponse endSession(EndSessionRequest endSessionRequest);

    SessionHistoryResponse getSessionHistory(String sessionId);

    List<SessionEndSummaryResponse> calculateSessionSummary(String sessionId);

    String findSessionCodeBySessionId(String sessionId);

    List<Map.Entry<String, List<AchievementUpdateResponse>>> getAchievementUpdateDetails(
            List<AchievementUpdateResponse> achievementUpdates, String sessionId);
}
