package com.bitorax.priziq.domain.session;

import com.bitorax.priziq.domain.BaseEntity;
import com.bitorax.priziq.domain.Collection;
import com.bitorax.priziq.domain.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "sessions")
public class Session extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String sessionId;

    @ManyToOne
    @JoinColumn(name = "collection_id", nullable = false)
    Collection collection;

    @ManyToOne
    @JoinColumn(name = "host_user_id", nullable = false)
    User hostUser;

    @OneToMany(mappedBy = "session", fetch = FetchType.LAZY)
    List<SessionParticipant> sessionParticipants;

    @Column(nullable = false, unique = true)
    String sessionCode;

    @Column(nullable = false)
    Instant startTime;

    Instant endTime;

    @Column(nullable = false)
    @Builder.Default
    Boolean isActive = true;
}