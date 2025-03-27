package com.bitorax.priziq.repository;

import com.bitorax.priziq.domain.ActivityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ActivityTypeRepository extends JpaRepository<ActivityType, String>, JpaSpecificationExecutor<ActivityType> {
    boolean existsByName(String name);
}
