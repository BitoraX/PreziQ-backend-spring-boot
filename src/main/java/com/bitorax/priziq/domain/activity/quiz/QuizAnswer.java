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
@Table(name = "quiz_answers")
public class QuizAnswer extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String quizAnswerId;

    @ManyToOne
    @JoinColumn(name = "quiz_id", nullable = false)
    @JsonIgnore
    Quiz quiz;

    @Column(columnDefinition = "TEXT", nullable = false)
    String answerText;

    @Column(nullable = false)
    @Builder.Default
    Boolean isCorrect = false;

    @Column(columnDefinition = "TEXT")
    String explanation;

    @Column(nullable = false)
    Integer orderIndex;
}
