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
public class CreateAchievementRequest {
    @NotBlank(message = "ACHIEVEMENT_NAME_NOT_BLANK")
    String name;

    String description;
    String iconUrl;

    @NotNull(message = "REQUIRED_POINTS_NOT_NULL")
    @Positive(message = "REQUIRED_POINTS_NOT_NEGATIVE")
    Integer requiredPoints;
}