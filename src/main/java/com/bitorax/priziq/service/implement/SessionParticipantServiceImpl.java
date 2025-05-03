package com.bitorax.priziq.service.implement;

import com.bitorax.priziq.domain.User;
import com.bitorax.priziq.domain.session.Session;
import com.bitorax.priziq.domain.session.SessionParticipant;
import com.bitorax.priziq.dto.request.session.session_participant.JoinSessionRequest;
import com.bitorax.priziq.dto.request.session.session_participant.LeaveSessionRequest;
import com.bitorax.priziq.dto.response.session.SessionParticipantResponse;
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

import java.util.List;

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
    public List<SessionParticipantResponse> joinSession(JoinSessionRequest request, String websocketSessionId) {
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

        return findParticipantsBySessionCode(session.getSessionCode());
    }

    @Override
    @Transactional
    public List<SessionParticipantResponse> leaveSession(LeaveSessionRequest request, String websocketSessionId) {
        Session session = sessionRepository.findBySessionCode(request.getSessionCode())
                .orElseThrow(() -> new ApplicationException(ErrorCode.SESSION_NOT_FOUND));

        SessionParticipant participant = sessionParticipantRepository
                .findBySessionAndWebsocketSessionId(session, websocketSessionId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.SESSION_PARTICIPANT_NOT_FOUND));

        sessionParticipantRepository.delete(participant);

        return findParticipantsBySessionCode(session.getSessionCode());
    }

    @Override
    public List<SessionParticipantResponse> findParticipantsBySessionCode(String sessionCode){
        return sessionParticipantMapper.sessionParticipantsToResponseList(sessionParticipantRepository.findBySession_SessionCode(sessionCode));
    }
}