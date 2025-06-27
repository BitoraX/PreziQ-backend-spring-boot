package com.bitorax.priziq.service.implement;

import com.bitorax.priziq.constant.SessionStatus;
import com.bitorax.priziq.domain.User;
import com.bitorax.priziq.domain.session.ActivitySubmission;
import com.bitorax.priziq.domain.session.Session;
import com.bitorax.priziq.domain.session.SessionParticipant;
import com.bitorax.priziq.dto.cache.ParticipantCacheDTO;
import com.bitorax.priziq.dto.cache.SessionCacheDTO;
import com.bitorax.priziq.dto.request.session.session_participant.GetParticipantsRequest;
import com.bitorax.priziq.dto.request.session.session_participant.JoinSessionRequest;
import com.bitorax.priziq.dto.request.session.session_participant.LeaveSessionRequest;
import com.bitorax.priziq.dto.response.achievement.AchievementUpdateResponse;
import com.bitorax.priziq.dto.response.session.SessionParticipantSummaryResponse;
import com.bitorax.priziq.exception.ApplicationException;
import com.bitorax.priziq.exception.ErrorCode;
import com.bitorax.priziq.mapper.SessionParticipantMapper;
import com.bitorax.priziq.mapper.cache.ParticipantCacheMapper;
import com.bitorax.priziq.mapper.cache.SessionCacheMapper;
import com.bitorax.priziq.repository.ActivitySubmissionRepository;
import com.bitorax.priziq.repository.SessionParticipantRepository;
import com.bitorax.priziq.repository.SessionRepository;
import com.bitorax.priziq.repository.UserRepository;
import com.bitorax.priziq.service.SessionParticipantService;
import com.bitorax.priziq.service.cache.SessionRedisCache;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SessionParticipantServiceImpl implements SessionParticipantService {
    SessionParticipantRepository sessionParticipantRepository;
    SessionRepository sessionRepository;
    UserRepository userRepository;
    ActivitySubmissionRepository activitySubmissionRepository;
    SessionParticipantMapper sessionParticipantMapper;
    ParticipantCacheMapper participantCacheMapper;
    SessionCacheMapper sessionCacheMapper;
    SessionRedisCache sessionRedisCache;

    @Override
    @Transactional
    public List<SessionParticipantSummaryResponse> joinSession(JoinSessionRequest request, String websocketSessionId, String stompClientId) {
        Session session = sessionRepository.findBySessionCode(request.getSessionCode())
                .orElseThrow(() -> new ApplicationException(ErrorCode.SESSION_NOT_FOUND));

        // Cache Session after retrieval
        SessionCacheDTO cacheDTO = sessionCacheMapper.sessionToCacheDTO(session);
        sessionRedisCache.cacheSession(session.getSessionId(), cacheDTO);

        if (session.getSessionStatus() != SessionStatus.PENDING) {
            throw new ApplicationException(ErrorCode.SESSION_NOT_PENDING);
        }

        User user = null;
        String displayName = request.getDisplayName();
        String displayAvatar = request.getDisplayAvatar();

        // Handle logged-in user
        if (request.getUserId() != null) {
            user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND));
            // If displayName is not provided, use default from firstName + lastName
            if (displayName == null || displayName.trim().isEmpty()) {
                displayName = (user.getFirstName() + " " + user.getLastName()).trim();
            }
            // If displayAvatar is not provided, use default from user.avatar
            if (displayAvatar == null || displayAvatar.trim().isEmpty()) {
                displayAvatar = user.getAvatar();
            }
        }

        // Handle guest
        if (user == null) {
            if (displayName == null || displayName.trim().isEmpty()) {
                throw new ApplicationException(ErrorCode.INVALID_DISPLAY_NAME);
            }
        }

        SessionParticipant sessionParticipant = SessionParticipant.builder()
                .session(session)
                .user(user)
                .displayName(displayName)
                .displayAvatar(displayAvatar)
                .websocketSessionId(websocketSessionId)
                .stompClientId(stompClientId)
                .realtimeScore(0)
                .realtimeRanking(0)
                .build();

        sessionParticipantRepository.save(sessionParticipant);

        // Update cache for Participants
        List<SessionParticipant> participants = sessionParticipantRepository.findBySession_SessionCode(session.getSessionCode());
        sessionRedisCache.cacheParticipants(session.getSessionId(),
                participants.stream()
                        .map(participantCacheMapper::sessionParticipantToCacheDTO)
                        .collect(Collectors.toList()));

        return findParticipantsBySessionCode(GetParticipantsRequest.builder()
                .sessionCode(session.getSessionCode())
                .build());
    }

    @Override
    @Transactional
    public List<SessionParticipantSummaryResponse> leaveSession(LeaveSessionRequest request, String websocketSessionId) {
        Session session = sessionRepository.findBySessionCode(request.getSessionCode())
                .orElseThrow(() -> new ApplicationException(ErrorCode.SESSION_NOT_FOUND));
        SessionStatus sessionStatus = session.getSessionStatus();

        // Cache Session after retrieval
        SessionCacheDTO cacheDTO = sessionCacheMapper.sessionToCacheDTO(session);
        sessionRedisCache.cacheSession(session.getSessionId(), cacheDTO);

        SessionParticipant participant = sessionParticipantRepository
                .findBySessionAndWebsocketSessionId(session, websocketSessionId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.SESSION_PARTICIPANT_NOT_FOUND));

        if (sessionStatus == SessionStatus.PENDING) {
            // Delete all related ActivitySubmissions to avoid foreign key constraint violation
            List<ActivitySubmission> submissions = activitySubmissionRepository
                    .findBySessionParticipant_SessionParticipantId(participant.getSessionParticipantId());
            if (!submissions.isEmpty()) {
                activitySubmissionRepository.deleteAll(submissions);

                // Update cache for Submissions
                sessionRedisCache.cacheSubmissions(session.getSessionId(), new ArrayList<>());
            }

            // Delete the SessionParticipant
            sessionParticipantRepository.delete(participant);

            // Update cache for Participants
            List<SessionParticipant> participants = sessionParticipantRepository.findBySession_SessionCode(session.getSessionCode());
            sessionRedisCache.cacheParticipants(session.getSessionId(),
                    participants.stream()
                            .map(participantCacheMapper::sessionParticipantToCacheDTO)
                            .collect(Collectors.toList()));

            // Return updated participant list
            return findParticipantsBySessionCode(GetParticipantsRequest.builder()
                    .sessionCode(session.getSessionCode())
                    .build());
        } else if (sessionStatus == SessionStatus.STARTED) {
            // Mark participant as inactive instead of deleting
            participant.setIsConnected(false);
            sessionParticipantRepository.save(participant);

            // Update cache for Participants
            List<SessionParticipant> participants = sessionParticipantRepository
                    .findBySession_SessionCodeAndIsConnectedTrue(session.getSessionCode());
            sessionRedisCache.cacheParticipants(session.getSessionId(),
                    participants.stream()
                            .map(participantCacheMapper::sessionParticipantToCacheDTO)
                            .collect(Collectors.toList()));

            // Return list of active participants
            return participants.stream()
                    .map(sessionParticipantMapper::sessionParticipantToSummaryResponse)
                    .collect(Collectors.toList());
        } else {
            throw new ApplicationException(ErrorCode.INVALID_SESSION_STATUS);
        }
    }

    @Override
    public List<SessionParticipantSummaryResponse> findParticipantsBySessionCode(GetParticipantsRequest request) {
        Session session = sessionRepository.findBySessionCode(request.getSessionCode())
                .orElseThrow(() -> new ApplicationException(ErrorCode.SESSION_NOT_FOUND));

        // Check the cache for Participants before querying DB
        List<ParticipantCacheDTO> cachedParticipants = sessionRedisCache.getCachedParticipants(session.getSessionId());
        if (!cachedParticipants.isEmpty()) {
            return cachedParticipants.stream()
                    .map(participant -> SessionParticipantSummaryResponse.builder()
                            .sessionParticipantId(participant.getSessionParticipantId())
                            .displayName(participant.getDisplayName())
                            .displayAvatar(participant.getDisplayAvatar())
                            .realtimeScore(participant.getRealtimeScore())
                            .realtimeRanking(participant.getRealtimeRanking())
                            .build())
                    .collect(Collectors.toList());
        }

        List<SessionParticipant> participants = sessionParticipantRepository.findBySession_SessionCode(request.getSessionCode());

        // Cache Participants after retrieval
        sessionRedisCache.cacheParticipants(session.getSessionId(),
                participants.stream()
                        .map(participantCacheMapper::sessionParticipantToCacheDTO)
                        .collect(Collectors.toList()));

        return sessionParticipantMapper.sessionParticipantsToSummaryResponseList(participants);
    }

    @Override
    @Transactional
    public List<SessionParticipantSummaryResponse> updateRealtimeScoreAndRanking(String sessionCode, String websocketSessionId, int responseScore) {
        Session session = sessionRepository.findBySessionCode(sessionCode)
                .orElseThrow(() -> new ApplicationException(ErrorCode.SESSION_NOT_FOUND));

        // Find SessionParticipant by sessionId and websocketSessionId
        SessionParticipant participant = sessionParticipantRepository
                .findBySessionAndWebsocketSessionId(session, websocketSessionId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.SESSION_PARTICIPANT_NOT_FOUND));

        // Update realtimeScore
        participant.setRealtimeScore(participant.getRealtimeScore() + responseScore);

        // Save the updated participant
        sessionParticipantRepository.save(participant);

        // Get all participants in the session and update rankings
        List<SessionParticipant> participants = sessionParticipantRepository.findBySession_SessionCode(sessionCode);
        // Sort by realtimeScore (descending) and assign rankings
        List<SessionParticipant> sortedParticipants = participants.stream()
                .sorted(Comparator.comparingInt(SessionParticipant::getRealtimeScore).reversed())
                .collect(Collectors.toList());

        // Update realtimeRanking
        for (int i = 0; i < sortedParticipants.size(); i++) {
            sortedParticipants.get(i).setRealtimeRanking(i + 1);
            // Update cache for each participant
            sessionRedisCache.updateParticipantScoreAndRanking(
                    session.getSessionId(),
                    sortedParticipants.get(i).getSessionParticipantId(),
                    sortedParticipants.get(i).getRealtimeScore(),
                    sortedParticipants.get(i).getRealtimeRanking()
            );
        }

        // Save all participants with updated rankings
        sessionParticipantRepository.saveAll(sortedParticipants);

        // Update cache for all Participants
        sessionRedisCache.cacheParticipants(session.getSessionId(),
                sortedParticipants.stream()
                        .map(participantCacheMapper::sessionParticipantToCacheDTO)
                        .collect(Collectors.toList()));

        // Return updated participant list
        return sessionParticipantMapper.sessionParticipantsToSummaryResponseList(participants);
    }

    @Override
    public List<Map.Entry<String, AchievementUpdateResponse>> getAchievementUpdateDetails(List<AchievementUpdateResponse> achievementUpdates, String sessionId) {
        // Check the cache for Participants before querying DB
        List<ParticipantCacheDTO> cachedParticipants = sessionRedisCache.getCachedParticipants(sessionId);
        List<SessionParticipant> participants;
        if (!cachedParticipants.isEmpty()) {
            participants = cachedParticipants.stream()
                    .map(participantCacheMapper::participantCacheDTOToSessionParticipant)
                    .collect(Collectors.toList());
        } else {
            participants = sessionParticipantRepository.findBySession_SessionId(sessionId);
            // Cache Participants after retrieval
            sessionRedisCache.cacheParticipants(sessionId,
                    participants.stream()
                            .map(participantCacheMapper::sessionParticipantToCacheDTO)
                            .collect(Collectors.toList()));
        }

        List<Map.Entry<String, AchievementUpdateResponse>> updateDetails = new ArrayList<>();

        if (achievementUpdates == null || achievementUpdates.isEmpty()) {
            return updateDetails;
        }

        Map<String, AchievementUpdateResponse> userIdToUpdateMap = new HashMap<>();
        for (AchievementUpdateResponse update : achievementUpdates) {
            String userId = update.getUserId();
            if (userId != null && userRepository.existsById(userId)) {
                userIdToUpdateMap.put(userId, update);
            }
        }

        for (SessionParticipant participant : participants) {
            String stompClientId = participant.getStompClientId();
            User user = participant.getUser();

            if (user != null && user.getUserId() != null && userIdToUpdateMap.containsKey(user.getUserId())) {
                AchievementUpdateResponse update = userIdToUpdateMap.get(user.getUserId());
                if (stompClientId != null) {
                    updateDetails.add(Map.entry(stompClientId, update));
                }
            }
        }

        return updateDetails;
    }
}