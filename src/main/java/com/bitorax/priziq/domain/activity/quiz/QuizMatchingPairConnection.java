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
@Table(name = "quiz_matching_pair_connections")
public class QuizMatchingPairConnection {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String quizMatchingPairConnectionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_matching_pair_answer_id", nullable = false)
    QuizMatchingPairAnswer quizMatchingPairAnswer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "left_item_id", nullable = false)
    QuizMatchingPairItem leftItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "right_item_id", nullable = false)
    QuizMatchingPairItem rightItem;
}