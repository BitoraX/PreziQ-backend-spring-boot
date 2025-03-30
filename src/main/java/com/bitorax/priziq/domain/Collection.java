package com.bitorax.priziq.domain;

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
@Table(name = "collections")
public class Collection extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    String title;

    @Column(columnDefinition = "TEXT")
    String description;

    @Builder.Default
    Boolean isPublished = false;

    String coverImage;
    String defaultBackgroundMusic;

    @OneToMany(mappedBy = "collection", fetch = FetchType.LAZY)
    List<Activity> activities;
}
