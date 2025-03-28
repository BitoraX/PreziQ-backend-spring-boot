package com.bitorax.priziq.repository;

import com.bitorax.priziq.domain.QuizReorderStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface QuizReorderStepRepository extends JpaRepository<QuizReorderStep, String>, JpaSpecificationExecutor<QuizReorderStep> {
}
