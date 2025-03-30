package com.bitorax.priziq.repository;

import com.bitorax.priziq.domain.activity.quiz.QuizAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface QuizAnswerRepository extends JpaRepository<QuizAnswer, String>, JpaSpecificationExecutor<QuizAnswer> {
}
