package com.bitorax.priziq.dto.response.session;

import com.bitorax.priziq.dto.response.common.AuditResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SessionParticipantSummaryResponse extends AuditResponse {
    String sessionParticipantId;

    String displayName;
    String displayAvatar;

    Integer realtimeScore;
    Integer realtimeRanking;
}