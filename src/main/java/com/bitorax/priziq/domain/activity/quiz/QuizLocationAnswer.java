package com.bitorax.priziq.domain.activity.quiz;

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
@Table(name = "quiz_location_answers")
public class QuizLocationAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String quizLocationAnswerId;

    @ManyToOne
    @JoinColumn(name = "quiz_id", nullable = false)
    @JsonIgnore
    Quiz quiz;

    @Column
    Double longitude;

    @Column
    Double latitude;

    @Column
    Double radius;
}
