package com.bitorax.priziq.repository;

import com.bitorax.priziq.domain.session.ActivitySubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivitySubmissionRepository extends JpaRepository<ActivitySubmission, String>, JpaSpecificationExecutor<ActivitySubmission> {
    List<ActivitySubmission> findBySessionParticipant_Session_SessionIdAndActivity_ActivityIdAndIsCorrect(String sessionId, String activityId, Boolean isCorrect);

    List<ActivitySubmission> findBySessionParticipant_SessionParticipantId(String participantId);

    List<ActivitySubmission> findBySessionParticipant_Session_SessionId(String sessionId);
}
