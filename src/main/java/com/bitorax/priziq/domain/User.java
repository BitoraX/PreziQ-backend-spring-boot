package com.bitorax.priziq.domain;

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

    @Column(unique = true)
    String email;

    String password;
    String firstName;
    String lastName;
    String nickname;

    @Column(unique = true, nullable = true)
    String phoneNumber;

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

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "role_users", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    @JsonIgnoreProperties(value = { "users" })
    List<Role> roles;
}
