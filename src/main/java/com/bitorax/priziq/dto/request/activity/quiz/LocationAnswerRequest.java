package com.bitorax.priziq.dto.request.activity.quiz;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Positive;
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
    @DecimalMin(value = "-180.0", message = "INVALID_LONGITUDE")
    @DecimalMax(value = "180.0", message = "INVALID_LONGITUDE")
    Double longitude;

    @NotNull(message = "LATITUDE_REQUIRED")
    @DecimalMin(value = "-90.0", message = "INVALID_LATITUDE")
    @DecimalMax(value = "90.0", message = "INVALID_LATITUDE")
    Double latitude;

    @NotNull(message = "RADIUS_REQUIRED")
    @Positive(message = "INVALID_RADIUS")
    Double radius;
}