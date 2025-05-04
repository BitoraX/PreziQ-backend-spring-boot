package com.bitorax.priziq.dto.request.achievement;

import jakarta.validation.constraints.Positive;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class UpdateAchievementRequest {
    String name;
    String description;
    String iconUrl;

    @Positive(message = "REQUIRED_POINTS_NOT_NEGATIVE")
    Integer requiredPoints;
}