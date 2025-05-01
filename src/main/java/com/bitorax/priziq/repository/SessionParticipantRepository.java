package com.bitorax.priziq.repository;

import com.bitorax.priziq.domain.session.SessionParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionParticipantRepository extends JpaRepository<SessionParticipant, String>, JpaSpecificationExecutor<SessionParticipant> {
}
