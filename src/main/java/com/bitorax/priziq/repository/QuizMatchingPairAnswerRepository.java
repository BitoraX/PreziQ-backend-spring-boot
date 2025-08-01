package com.bitorax.priziq.repository;

import com.bitorax.priziq.domain.activity.quiz.QuizMatchingPairAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface QuizMatchingPairAnswerRepository extends JpaRepository<QuizMatchingPairAnswer, String>, JpaSpecificationExecutor<QuizMatchingPairAnswer> {
}
