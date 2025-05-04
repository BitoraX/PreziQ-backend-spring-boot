package com.bitorax.priziq.dto.request.session.session_participant;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class GetParticipantsRequest {
    @NotBlank(message = "SESSION_CODE_REQUIRED")
    String sessionCode;
}
