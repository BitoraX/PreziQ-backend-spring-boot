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
@Table(name = "activities")
public class Activity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    String title;

    @Column(columnDefinition = "TEXT")
    String description;

    @Builder.Default
    Boolean isPublished = true;

    Integer orderIndex;
    String backgroundColor;
    String backgroundImage;
    String customBackgroundMusic;

    @ManyToOne
    @JoinColumn(name = "collection_id")
    Collection collection;

    @ManyToOne
    @JoinColumn(name = "activity_type_id")
    ActivityType activityType;

    @OneToOne(mappedBy = "activity")
    Quiz quiz;
}
