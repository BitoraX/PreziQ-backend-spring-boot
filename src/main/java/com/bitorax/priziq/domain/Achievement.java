package com.bitorax.priziq.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@Table(name = "achievements")
public class Achievement extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String achievementId;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "achievement_users", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "achievement_id"))
    @JsonIgnoreProperties(value = { "users" })
    List<User> users;

    @Column(nullable = false, unique = true)
    String name;

    @Column(columnDefinition = "TEXT")
    String description;

    @Column(columnDefinition = "TEXT")
    String iconUrl;

    @Column(nullable = false)
    Integer requiredPoints;
}
