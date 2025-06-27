package com.bitorax.priziq.dto.cache;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SessionCacheDTO implements Serializable {
    String sessionId;
    String sessionCode;
    String collectionId;
    String hostUserId;
    String sessionStatus;
    Instant startTime;
    Instant endTime;
    List<String> participantIds;
}