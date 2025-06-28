package com.bitorax.priziq.repository;

import com.bitorax.priziq.constant.SessionStatus;
import com.bitorax.priziq.domain.session.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session, String>, JpaSpecificationExecutor<Session> {
    Optional<Session> findBySessionCode(String sessionCode);

    @Query("SELECT s.sessionCode FROM Session s WHERE s.sessionId = :sessionId")
    Optional<String> findSessionCodeBySessionId(@Param("sessionId") String sessionId);

    List<Session> findBySessionStatusAndStartTimeBefore(SessionStatus sessionStatus, Instant minus);

    void deleteByCollectionCollectionId(String collectionId);

    List<Session> findByCollectionCollectionId(String collectionId);
}
