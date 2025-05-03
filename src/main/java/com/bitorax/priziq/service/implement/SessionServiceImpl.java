package com.bitorax.priziq.service.implement;

import com.bitorax.priziq.domain.Collection;
import com.bitorax.priziq.domain.session.Session;
import com.bitorax.priziq.dto.request.session.CreateSessionRequest;
import com.bitorax.priziq.dto.response.session.SessionResponse;
import com.bitorax.priziq.exception.ApplicationException;
import com.bitorax.priziq.exception.ErrorCode;
import com.bitorax.priziq.mapper.SessionMapper;
import com.bitorax.priziq.repository.CollectionRepository;
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

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SessionServiceImpl implements SessionService {
    SessionRepository sessionRepository;
    CollectionRepository collectionRepository;
    SecurityUtils securityUtils;
    SessionMapper sessionMapper;

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
    public SessionResponse createSession(CreateSessionRequest createSessionRequest){
        Collection currentCollection = collectionRepository.findById(createSessionRequest.getCollectionId()).orElseThrow(() -> new ApplicationException(ErrorCode.COLLECTION_NOT_FOUND));

        Session session = Session.builder()
                .collection(currentCollection)
                .hostUser(securityUtils.getAuthenticatedUser())
                .sessionCode(generateUniqueSessionCode())
                .startTime(Instant.now())
                .isActive(true)
                .build();

        return sessionMapper.sessionToResponse(sessionRepository.save(session));
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
