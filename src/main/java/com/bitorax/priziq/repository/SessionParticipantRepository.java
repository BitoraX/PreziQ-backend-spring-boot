package com.bitorax.priziq.repository;

import com.bitorax.priziq.domain.session.Session;
import com.bitorax.priziq.domain.session.SessionParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SessionParticipantRepository extends JpaRepository<SessionParticipant, String>, JpaSpecificationExecutor<SessionParticipant> {
    List<SessionParticipant> findBySession_SessionCode(String sessionCode);

    List<SessionParticipant> findBySession_SessionId(String sessionId);

    Optional<SessionParticipant> findBySessionAndWebsocketSessionId(Session session, String websocketSessionId);

    Optional<SessionParticipant> findByWebsocketSessionId(String websocketSessionId);

    List<SessionParticipant> findBySession_SessionCodeAndIsConnectedTrue(String sessionCode);
}