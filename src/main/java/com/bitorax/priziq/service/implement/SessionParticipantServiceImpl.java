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
    public SessionParticipantResponse createSessionParticipant(CreateSessionParticipantRequest createSessionParticipantRequest) {
        Session session = sessionRepository.findById(createSessionParticipantRequest.getSessionId())
                .orElseThrow(() -> new ApplicationException(ErrorCode.SESSION_NOT_FOUND));

        User user = userRepository.findById(createSessionParticipantRequest.getUserId())
                .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND));

        SessionParticipant sessionParticipant = SessionParticipant.builder()
                .session(session)
                .user(user)
                .realtimeScore(0)
                .build();

        return sessionParticipantMapper.sessionParticipantToResponse(sessionParticipantRepository.save(sessionParticipant));
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