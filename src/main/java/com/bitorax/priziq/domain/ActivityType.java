package com.bitorax.priziq.domain;

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
@Table(name = "activity_types")
public class ActivityType extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    String name;

    @Column(columnDefinition = "TEXT")
    String description;

    String icon;

    @OneToMany(mappedBy = "activityType", fetch = FetchType.LAZY)
    List<Activity> activities;
}
