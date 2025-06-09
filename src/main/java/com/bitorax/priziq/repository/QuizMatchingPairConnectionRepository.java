package com.bitorax.priziq.repository;

import com.bitorax.priziq.domain.activity.quiz.QuizMatchingPairConnection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface QuizMatchingPairConnectionRepository extends JpaRepository<QuizMatchingPairConnection, String>, JpaSpecificationExecutor<QuizMatchingPairConnection> {
}
