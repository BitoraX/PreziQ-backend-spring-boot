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
@Table(name = "matching_items")
public class QuizMatchingItem extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String itemId;

    @ManyToOne
    @JoinColumn(name = "quiz_matching_pair_id", nullable = false)
    @JsonIgnore
    QuizMatchingPair quizMatchingPair;

    @Column
    String text;
}