package com.bitorax.priziq.repository;

import com.bitorax.priziq.domain.activity.ActivityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivityTypeRepository extends JpaRepository<ActivityType, String>, JpaSpecificationExecutor<ActivityType> {
    boolean existsByName(String name);
}
