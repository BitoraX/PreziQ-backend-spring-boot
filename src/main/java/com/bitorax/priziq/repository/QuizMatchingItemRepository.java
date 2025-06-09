package com.bitorax.priziq.repository;

import com.bitorax.priziq.domain.activity.quiz.QuizMatchingItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface QuizMatchingItemRepository extends JpaRepository<QuizMatchingItem, String>, JpaSpecificationExecutor<QuizMatchingItem> {
}