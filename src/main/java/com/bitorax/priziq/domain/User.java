package com.bitorax.priziq.domain;

import com.bitorax.priziq.domain.archivement.UserAchievement;
import com.bitorax.priziq.domain.session.ActivitySubmission;
import com.bitorax.priziq.domain.session.Session;
import com.bitorax.priziq.domain.session.SessionParticipant;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@Table(name = "users")
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String userId;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "role_users", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    @JsonIgnoreProperties(value = { "users" })
    List<Role> roles;

    @OneToMany(mappedBy = "creator", fetch = FetchType.LAZY)
    List<Collection> collections;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    List<SessionParticipant> sessionParticipants;

    @OneToMany(mappedBy = "hostUser", fetch = FetchType.LAZY)
    List<Session> sessions;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    List<ActivitySubmission> activitySubmissions;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    List<UserAchievement> userAchievements;

    @Column(unique = true)
    String email;

    String password;
    String firstName;
    String lastName;
    String nickname;

    @Column(unique = true)
    String phoneNumber;

    @Column(columnDefinition = "TEXT")
    String avatar;

    Instant birthDate;
    String gender;
    String nationality;

    @Column(columnDefinition = "TEXT")
    String refreshToken;

    String otpCode;
    Instant otpExpiration;

    @Column(nullable = false)
    @Builder.Default
    String provider = "basic";

    String providerUserId;

    @Column(nullable = false)
    @Builder.Default
    Boolean isVerified = false;

    @Builder.Default
    Integer totalPoints = 0;

    @Builder.Default
    Integer level = 0;
}
