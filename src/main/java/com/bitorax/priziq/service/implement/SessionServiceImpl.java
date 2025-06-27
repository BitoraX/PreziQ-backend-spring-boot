package com.bitorax.priziq.service.implement;

import com.bitorax.priziq.constant.SessionStatus;
import com.bitorax.priziq.domain.Collection;
import com.bitorax.priziq.domain.User;
import com.bitorax.priziq.domain.activity.Activity;
import com.bitorax.priziq.domain.session.ActivitySubmission;
import com.bitorax.priziq.domain.session.Session;
import com.bitorax.priziq.domain.session.SessionParticipant;
import com.bitorax.priziq.dto.cache.SessionCacheDTO;
import com.bitorax.priziq.dto.cache.CollectionCacheDTO;
import com.bitorax.priziq.dto.cache.ActivityCacheDTO;
import com.bitorax.priziq.dto.cache.SubmissionCacheDTO;
import com.bitorax.priziq.dto.request.achievement.AssignAchievementToUserRequest;
import com.bitorax.priziq.dto.request.session.CreateSessionRequest;
import com.bitorax.priziq.dto.request.session.EndSessionRequest;
import com.bitorax.priziq.dto.request.session.NextActivityRequest;
import com.bitorax.priziq.dto.request.session.StartSessionRequest;
import com.bitorax.priziq.dto.response.achievement.AchievementUpdateResponse;
import com.bitorax.priziq.dto.response.activity.ActivityDetailResponse;
import com.bitorax.priziq.dto.response.common.PaginationMeta;
import com.bitorax.priziq.dto.response.common.PaginationResponse;
import com.bitorax.priziq.dto.response.session.*;
import com.bitorax.priziq.exception.ApplicationException;
import com.bitorax.priziq.exception.ErrorCode;
import com.bitorax.priziq.mapper.ActivityMapper;
import com.bitorax.priziq.mapper.ActivitySubmissionMapper;
import com.bitorax.priziq.mapper.SessionMapper;
import com.bitorax.priziq.mapper.cache.ActivityCacheMapper;
import com.bitorax.priziq.mapper.cache.CollectionCacheMapper;
import com.bitorax.priziq.mapper.cache.SessionCacheMapper;
import com.bitorax.priziq.mapper.cache.SubmissionCacheMapper;
import com.bitorax.priziq.repository.*;
import com.bitorax.priziq.service.AchievementService;
import com.bitorax.priziq.service.SessionService;
import com.bitorax.priziq.service.cache.SessionRedisCache;
import com.bitorax.priziq.utils.QRCodeUtils;
import com.bitorax.priziq.utils.SecurityUtils;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SessionServiceImpl implements SessionService {
    SessionRepository sessionRepository;
    CollectionRepository collectionRepository;
    SessionParticipantRepository sessionParticipantRepository;
    ActivitySubmissionRepository activitySubmissionRepository;
    UserRepository userRepository;
    AchievementService achievementService;
    SessionMapper sessionMapper;
    ActivityMapper activityMapper;
    ActivitySubmissionMapper activitySubmissionMapper;
    SecurityUtils securityUtils;
    QRCodeUtils qrCodeUtils;
    SessionRedisCache sessionRedisCache;
    SessionCacheMapper sessionCacheMapper;
    CollectionCacheMapper collectionCacheMapper;
    ActivityCacheMapper activityCacheMapper;
    SubmissionCacheMapper submissionCacheMapper;

    @NonFinal
    @Value("${session.code.characters}")
    String SESSION_CODE_CHARACTERS;

    @NonFinal
    @Value("${session.code.length}")
    Integer SESSION_CODE_LENGTH;

    @NonFinal
    @Value("${session.code.max-attempts}")
    Integer SESSION_CODE_MAX_ATTEMPTS;

    @NonFinal
    @Value("${priziq.frontend.base-url}")
    String FRONT_END_BASE_URL;

    @Override
    @Transactional
    public SessionDetailResponse createSession(CreateSessionRequest createSessionRequest) {
        // Check cache for Collection before querying DB
        CollectionCacheDTO cachedCollection = sessionRedisCache.getCachedCollectionById(createSessionRequest.getCollectionId());
        Collection currentCollection;
        if (cachedCollection != null) {
            currentCollection = collectionCacheMapper.collectionCacheDTOToCollection(cachedCollection);
        } else {
            currentCollection = collectionRepository.findById(createSessionRequest.getCollectionId())
                    .orElseThrow(() -> new ApplicationException(ErrorCode.COLLECTION_NOT_FOUND));
            sessionRedisCache.cacheCollectionById(createSessionRequest.getCollectionId(),
                    collectionCacheMapper.collectionToCacheDTO(currentCollection));
        }

        Session session = Session.builder()
                .collection(currentCollection)
                .hostUser(securityUtils.getAuthenticatedUser())
                .sessionCode(generateUniqueSessionCode())
                .startTime(Instant.now())
                .sessionStatus(SessionStatus.PENDING)
                .build();
        sessionRepository.save(session);

        // Generate QR code for session
        try {
            String contentQrCode = FRONT_END_BASE_URL + "/sessions/" + session.getSessionCode();
            String qrUrl = qrCodeUtils.generateQRCode(contentQrCode);
            session.setJoinSessionQrUrl(qrUrl);
        } catch (Exception e) {
            throw new ApplicationException(ErrorCode.QR_CODE_GENERATION_FAILED);
        }

        Session savedSession = sessionRepository.save(session);
        // Cache Session and Collection
        sessionRedisCache.cacheSession(savedSession.getSessionId(),
                sessionCacheMapper.sessionToCacheDTO(savedSession));
        sessionRedisCache.cacheCollection(savedSession.getSessionId(),
                collectionCacheMapper.collectionToCacheDTO(currentCollection));
        // Cache Activities
        List<ActivityCacheDTO> activities = currentCollection.getActivities().stream()
                .map(activityCacheMapper::activityToCacheDTO)
                .collect(Collectors.toList());
        sessionRedisCache.cacheActivities(savedSession.getSessionId(), activities);

        return sessionMapper.sessionToDetailResponse(savedSession);
    }

    @Override
    @Transactional
    public SessionSummaryResponse startSession(StartSessionRequest request) {
        Session session = getSessionById(request.getSessionId());

        if (session.getSessionStatus() != SessionStatus.PENDING) {
            throw new ApplicationException(ErrorCode.SESSION_NOT_PENDING);
        }

        session.setSessionStatus(SessionStatus.STARTED);
        Session savedSession = sessionRepository.save(session);

        // Update cache for Session
        sessionRedisCache.cacheSession(savedSession.getSessionId(),
                sessionCacheMapper.sessionToCacheDTO(savedSession));

        return sessionMapper.sessionToSummaryResponse(savedSession);
    }

    @Override
    @Transactional
    public ActivityDetailResponse nextActivity(NextActivityRequest request) {
        Session session = getSessionById(request.getSessionId());

        if (session.getSessionStatus() != SessionStatus.STARTED) {
            throw new ApplicationException(ErrorCode.SESSION_NOT_STARTED);
        }

        // Check the cache for Activities before querying DB and find the next activity
        List<ActivityCacheDTO> cachedActivities = sessionRedisCache.getCachedActivities(session.getSessionId());
        List<Activity> activities;
        if (!cachedActivities.isEmpty()) {
            activities = cachedActivities.stream()
                    .map(activityCacheMapper::activityCacheDTOToActivity)
                    .filter(activity -> activity.getIsPublished() != null && activity.getIsPublished())
                    .sorted(Comparator.comparingInt(Activity::getOrderIndex))
                    .collect(Collectors.toList());
        } else {
            activities = session.getCollection().getActivities().stream()
                    .filter(Activity::getIsPublished)
                    .sorted(Comparator.comparingInt(Activity::getOrderIndex))
                    .toList();
            sessionRedisCache.cacheActivities(session.getSessionId(),
                    activities.stream()
                            .map(activityCacheMapper::activityToCacheDTO)
                            .collect(Collectors.toList()));
        }

        if (activities.isEmpty()) {
            return null;
        }

        if (request.getActivityId() == null) {
            return activityMapper.activityToDetailResponse(activities.getFirst());
        }

        for (int i = 0; i < activities.size() - 1; i++) {
            if (activities.get(i).getActivityId().equals(request.getActivityId())) {
                return activityMapper.activityToDetailResponse(activities.get(i + 1));
            }
        }

        return null; // No next activity
    }

    @Override
    @Transactional
    public SessionEndResultResponse endSession(EndSessionRequest endSessionRequest) {
        Session currentSession = getSessionById(endSessionRequest.getSessionId());
        String hostUserid = currentSession.getHostUser().getUserId();

        if (currentSession.getSessionStatus() == SessionStatus.ENDED) {
            throw new ApplicationException(ErrorCode.SESSION_ALREADY_ENDED);
        }

        // Update session status and end time
        currentSession.setEndTime(Instant.now());
        currentSession.setSessionStatus(SessionStatus.ENDED);
        Session savedSession = sessionRepository.save(currentSession);

        // Update cache for Session
        sessionRedisCache.cacheSession(savedSession.getSessionId(),
                sessionCacheMapper.sessionToCacheDTO(savedSession));

        // Update totalPoints for each participant and collect achievement updates
        List<AchievementUpdateResponse> achievementUpdates = new ArrayList<>();
        List<SessionParticipant> participants = sessionParticipantRepository.findBySession_SessionId(endSessionRequest.getSessionId());
        Map<String, Integer> userScoreMap = new HashMap<>();
        Set<String> processedUserIds = new HashSet<>();

        log.info("Processing {} participants for sessionId: {}", participants.size(), endSessionRequest.getSessionId());

        for (SessionParticipant participant : participants) {
            User user = participant.getUser();
            if (user != null) { // Only update for registered users
                String userId = user.getUserId();
                if (!userRepository.existsById(userId)) {
                    log.warn("User with userId {} does not exist, skipping achievement update", userId);
                    continue;
                }
                userScoreMap.merge(userId, participant.getRealtimeScore(), Integer::sum);

                if (processedUserIds.add(userId)) {
                    int scoreToAdd = userScoreMap.get(userId);

                    if (!user.getUserId().equals(hostUserid)) {
                        user.setTotalPoints(user.getTotalPoints() + scoreToAdd);
                        userRepository.save(user);

                        try {
                            AchievementUpdateResponse updateResponse = achievementService.assignAchievementsToUser(
                                    AssignAchievementToUserRequest.builder()
                                            .userId(userId)
                                            .totalPoints(user.getTotalPoints())
                                            .build()
                            );
                            achievementUpdates.add(updateResponse);
                        } catch (Exception e) {
                            log.error("Failed to assign achievements for userId {}: {}", userId, e.getMessage(), e);
                        }
                    } else {
                        log.info("Host user {} has realtimeScore {} but will not update totalPoints or receive achievements", userId, scoreToAdd);
                    }
                }
            }
        }

        // Remove Session cache after ending
        sessionRedisCache.removeCachedSession(savedSession.getSessionId());

        return SessionEndResultResponse.builder()
                .sessionSummary(sessionMapper.sessionToSummaryResponse(savedSession))
                .achievementUpdates(achievementUpdates)
                .build();
    }

    @Override
    public PaginationResponse getMySessions(Specification<Session> spec, Pageable pageable) {
        User creator = userRepository.findByEmail(SecurityUtils.getCurrentUserEmailFromJwt())
                .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND));

        // Filter sessions where the user is either the host or a participant
        Specification<Session> userSpec = (root, query, criteriaBuilder) -> {
            Join<Session, SessionParticipant> participantJoin = root.join("sessionParticipants", JoinType.LEFT);

            // Condition: user is either the host (hostUser) or a participant (user in sessionParticipants)
            return criteriaBuilder.or(
                    criteriaBuilder.equal(root.get("hostUser").get("userId"), creator.getUserId()),
                    criteriaBuilder.equal(participantJoin.get("user").get("userId"), creator.getUserId())
            );
        };

        // Merge with client-provided specification if present and query
        Specification<Session> finalSpec = spec != null ? Specification.where(spec).and(userSpec) : userSpec;
        Page<Session> sessionPage = this.sessionRepository.findAll(finalSpec, pageable);

        // Cache each Session in the page
        sessionPage.getContent().forEach(session ->
                sessionRedisCache.cacheSession(session.getSessionId(),
                        sessionCacheMapper.sessionToCacheDTO(session)));
        return PaginationResponse.builder()
                .meta(PaginationMeta.builder()
                        .currentPage(pageable.getPageNumber() + 1) // base-index = 0
                        .pageSize(pageable.getPageSize())
                        .totalPages(sessionPage.getTotalPages())
                        .totalElements(sessionPage.getTotalElements())
                        .hasNext(sessionPage.hasNext())
                        .hasPrevious(sessionPage.hasPrevious())
                        .build())
                .content(this.sessionMapper.sessionsToDetailResponseList(sessionPage.getContent()))
                .build();
    }

    @Override
    public List<SessionEndSummaryResponse> calculateSessionSummary(String sessionId) {
        // Check cache for Session before querying DB
        SessionCacheDTO cachedSession = sessionRedisCache.getCachedSession(sessionId);
        if (cachedSession == null) {
            sessionRepository.findById(sessionId)
                    .orElseThrow(() -> new ApplicationException(ErrorCode.SESSION_NOT_FOUND));
        }

        List<SessionParticipant> participants = sessionParticipantRepository.findBySession_SessionId(sessionId);
        List<SessionEndSummaryResponse> summaries = new ArrayList<>();

        for (SessionParticipant participant : participants) {
            // Check cache for Submissions
            List<SubmissionCacheDTO> cachedSubmissions = sessionRedisCache.getCachedSubmissions(sessionId);
            List<ActivitySubmission> submissions;
            if (!cachedSubmissions.isEmpty()) {
                submissions = cachedSubmissions.stream()
                        .filter(sub -> sub.getSessionParticipantId().equals(participant.getSessionParticipantId()))
                        .map(submissionCacheMapper::submissionCacheDTOToActivitySubmission)
                        .collect(Collectors.toList());
            } else {
                submissions = activitySubmissionRepository
                        .findBySessionParticipant_SessionParticipantId(participant.getSessionParticipantId());
                sessionRedisCache.cacheSubmissions(sessionId,
                        submissions.stream()
                                .map(submissionCacheMapper::activitySubmissionToCacheDTO)
                                .collect(Collectors.toList()));
            }

            int finalScore = submissions.stream()
                    .mapToInt(submission -> submission.getResponseScore() != null ? submission.getResponseScore() : 0)
                    .sum();
            int finalCorrectCount = (int) submissions.stream()
                    .filter(submission -> Boolean.TRUE.equals(submission.getIsCorrect()))
                    .count();
            int finalIncorrectCount = submissions.size() - finalCorrectCount;

            SessionEndSummaryResponse summary = SessionEndSummaryResponse.builder()
                    .sessionParticipantId(participant.getSessionParticipantId())
                    .displayName(participant.getDisplayName())
                    .displayAvatar(participant.getDisplayAvatar())
                    .finalScore(finalScore)
                    .finalCorrectCount(finalCorrectCount)
                    .finalIncorrectCount(finalIncorrectCount)
                    .build();

            summaries.add(summary);
        }

        // Sort and assign ratings
        summaries.sort(Comparator.comparingInt(SessionEndSummaryResponse::getFinalScore).reversed());
        for (int i = 0; i < summaries.size(); i++) {
            summaries.get(i).setFinalRanking(i + 1);
        }

        return summaries;
    }

    @Override
    public List<Map.Entry<String, Object>> getSessionSummaryDetails(String sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.SESSION_NOT_FOUND));
        User hostUser = session.getHostUser();

        List<SessionEndSummaryResponse> summaries = calculateSessionSummary(sessionId);
        List<SessionParticipant> participants = sessionParticipantRepository.findBySession_SessionId(sessionId);
        List<Map.Entry<String, Object>> summaryDetails = new ArrayList<>();

        // Handle each participant
        for (SessionParticipant participant : participants) {
            String stompClientId = participant.getStompClientId();
            if (stompClientId == null) {
                continue;
            }

            // Find the corresponding SessionEndSummaryResponse
            SessionEndSummaryResponse participantSummary = summaries.stream()
                    .filter(summary -> summary.getSessionParticipantId().equals(participant.getSessionParticipantId()))
                    .findFirst()
                    .orElseThrow(() -> new ApplicationException(ErrorCode.SUMMARY_NOT_FOUND_FOR_PARTICIPANT));

            // If host user, add the entire list of summaries
            if (participant.getUser() != null && hostUser != null && participant.getUser().getUserId().equals(hostUser.getUserId())) {
                summaryDetails.add(Map.entry(stompClientId, summaries));
            } else {
                // If not host user (guest or user login), add just their summary
                summaryDetails.add(Map.entry(stompClientId, participantSummary));
            }
        }

        return summaryDetails;
    }

    @Override
    public String findSessionCodeBySessionId(String sessionId) {
        // Check cache before querying DB
        SessionCacheDTO cachedSession = sessionRedisCache.getCachedSession(sessionId);
        if (cachedSession != null) {
            return cachedSession.getSessionCode();
        }

        return sessionRepository.findSessionCodeBySessionId(sessionId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.SESSION_NOT_FOUND));
    }

    @Override
    public PaginationResponse getAllParticipantHistoryWithQuery(String sessionId, Specification<SessionParticipant> spec, Pageable pageable) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.SESSION_NOT_FOUND));

        // Check if a user has an ADMIN role. If not admin, verify if user is a participant in the session
        User currentUser = securityUtils.getAuthenticatedUser();
        boolean isAdmin = securityUtils.isAdmin(currentUser);
        boolean isParticipant = session.getSessionParticipants().stream()
                .anyMatch(participant -> participant.getUser() != null && currentUser.getUserId().equals(participant.getUser().getUserId()));
        if (!isAdmin && !isParticipant) {
            throw new ApplicationException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        // Create specification to filter SessionParticipant by sessionId
        Specification<SessionParticipant> finalSpec = Specification.where(spec)
                .and((root, query, cb) -> cb.equal(root.get("session").get("sessionId"), sessionId));

        Page<SessionParticipant> participantPage = sessionParticipantRepository.findAll(finalSpec, pageable);

        // Calculate summary for session and convert to SessionParticipantHistoryResponse
        List<SessionEndSummaryResponse> summaries = calculateSessionSummary(sessionId);
        List<SessionParticipantHistoryResponse> participantHistories = participantPage.getContent().stream()
                .map(participant -> {
                    // Find summary corresponding to participant
                    SessionEndSummaryResponse summary = summaries.stream()
                            .filter(s -> s.getSessionParticipantId().equals(participant.getSessionParticipantId()))
                            .findFirst()
                            .orElseThrow(() -> new ApplicationException(ErrorCode.SESSION_PARTICIPANT_NOT_FOUND));

                    return SessionParticipantHistoryResponse.builder()
                            .sessionParticipantId(summary.getSessionParticipantId())
                            .displayName(summary.getDisplayName())
                            .displayAvatar(summary.getDisplayAvatar())
                            .finalScore(summary.getFinalScore())
                            .finalRanking(summary.getFinalRanking())
                            .finalCorrectCount(summary.getFinalCorrectCount())
                            .finalIncorrectCount(summary.getFinalIncorrectCount())
                            .build();
                })
                .toList();

        return PaginationResponse.builder()
                .meta(PaginationMeta.builder()
                        .currentPage(pageable.getPageNumber() + 1)
                        .pageSize(pageable.getPageSize())
                        .totalPages(participantPage.getTotalPages())
                        .totalElements(participantPage.getTotalElements())
                        .hasNext(participantPage.hasNext())
                        .hasPrevious(participantPage.hasPrevious())
                        .build())
                .content(participantHistories)
                .build();
    }

    @Override
    public PaginationResponse getAllActivitySubmissionHistoryWithQuery(String sessionId, String participantId, Specification<ActivitySubmission> spec, Pageable pageable) {
        // Validate participant
        SessionParticipant participant = sessionParticipantRepository.findById(participantId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.SESSION_PARTICIPANT_NOT_FOUND));

        if (!participant.getSession().getSessionId().equals(sessionId)) {
            throw new ApplicationException(ErrorCode.PARTICIPANT_NOT_IN_SESSION);
        }

        // Check if user has ADMIN role. If not admin, verify if user is the participant
        User currentUser = securityUtils.getAuthenticatedUser();
        boolean isAdmin = securityUtils.isAdmin(currentUser);
        boolean isSelf = participant.getUser() != null && currentUser.getUserId().equals(participant.getUser().getUserId());
        if (!isAdmin && !isSelf) {
            throw new ApplicationException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        // Create specification to filter ActivitySubmission by participantId
        Specification<ActivitySubmission> finalSpec = Specification.where(spec)
                .and((root, query, cb) -> cb.equal(root.get("sessionParticipant").get("sessionParticipantId"), participantId));

        Page<ActivitySubmission> submissionPage = activitySubmissionRepository.findAll(finalSpec, pageable);

        // Convert to ActivitySubmissionHistoryResponse
        List<ActivitySubmissionHistoryResponse> submissionHistories = submissionPage.getContent().stream()
                .map(activitySubmissionMapper::activitySubmissionToHistoryResponse)
                .toList();

        return PaginationResponse.builder()
                .meta(PaginationMeta.builder()
                        .currentPage(pageable.getPageNumber() + 1)
                        .pageSize(pageable.getPageSize())
                        .totalPages(submissionPage.getTotalPages())
                        .totalElements(submissionPage.getTotalElements())
                        .hasNext(submissionPage.hasNext())
                        .hasPrevious(submissionPage.hasPrevious())
                        .build())
                .content(submissionHistories)
                .build();
    }

    private Session getSessionById(String sessionId) {
        // Check cache before querying DB
        SessionCacheDTO cachedSession = sessionRedisCache.getCachedSession(sessionId);
        if (cachedSession != null) {
            return sessionCacheMapper.sessionCacheDTOToSession(cachedSession);
        }

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.SESSION_NOT_FOUND));
        sessionRedisCache.cacheSession(sessionId, sessionCacheMapper.sessionToCacheDTO(session));

        return session;
    }

    private String generateUniqueSessionCode() {
        SecureRandom random = new SecureRandom();
        StringBuilder codeBuilder = new StringBuilder(SESSION_CODE_LENGTH);
        int attempts = 0;

        while (attempts < SESSION_CODE_MAX_ATTEMPTS) {
            codeBuilder.setLength(0); // Reset builder
            for (int i = 0; i < SESSION_CODE_LENGTH; i++) {
                int index = random.nextInt(SESSION_CODE_CHARACTERS.length());
                codeBuilder.append(SESSION_CODE_CHARACTERS.charAt(index));
            }
            String sessionCode = codeBuilder.toString();

            // Check if code is unique
            if (sessionRepository.findBySessionCode(sessionCode).isEmpty()) {
                return sessionCode;
            }
            attempts++;
        }

        // Throw exception if no unique code is found after max attempts
        throw new ApplicationException(ErrorCode.UNABLE_TO_GENERATE_SESSION_CODE);
    }
}