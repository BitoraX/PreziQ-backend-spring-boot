package com.bitorax.priziq.repository;

import com.bitorax.priziq.domain.User;
import com.bitorax.priziq.domain.session.Session;
import com.bitorax.priziq.domain.session.SessionParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SessionParticipantRepository extends JpaRepository<SessionParticipant, String>, JpaSpecificationExecutor<SessionParticipant> {
    boolean existsBySessionAndUser(Session session, User user);

    boolean existsBySessionAndGuestName(Session session, String guestName);

    List<SessionParticipant> findBySession_SessionCode(String sessionCode);

    Optional<SessionParticipant> findBySessionAndClientSessionId(Session session, String clientSessionId);

    Optional<SessionParticipant> findByClientSessionId(String clientSessionId);
}