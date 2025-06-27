package com.bitorax.priziq.dto.cache;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ParticipantCacheDTO implements Serializable {
    String sessionParticipantId;
    String sessionId;
    String userId;
    String displayName;
    int realtimeScore;
    int realtimeRanking;
}