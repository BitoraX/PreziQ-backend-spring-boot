package com.bitorax.priziq.dto.cache;

import com.bitorax.priziq.constant.SessionStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SessionCacheDTO {
    String sessionId;
    String collectionId;
    String hostUserId;
    String sessionCode;
    String joinSessionQrUrl;
    Instant startTime;
    Instant endTime;
    SessionStatus sessionStatus;
}