package com.bitorax.priziq.dto.request.session.session_participant;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class UpdateSessionParticipantRequest {
    Integer realtimeScore;
    Integer realtimeRanking;
}
