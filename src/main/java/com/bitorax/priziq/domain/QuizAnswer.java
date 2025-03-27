package com.bitorax.priziq.domain;

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
    String id;

    @Column(columnDefinition = "TEXT")
    String answerText;

    @Builder.Default
    Boolean isCorrect = false;

    @Column(columnDefinition = "TEXT")
    String explanation;

    @ManyToOne
    @JoinColumn(name = "quiz_id")
    Quiz quiz;
}
