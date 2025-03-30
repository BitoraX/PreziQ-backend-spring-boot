package com.bitorax.priziq.domain.activity.quiz;

import com.bitorax.priziq.domain.BaseEntity;
import com.bitorax.priziq.domain.activity.Activity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "quizzes")
public class Quiz extends BaseEntity {
    @Id
    String id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "activity_id")
    Activity activity;

    @Column(columnDefinition = "TEXT")
    String questionText;

    @Builder.Default
    Integer timeLimitSeconds = 30;

    @Builder.Default
    String pointType = "STANDARD";

    @OneToMany(mappedBy = "quiz", fetch = FetchType.LAZY)
    List<QuizAnswer> quizAnswers;

    @OneToMany(mappedBy = "quiz", fetch = FetchType.LAZY)
    List<QuizReorderStep> quizReorderSteps;
}
