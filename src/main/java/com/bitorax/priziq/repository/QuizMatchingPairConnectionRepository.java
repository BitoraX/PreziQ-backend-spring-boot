package com.bitorax.priziq.repository;

import com.bitorax.priziq.domain.activity.quiz.QuizMatchingPairConnection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface QuizMatchingPairConnectionRepository extends JpaRepository<QuizMatchingPairConnection, String>, JpaSpecificationExecutor<QuizMatchingPairConnection> {
    @Query("SELECT COUNT(c) > 0 FROM QuizMatchingPairConnection c WHERE c.quizMatchingPairAnswer.quiz.quizId = :quizId AND c.leftItem.quizMatchingPairItemId = :leftItemId AND c.rightItem.quizMatchingPairItemId = :rightItemId")
    boolean existsByQuizIdAndLeftItemIdAndRightItemId(String quizId, String leftItemId, String rightItemId);
}
