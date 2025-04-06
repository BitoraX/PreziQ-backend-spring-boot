package com.bitorax.priziq.dto.request.activity.slide;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class UpdateSlideRequest {
    String transitionEffect;

    @NotNull(message = "TRANSITION_DURATION_REQUIRED")
    @PositiveOrZero(message = "TRANSITION_DURATION_NON_NEGATIVE")
    BigDecimal transitionDuration;

    @NotNull(message = "AUTO_ADVANCE_SECONDS_REQUIRED")
    @PositiveOrZero(message = "AUTO_ADVANCE_SECONDS_NON_NEGATIVE")
    Integer autoAdvanceSeconds;
}