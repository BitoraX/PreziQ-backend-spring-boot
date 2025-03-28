package com.bitorax.priziq.repository;

import com.bitorax.priziq.domain.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, String>, JpaSpecificationExecutor<Quiz> {
}
