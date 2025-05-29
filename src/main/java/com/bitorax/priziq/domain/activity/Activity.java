package com.bitorax.priziq.domain.activity;

import com.bitorax.priziq.constant.ActivityType;
import com.bitorax.priziq.domain.BaseEntity;
import com.bitorax.priziq.domain.Collection;
import com.bitorax.priziq.domain.activity.quiz.Quiz;
import com.bitorax.priziq.domain.activity.slide.Slide;
import com.bitorax.priziq.domain.session.ActivitySubmission;
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
@Table(name = "activities")
public class Activity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String activityId;

    @ManyToOne
    @JoinColumn(name = "collection_id")
    @JsonIgnore
    Collection collection;

    @OneToOne(mappedBy = "activity", cascade = CascadeType.ALL)
    Quiz quiz;

    @OneToOne(mappedBy = "activity", cascade = CascadeType.ALL)
    Slide slide;

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
}
