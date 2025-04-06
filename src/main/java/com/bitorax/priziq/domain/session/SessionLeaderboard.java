package com.bitorax.priziq.domain.session;

import com.bitorax.priziq.domain.BaseEntity;
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
@Table(name = "session_leaderboards")
public class SessionLeaderboard extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String sessionLeaderboardId;

    @OneToOne
    @JoinColumn(name = "session_participant_id", nullable = false, unique = true)
    SessionParticipant sessionParticipant;

    @Column(nullable = false)
    Integer finalScore;

    @Column(nullable = false)
    Integer finalRanking;

    @Column(nullable = false)
    Integer finalCorrectCount;

    @Column(nullable = false)
    Integer finalIncorrectCount;
}
