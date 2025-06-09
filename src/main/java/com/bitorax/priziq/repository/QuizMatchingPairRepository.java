package com.bitorax.priziq.repository;

import com.bitorax.priziq.domain.activity.quiz.QuizMatchingPair;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface QuizMatchingPairRepository extends JpaRepository<QuizMatchingPair, String>, JpaSpecificationExecutor<QuizMatchingPair> {
}
