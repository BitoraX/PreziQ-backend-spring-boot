package com.bitorax.priziq.domain.session;

import com.bitorax.priziq.domain.BaseEntity;
import com.bitorax.priziq.domain.User;
import com.bitorax.priziq.domain.archivement.UserAchievement;
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
@Table(name = "session_participants")
public class SessionParticipant extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String sessionParticipantId;

    @ManyToOne
    @JoinColumn(name = "session_id", nullable = false)
    Session session;

    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;

    @OneToMany(mappedBy = "sessionParticipant")
    List<ActivitySubmission> activitySubmissions;

    @Column
    String websocketSessionId;

    @Column
    String displayName;

    @Column(columnDefinition = "TEXT")
    String displayAvatar;

    @Column(nullable = false)
    @Builder.Default
    Integer realtimeScore = 0;

    Integer realtimeRanking;
}
