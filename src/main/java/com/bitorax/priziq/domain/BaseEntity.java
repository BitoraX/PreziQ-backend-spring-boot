package com.bitorax.priziq.domain;

import com.bitorax.priziq.utils.SecurityUtils;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Getter
@Setter
@MappedSuperclass
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class BaseEntity {
    @Column(nullable = false, updatable = false)
    Instant createdAt;

    @Column(nullable = false)
    Instant updatedAt;

    @Column(updatable = false)
    String createdBy;

    String updatedBy;

    @Version
    Long version;

    @PrePersist
    protected void onCreate() {
        createdAt = updatedAt = Instant.now();
        createdBy = SecurityUtils.getCurrentUserEmailFromJwt();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
        updatedBy = SecurityUtils.getCurrentUserEmailFromJwt();
    }
}
