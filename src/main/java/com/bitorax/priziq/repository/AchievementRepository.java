package com.bitorax.priziq.repository;

import com.bitorax.priziq.domain.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AchievementRepository extends JpaRepository<Achievement, String>, JpaSpecificationExecutor<Achievement> {
    boolean existsByName(String name);

    List<Achievement> findByRequiredPointsLessThanEqual(Integer requiredPoints);
}
