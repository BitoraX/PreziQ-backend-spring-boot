package com.bitorax.priziq.dto.request.achievement;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class AssignAchievementToUserRequest {
    @NotBlank(message = "USER_ID_REQUIRED")
    String userId;

    @NotNull(message = "TOTAL_POINTS_REQUIRED")
    @Positive(message = "TOTAL_POINTS_POSITIVE")
    Integer totalPoints;
}
