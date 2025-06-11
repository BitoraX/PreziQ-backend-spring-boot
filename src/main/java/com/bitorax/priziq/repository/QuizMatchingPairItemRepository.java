package com.bitorax.priziq.repository;

import com.bitorax.priziq.domain.activity.quiz.QuizMatchingPairAnswer;
import com.bitorax.priziq.domain.activity.quiz.QuizMatchingPairItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QuizMatchingPairItemRepository extends JpaRepository<QuizMatchingPairItem, String>, JpaSpecificationExecutor<QuizMatchingPairItem> {
    @Query("SELECT COALESCE(MAX(i.displayOrder), 0) FROM QuizMatchingPairItem i WHERE i.quizMatchingPairAnswer = :answer AND i.isLeftColumn = :isLeftColumn")
    Optional<Integer> findMaxDisplayOrderByQuizMatchingPairAnswerAndIsLeftColumn(QuizMatchingPairAnswer answer, Boolean isLeftColumn);
}