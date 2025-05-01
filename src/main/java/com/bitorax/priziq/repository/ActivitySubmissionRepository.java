package com.bitorax.priziq.repository;

import com.bitorax.priziq.domain.session.ActivitySubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivitySubmissionRepository extends JpaRepository<ActivitySubmission, String>, JpaSpecificationExecutor<ActivitySubmission> {
}
