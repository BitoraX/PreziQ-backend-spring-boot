package com.bitorax.priziq.dto.cache;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ParticipantCacheDTO {
    String sessionParticipantId;
    String sessionId;
    String userId;
    String displayName;
    String displayAvatar;
    String websocketSessionId;
    String stompClientId;
    Integer realtimeScore;
    Integer realtimeRanking;
    Boolean isConnected;
}