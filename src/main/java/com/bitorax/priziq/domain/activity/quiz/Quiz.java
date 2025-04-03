package com.bitorax.priziq.domain.activity.quiz;

import com.bitorax.priziq.constant.PointType;
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
    String quizId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "activity_id")
    Activity activity;

    @Column(columnDefinition = "TEXT", nullable = false)
    String questionText;

    @Column(nullable = false)
    @Builder.Default
    Integer timeLimitSeconds = 30;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    PointType pointType;

    @OneToMany(mappedBy = "quiz", fetch = FetchType.LAZY)
    List<QuizAnswer> quizAnswers;
}
