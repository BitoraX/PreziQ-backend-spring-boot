package com.bitorax.priziq.dto.request.session.session_participant;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class CreateSessionParticipantRequest {
    @NotBlank(message = "SESSION_ID_REQUIRED")
    String sessionId;

    @NotBlank(message = "USER_ID_REQUIRED")
    String userId;
}