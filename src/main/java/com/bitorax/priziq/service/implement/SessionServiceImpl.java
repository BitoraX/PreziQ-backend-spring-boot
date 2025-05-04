package com.bitorax.priziq.service.implement;

import com.bitorax.priziq.domain.Collection;
import com.bitorax.priziq.domain.User;
import com.bitorax.priziq.domain.session.ActivitySubmission;
import com.bitorax.priziq.domain.session.Session;
import com.bitorax.priziq.domain.session.SessionParticipant;
import com.bitorax.priziq.dto.request.session.CreateSessionRequest;
import com.bitorax.priziq.dto.request.session.EndSessionRequest;
import com.bitorax.priziq.dto.response.session.*;
import com.bitorax.priziq.exception.ApplicationException;
import com.bitorax.priziq.exception.ErrorCode;
import com.bitorax.priziq.mapper.ActivitySubmissionMapper;
import com.bitorax.priziq.mapper.SessionMapper;
import com.bitorax.priziq.mapper.SessionParticipantMapper;
import com.bitorax.priziq.repository.ActivitySubmissionRepository;
import com.bitorax.priziq.repository.CollectionRepository;
import com.bitorax.priziq.repository.SessionParticipantRepository;
import com.bitorax.priziq.repository.SessionRepository;
import com.bitorax.priziq.service.SessionService;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
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
    SessionMapper sessionMapper;
    SessionParticipantMapper sessionParticipantMapper;
    ActivitySubmissionMapper activitySubmissionMapper;
    SecurityUtils securityUtils;

    @NonFinal
    @Value("${session.code.characters}")
    String SESSION_CODE_CHARACTERS;

    @NonFinal
    @Value("${session.code.length}")
    Integer SESSION_CODE_LENGTH;

    @NonFinal
    @Value("${session.code.max-attempts}")
    Integer SESSION_CODE_MAX_ATTEMPTS;

    @Override
    @Transactional
    public SessionDetailResponse createSession(CreateSessionRequest createSessionRequest){
        Collection currentCollection = collectionRepository.findById(createSessionRequest.getCollectionId()).orElseThrow(() -> new ApplicationException(ErrorCode.COLLECTION_NOT_FOUND));

        Session session = Session.builder()
                .collection(currentCollection)
                .hostUser(securityUtils.getAuthenticatedUser())
                .sessionCode(generateUniqueSessionCode())
                .startTime(Instant.now())
                .isActive(true)
                .build();

        return sessionMapper.sessionToDetailResponse(sessionRepository.save(session));
    }

    @Override
    @Transactional
    public SessionSummaryResponse endSession(EndSessionRequest endSessionRequest) {
        Session currentSession = sessionRepository.findById(endSessionRequest.getSessionId())
                .orElseThrow(() -> new ApplicationException(ErrorCode.SESSION_NOT_FOUND));

        // Only the host can end the session
        User currentUser = securityUtils.getAuthenticatedUser();
        if (!currentSession.getHostUser().getUserId().equals(currentUser.getUserId())) {
            throw new ApplicationException(ErrorCode.ONLY_HOST_USER_END_SESSION);
        }

        if (!currentSession.getIsActive()) {
            throw new ApplicationException(ErrorCode.SESSION_ALREADY_ENDED);
        }

        currentSession.setEndTime(Instant.now());
        currentSession.setIsActive(false);

        return sessionMapper.sessionToSummaryResponse(sessionRepository.save(currentSession));
    }

    @Override
    public SessionHistoryResponse getSessionHistory(String sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.SESSION_NOT_FOUND));

        // Retrieve all participants associated with the session
        List<SessionParticipant> participants = sessionParticipantRepository.findBySession_SessionId(sessionId);

        List<EndSessionSummaryResponse> summaries = calculateSessionSummary(sessionId);
        List<SessionParticipantHistoryResponse> participantHistoryResponses = new ArrayList<>();

        for (int i = 0; i < participants.size(); i++) {
            SessionParticipant participant = participants.get(i);
            EndSessionSummaryResponse summary = summaries.get(i);

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
    public List<EndSessionSummaryResponse> calculateSessionSummary(String sessionId) {
        Session currentSession = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.SESSION_NOT_FOUND));

        List<SessionParticipant> participants = sessionParticipantRepository.findBySession_SessionId(sessionId);
        List<EndSessionSummaryResponse> summaries = new ArrayList<>();

        for (SessionParticipant participant : participants) {
            List<ActivitySubmission> submissions = activitySubmissionRepository
                    .findBySessionParticipant_Session_SessionIdAndActivity_ActivityIdAndIsCorrect(
                            sessionId, null, null);

            int finalScore = submissions.stream()
                    .mapToInt(ActivitySubmission::getResponseScore)
                    .sum();
            int finalCorrectCount = (int) submissions.stream()
                    .filter(ActivitySubmission::getIsCorrect)
                    .count();
            int finalIncorrectCount = submissions.size() - finalCorrectCount;

            EndSessionSummaryResponse summary = EndSessionSummaryResponse.builder()
                    .displayName(participant.getDisplayName())
                    .displayAvatar(participant.getDisplayAvatar())
                    .finalScore(finalScore)
                    .finalCorrectCount(finalCorrectCount)
                    .finalIncorrectCount(finalIncorrectCount)
                    .build();

            summaries.add(summary);
        }

        // Sort and assign ratings
        summaries.sort(Comparator.comparingInt(EndSessionSummaryResponse::getFinalScore).reversed());
        for (int i = 0; i < summaries.size(); i++) {
            summaries.get(i).setFinalRanking(i + 1);
        }

        return summaries;
    }

    @Override
    public String findSessionCodeBySessionId(String sessionId){
        return sessionRepository.findSessionCodeBySessionId(sessionId).orElseThrow(() -> new ApplicationException(ErrorCode.SESSION_NOT_FOUND));
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
