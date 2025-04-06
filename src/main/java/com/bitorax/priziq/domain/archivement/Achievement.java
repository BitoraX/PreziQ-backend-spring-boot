package com.bitorax.priziq.domain.archivement;

import com.bitorax.priziq.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "achievements")
public class Achievement extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String achievementId;

    @OneToMany(mappedBy = "achievement", fetch = FetchType.LAZY)
    List<UserAchievement> userAchievements;

    @Column(nullable = false)
    String name;

    @Column(columnDefinition = "TEXT")
    String description;

    String icon;

    @Column(nullable = false)
    Integer requiredLevel;

    @Column(nullable = false)
    Integer requiredPoints;
}