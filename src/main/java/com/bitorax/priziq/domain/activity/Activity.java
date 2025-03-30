package com.bitorax.priziq.domain.activity;

import com.bitorax.priziq.constant.ActivityType;
import com.bitorax.priziq.domain.BaseEntity;
import com.bitorax.priziq.domain.Collection;
import com.bitorax.priziq.domain.activity.quiz.Quiz;
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
@Table(name = "activities")
public class Activity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    ActivityType activityType;

    String title;

    @Column(columnDefinition = "TEXT")
    String description;

    @Column(nullable = false)
    @Builder.Default
    Boolean isPublished = true;

    @Column(nullable = false)
    Integer orderIndex;

    String backgroundColor;
    String backgroundImage;
    String customBackgroundMusic;

    @ManyToOne
    @JoinColumn(name = "collection_id")
    Collection collection;

    @OneToOne(mappedBy = "activity")
    Quiz quiz;
}
