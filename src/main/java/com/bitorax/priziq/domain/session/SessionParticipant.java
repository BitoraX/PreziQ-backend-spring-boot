package com.bitorax.priziq.domain.session;

import com.bitorax.priziq.domain.BaseEntity;
import com.bitorax.priziq.domain.User;
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
@Table(name = "session_participants")
public class SessionParticipant extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String sessionParticipantId;

    @ManyToOne
    @JoinColumn(name = "session_id", nullable = false)
    Session session;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @Column(nullable = false)
    @Builder.Default
    Integer realtimeScore = 0;

    Integer realtimeRanking;
}
