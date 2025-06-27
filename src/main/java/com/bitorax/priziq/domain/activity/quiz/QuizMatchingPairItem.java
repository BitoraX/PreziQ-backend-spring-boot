package com.bitorax.priziq.domain.activity.quiz;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "quiz_matching_pair_items")
public class QuizMatchingPairItem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String quizMatchingPairItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_matching_pair_answer_id", nullable = false)
    QuizMatchingPairAnswer quizMatchingPairAnswer;

    @Column(nullable = false, columnDefinition = "TEXT")
    String content;

    @Column(nullable = false)
    Boolean isLeftColumn;

    @Column(nullable = false)
    Integer displayOrder;
}