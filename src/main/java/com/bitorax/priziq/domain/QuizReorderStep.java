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
@Table(name = "quiz_reorder_steps")
public class QuizReorderStep extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    Integer stepOrder;

    @Column(columnDefinition = "TEXT")
    String stepText;

    @Column(columnDefinition = "TEXT")
    String explanation;

    @ManyToOne
    @JoinColumn(name = "quiz_id")
    Quiz quiz;
}
