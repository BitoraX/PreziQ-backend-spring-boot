package com.bitorax.priziq.service.implement;

import com.bitorax.priziq.constant.SessionStatus;
import com.bitorax.priziq.domain.Collection;
import com.bitorax.priziq.domain.User;
import com.bitorax.priziq.domain.activity.Activity;
import com.bitorax.priziq.domain.session.ActivitySubmission;
import com.bitorax.priziq.domain.session.Session;
import com.bitorax.priziq.domain.session.SessionParticipant;
import com.bitorax.priziq.dto.request.achievement.AssignAchievementToUserRequest;
import com.bitorax.priziq.dto.request.session.CreateSessionRequest;
import com.bitorax.priziq.dto.request.session.EndSessionRequest;
import com.bitorax.priziq.dto.request.session.NextActivityRequest;
import com.bitorax.priziq.dto.request.session.StartSessionRequest;
import com.bitorax.priziq.dto.response.achievement.AchievementUpdateResponse;
import com.bitorax.priziq.dto.response.activity.ActivityDetailResponse;
import com.bitorax.priziq.dto.response.session.*;
import com.bitorax.priziq.exception.ApplicationException;
import com.bitorax.priziq.exception.ErrorCode;
import com.bitorax.priziq.mapper.ActivityMapper;
import com.bitorax.priziq.mapper.SessionMapper;
import com.bitorax.priziq.repository.*;
import com.bitorax.priziq.service.AchievementService;
import com.bitorax.priziq.service.SessionService;
import com.bitorax.priziq.utils.QRCodeUtils;
import com.bitorax.priziq.utils.SecurityUtils;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
    SecurityUtils securityUtils;
    QRCodeUtils qrCodeUtils;

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
        Collection currentCollection = collectionRepository.findById(createSessionRequest.getCollectionId())
                .orElseThrow(() -> new ApplicationException(ErrorCode.COLLECTION_NOT_FOUND));

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

        return sessionMapper.sessionToDetailResponse(sessionRepository.save(session));
    }

    @Override
    @Transactional
    public SessionSummaryResponse startSession(StartSessionRequest request) {
        Session session = getSessionById(request.getSessionId());

        if (session.getSessionStatus() != SessionStatus.PENDING) {
            throw new ApplicationException(ErrorCode.SESSION_NOT_PENDING);
        }

        session.setSessionStatus(SessionStatus.STARTED);
        sessionRepository.save(session);

        return sessionMapper.sessionToSummaryResponse(session);
    }

    @Override
    @Transactional
    public ActivityDetailResponse nextActivity(NextActivityRequest request) {
        Session session = getSessionById(request.getSessionId());

        if (session.getSessionStatus() != SessionStatus.STARTED) {
            throw new ApplicationException(ErrorCode.SESSION_NOT_STARTED);
        }

        // Find the next activity
        List<Activity> activities = session.getCollection().getActivities().stream()
                .filter(Activity::getIsPublished)
                .sorted(Comparator.comparingInt(Activity::getOrderIndex))
                .toList();

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

        if (currentSession.getSessionStatus() == SessionStatus.ENDED) {
            throw new ApplicationException(ErrorCode.SESSION_ALREADY_ENDED);
        }

        // Update session status and end time
        currentSession.setEndTime(Instant.now());
        currentSession.setSessionStatus(SessionStatus.ENDED);
        sessionRepository.save(currentSession);

        // Update totalPoints for each participant and collect achievement updates
        List<AchievementUpdateResponse> achievementUpdates = new ArrayList<>();
        List<SessionParticipant> participants = sessionParticipantRepository.findBySession_SessionId(endSessionRequest.getSessionId());
        List<User> usersToUpdate = new ArrayList<>();
        Map<String, Integer> userScoreMap = new HashMap<>();
        Set<String> processedUserIds = new HashSet<>();

        for (SessionParticipant participant : participants) {
            User user = participant.getUser();
            if (user != null) { // Only update for registered users
                String userId = user.getUserId();
                userScoreMap.merge(userId, participant.getRealtimeScore(), Integer::sum);

                if (processedUserIds.add(userId)) {
                    int scoreToAdd = userScoreMap.get(userId);
                    user.setTotalPoints(user.getTotalPoints() + scoreToAdd);
                    usersToUpdate.add(user);

                    AchievementUpdateResponse updateResponse = achievementService.assignAchievementsToUser(
                            AssignAchievementToUserRequest.builder()
                                    .userId(userId)
                                    .totalPoints(user.getTotalPoints())
                                    .build()
                    );

                    if (!updateResponse.getNewAchievements().isEmpty()) {
                        achievementUpdates.add(updateResponse);
                    }
                }
            }
        }

        if (!usersToUpdate.isEmpty()) {
            userRepository.saveAll(usersToUpdate);
        }

        return SessionEndResultResponse.builder()
                .sessionSummary(sessionMapper.sessionToSummaryResponse(currentSession))
                .achievementUpdates(achievementUpdates)
                .build();
    }

    @Override
    public SessionHistoryResponse getSessionHistory(String sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.SESSION_NOT_FOUND));

        // Retrieve all participants associated with the session
        List<SessionParticipant> participants = sessionParticipantRepository.findBySession_SessionId(sessionId);

        List<SessionEndSummaryResponse> summaries = calculateSessionSummary(sessionId);
        List<SessionParticipantHistoryResponse> participantHistoryResponses = new ArrayList<>();

        for (int i = 0; i < participants.size(); i++) {
            SessionParticipant participant = participants.get(i);
            SessionEndSummaryResponse summary = summaries.get(i);

            List<ActivitySubmission> submissions = activitySubmissionRepository
                    .findBySessionParticipant_SessionParticipantId(participant.getSessionParticipantId());

            List<ActivitySubmissionSummaryResponse> submissionResponses = submissions.stream()
                    .map(submission -> ActivitySubmissionSummaryResponse.builder()
                            .activitySubmissionId(submission.getActivitySubmissionId())
                            .answerContent(submission.getAnswerContent())
                            .isCorrect(submission.getIsCorrect())
                            .responseScore(submission.getResponseScore())
                            .build())
                    .collect(Collectors.toList());

            // Create SessionParticipantHistoryResponse
            SessionParticipantHistoryResponse participantResponse = SessionParticipantHistoryResponse.builder()
                    .sessionParticipantId(participant.getSessionParticipantId())
                    .activitySubmissions(submissionResponses)
                    .displayName(summary.getDisplayName())
                    .displayAvatar(summary.getDisplayAvatar())
                    .finalScore(summary.getFinalScore())
                    .finalRanking(summary.getFinalRanking())
                    .finalCorrectCount(summary.getFinalCorrectCount())
                    .finalIncorrectCount(summary.getFinalIncorrectCount())
                    .build();

            participantHistoryResponses.add(participantResponse);
        }

        return SessionHistoryResponse.builder()
                .session(sessionMapper.sessionToDetailResponse(session))
                .participantHistoryResponses(participantHistoryResponses)
                .build();
    }

    @Override
    public List<SessionEndSummaryResponse> calculateSessionSummary(String sessionId) {
        sessionRepository.findById(sessionId).orElseThrow(() -> new ApplicationException(ErrorCode.SESSION_NOT_FOUND));

        List<SessionParticipant> participants = sessionParticipantRepository.findBySession_SessionId(sessionId);
        List<SessionEndSummaryResponse> summaries = new ArrayList<>();

        for (SessionParticipant participant : participants) {
            List<ActivitySubmission> submissions = activitySubmissionRepository
                    .findBySessionParticipant_SessionParticipantId(participant.getSessionParticipantId());

            int finalScore = submissions.stream()
                    .mapToInt(ActivitySubmission::getResponseScore)
                    .sum(); // errors
            int finalCorrectCount = (int) submissions.stream()
                    .filter(ActivitySubmission::getIsCorrect)
                    .count();
            int finalIncorrectCount = submissions.size() - finalCorrectCount;

            SessionEndSummaryResponse summary = SessionEndSummaryResponse.builder()
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
    public String findSessionCodeBySessionId(String sessionId) {
        return sessionRepository.findSessionCodeBySessionId(sessionId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.SESSION_NOT_FOUND));
    }

    private Session getSessionById(String sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.SESSION_NOT_FOUND));
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