package com.bitorax.priziq.service.implement;

import com.bitorax.priziq.domain.User;
import com.bitorax.priziq.domain.session.Session;
import com.bitorax.priziq.domain.session.SessionParticipant;
import com.bitorax.priziq.dto.request.session.session_participant.GetParticipantsRequest;
import com.bitorax.priziq.dto.request.session.session_participant.JoinSessionRequest;
import com.bitorax.priziq.dto.request.session.session_participant.LeaveSessionRequest;
import com.bitorax.priziq.dto.response.session.SessionParticipantSummaryResponse;
import com.bitorax.priziq.exception.ApplicationException;
import com.bitorax.priziq.exception.ErrorCode;
import com.bitorax.priziq.mapper.SessionParticipantMapper;
import com.bitorax.priziq.repository.SessionParticipantRepository;
import com.bitorax.priziq.repository.SessionRepository;
import com.bitorax.priziq.repository.UserRepository;
import com.bitorax.priziq.service.SessionParticipantService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SessionParticipantServiceImpl implements SessionParticipantService {
    SessionParticipantRepository sessionParticipantRepository;
    SessionRepository sessionRepository;
    UserRepository userRepository;
    SessionParticipantMapper sessionParticipantMapper;

    @Override
    @Transactional
    public List<SessionParticipantSummaryResponse> joinSession(JoinSessionRequest request, String websocketSessionId) {
        Session session = sessionRepository.findBySessionCode(request.getSessionCode())
                .orElseThrow(() -> new ApplicationException(ErrorCode.SESSION_NOT_FOUND));

        if (!session.getIsActive()) {
            throw new ApplicationException(ErrorCode.SESSION_NOT_ACTIVE);
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
                .realtimeScore(0)
                .realtimeRanking(0)
                .build();

        sessionParticipantRepository.save(sessionParticipant);

        return findParticipantsBySessionCode(GetParticipantsRequest.builder()
                .sessionCode(session.getSessionCode())
                .build());
    }

    @Override
    @Transactional
    public List<SessionParticipantSummaryResponse> leaveSession(LeaveSessionRequest request, String websocketSessionId) {
        Session session = sessionRepository.findBySessionCode(request.getSessionCode())
                .orElseThrow(() -> new ApplicationException(ErrorCode.SESSION_NOT_FOUND));

        SessionParticipant participant = sessionParticipantRepository
                .findBySessionAndWebsocketSessionId(session, websocketSessionId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.SESSION_PARTICIPANT_NOT_FOUND));

        sessionParticipantRepository.delete(participant);

        return findParticipantsBySessionCode(GetParticipantsRequest.builder()
                .sessionCode(session.getSessionCode())
                .build());
    }

    @Override
    public List<SessionParticipantSummaryResponse> findParticipantsBySessionCode(GetParticipantsRequest request){
        return sessionParticipantMapper.sessionParticipantsToSummaryResponseList(sessionParticipantRepository.findBySession_SessionCode(request.getSessionCode()));
    }

    @Override
    @Transactional
    public List<SessionParticipantSummaryResponse> updateRealtimeScoreAndRanking(String sessionId, String websocketSessionId, int responseScore) {
        Session session = sessionRepository.findById(sessionId)
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
        List<SessionParticipant> participants = sessionParticipantRepository.findBySession_SessionId(sessionId);
        // Sort by realtimeScore (descending) and assign rankings
        List<SessionParticipant> sortedParticipants = participants.stream()
                .sorted(Comparator.comparingInt(SessionParticipant::getRealtimeScore).reversed())
                .collect(Collectors.toList());

        // Update realtimeRanking
        for (int i = 0; i < sortedParticipants.size(); i++) {
            sortedParticipants.get(i).setRealtimeRanking(i + 1);
        }

        // Save all participants with updated rankings
        sessionParticipantRepository.saveAll(sortedParticipants);

        // Return updated participant list
        return sessionParticipantMapper.sessionParticipantsToSummaryResponseList(participants);
    }
}