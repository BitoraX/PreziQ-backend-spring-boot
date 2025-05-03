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
    public List<SessionParticipantResponse> joinSession(JoinSessionRequest request, String clientSessionId) {
        Session session = sessionRepository.findBySessionCode(request.getSessionCode())
                .orElseThrow(() -> new ApplicationException(ErrorCode.SESSION_NOT_FOUND));

        if (!session.getIsActive()) {
            throw new ApplicationException(ErrorCode.SESSION_NOT_ACTIVE);
        }

        User user = null;
        if (request.getUserId() != null) {
            user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND));
        }

        String guestName = request.getGuestName();
        String guestAvatar = request.getGuestAvatar();

        if (guestName != null) {
            guestName = guestName.trim();
            if (guestName.isEmpty()) {
                throw new ApplicationException(ErrorCode.INVALID_GUEST_NAME);
            }
        }

        if (user != null) {
            boolean alreadyJoined = sessionParticipantRepository.existsBySessionAndUser(session, user);
            if (alreadyJoined) {
                throw new ApplicationException(ErrorCode.PARTICIPANT_ALREADY_JOINED);
            }
        } else if (guestName != null) {
            boolean alreadyJoined = sessionParticipantRepository.existsBySessionAndGuestName(session, guestName);
            if (alreadyJoined) {
                throw new ApplicationException(ErrorCode.PARTICIPANT_ALREADY_JOINED);
            }
        } else {
            throw new ApplicationException(ErrorCode.USER_OR_GUEST_REQUIRED);
        }

        SessionParticipant sessionParticipant = SessionParticipant.builder()
                .session(session)
                .user(user)
                .guestName(user == null ? guestName : null)
                .guestAvatar(user == null ? guestAvatar : null)
                .clientSessionId(clientSessionId)
                .realtimeScore(0)
                .realtimeRanking(0)
                .build();

        sessionParticipantRepository.save(sessionParticipant);

        List<SessionParticipant> participants = sessionParticipantRepository.findBySession_SessionCode(session.getSessionCode());
        return participants.stream()
                .map(sessionParticipantMapper::sessionParticipantToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<SessionParticipantResponse> leaveSession(LeaveSessionRequest request, String clientSessionId) {
        Session session = sessionRepository.findBySessionCode(request.getSessionCode())
                .orElseThrow(() -> new ApplicationException(ErrorCode.SESSION_NOT_FOUND));

        SessionParticipant participant = sessionParticipantRepository
                .findBySessionAndClientSessionId(session, clientSessionId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.SESSION_PARTICIPANT_NOT_FOUND));

        sessionParticipantRepository.delete(participant);

        List<SessionParticipant> participants = sessionParticipantRepository.findBySession_SessionCode(session.getSessionCode());
        return participants.stream()
                .map(sessionParticipantMapper::sessionParticipantToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<SessionParticipantResponse> findParticipantsBySessionCode(String sessionCode){
        return sessionParticipantMapper.sessionParticipantsToResponseList(sessionParticipantRepository.findBySession_SessionCode(sessionCode));
    }
}