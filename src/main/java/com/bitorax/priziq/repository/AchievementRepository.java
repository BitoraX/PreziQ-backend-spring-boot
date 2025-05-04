package com.bitorax.priziq.repository;

import com.bitorax.priziq.domain.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AchievementRepository extends JpaRepository<Achievement, String>, JpaSpecificationExecutor<Achievement> {
    boolean existsByName(String name);
}
