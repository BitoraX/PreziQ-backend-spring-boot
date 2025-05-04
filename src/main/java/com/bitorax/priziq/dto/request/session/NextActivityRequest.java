package com.bitorax.priziq.dto.request.session;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class NextActivityRequest {
    @NotBlank(message = "SESSION_ID_REQUIRED")
    String sessionId;

    String activityId; // Current activity ID to determine the next one
}