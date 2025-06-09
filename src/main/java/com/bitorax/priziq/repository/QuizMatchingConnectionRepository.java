package com.bitorax.priziq.repository;

import com.bitorax.priziq.domain.activity.quiz.QuizMatchingConnection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface QuizMatchingConnectionRepository extends JpaRepository<QuizMatchingConnection, String>, JpaSpecificationExecutor<QuizMatchingConnection> {
}
