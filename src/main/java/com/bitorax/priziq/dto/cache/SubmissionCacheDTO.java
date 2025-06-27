package com.bitorax.priziq.dto.cache;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.time.Instant;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SubmissionCacheDTO implements Serializable {
    String activitySubmissionId;
    String sessionParticipantId;
    String activityId;
    boolean isCorrect;
    int responseScore;
    Instant createdAt;
}