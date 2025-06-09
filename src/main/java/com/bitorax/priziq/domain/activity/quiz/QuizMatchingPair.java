package com.bitorax.priziq.domain.activity.quiz;

import com.bitorax.priziq.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
@Table(name = "quiz_matching_pairs")
public class QuizMatchingPair extends BaseEntity {
    @Id
    String quizMatchingPairId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "quiz_id", nullable = false)
    @JsonIgnore
    Quiz quiz;

    @Column
    String leftColumnName;

    @Column
    String rightColumnName;

    @OneToMany(mappedBy = "quizMatchingPair", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    List<QuizMatchingItem> leftItems;

    @OneToMany(mappedBy = "quizMatchingPair", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    List<QuizMatchingItem> rightItems;

    @OneToMany(mappedBy = "quizMatchingPair", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    List<QuizMatchingConnection> connections;
}