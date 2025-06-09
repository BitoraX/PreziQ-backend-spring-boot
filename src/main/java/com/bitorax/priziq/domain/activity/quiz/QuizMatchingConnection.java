package com.bitorax.priziq.domain.activity.quiz;

import com.bitorax.priziq.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
@Table(name = "matching_connections")
public class QuizMatchingConnection extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String connectionId;

    @ManyToOne
    @JoinColumn(name = "quiz_matching_pair_id", nullable = false)
    @JsonIgnore
    QuizMatchingPair quizMatchingPair;

    @ManyToOne
    @JoinColumn(name = "left_item_id", nullable = false)
    QuizMatchingItem leftItem;

    @ManyToOne
    @JoinColumn(name = "right_item_id", nullable = false)
    QuizMatchingItem rightItem;

    @Column(nullable = false)
    Boolean isCorrect;
}