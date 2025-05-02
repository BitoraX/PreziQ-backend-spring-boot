package com.bitorax.priziq.service.implement;

import com.bitorax.priziq.domain.User;
import com.bitorax.priziq.domain.session.Session;
import com.bitorax.priziq.domain.session.SessionParticipant;
import com.bitorax.priziq.dto.request.session.session_participant.CreateSessionParticipantRequest;
import com.bitorax.priziq.dto.request.session.session_participant.UpdateSessionParticipantRequest;
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
    public SessionParticipantResponse joinSession(CreateSessionParticipantRequest request) {
        // Find session by sessionCode and validate is active = true
        Session session = sessionRepository.findBySessionCode(request.getSessionCode())
                .orElseThrow(() -> new ApplicationException(ErrorCode.SESSION_NOT_FOUND));

        if (!session.getIsActive()) {
            throw new ApplicationException(ErrorCode.SESSION_NOT_ACTIVE);
        }

        // Check request is user or guest
        User user = null;
        if (request.getUserId() != null) {
            user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND));
        }

        String guestName = request.getGuestName();
        String guestAvatar = request.getGuestAvatar();

        // Check if the player has joined
        boolean alreadyJoined = sessionParticipantRepository.existsBySessionAndUser(session, user)
                || (guestName != null && sessionParticipantRepository.existsBySessionAndGuestName(session, guestName));
        if (alreadyJoined) {
            throw new ApplicationException(ErrorCode.PARTICIPANT_ALREADY_JOINED);
        }

        // Create session participant
        SessionParticipant sessionParticipant = SessionParticipant.builder()
                .session(session)
                .user(user)
                .guestName(user == null ? guestName : null)
                .guestAvatar(user == null ? guestAvatar : null)
                .realtimeScore(0)
                .realtimeRanking(0)
                .build();

        SessionParticipant savedParticipant = sessionParticipantRepository.save(sessionParticipant);
        return sessionParticipantMapper.sessionParticipantToResponse(savedParticipant);
    }

    @Override
    @Transactional
    public SessionParticipantResponse updateSessionParticipantById(String sessionParticipantId, UpdateSessionParticipantRequest updateSessionParticipantRequest) {
        SessionParticipant currentSessionParticipant = sessionParticipantRepository.findById(sessionParticipantId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.SESSION_PARTICIPANT_NOT_FOUND));

        sessionParticipantMapper.updateSessionParticipantFromRequest(updateSessionParticipantRequest, currentSessionParticipant);

        return sessionParticipantMapper.sessionParticipantToResponse(sessionParticipantRepository.save(currentSessionParticipant));
    }
}