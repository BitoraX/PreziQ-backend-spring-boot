package com.bitorax.priziq.dto.request.activity.quiz;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
public class LocationAnswerRequest {
    @NotNull(message = "LONGITUDE_REQUIRED")
    Double longitude;

    @NotNull(message = "LATITUDE_REQUIRED")
    Double latitude;

    @NotNull(message = "RADIUS_REQUIRED")
    Double radius;
}
