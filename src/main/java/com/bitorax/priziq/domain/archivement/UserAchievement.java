package com.bitorax.priziq.domain.archivement;

import com.bitorax.priziq.domain.BaseEntity;
import com.bitorax.priziq.domain.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "user_achievements")
@IdClass(UserAchievement.UserAchievementId.class)
public class UserAchievement extends BaseEntity {
    @Id
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @Id
    @ManyToOne
    @JoinColumn(name = "achievement_id", nullable = false)
    Achievement achievement;

    @Getter
    @Setter
    @EqualsAndHashCode
    public static class UserAchievementId implements Serializable {
        String user;
        String achievement;
    }
}