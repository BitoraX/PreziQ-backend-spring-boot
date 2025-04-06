package com.bitorax.priziq.dto.request.activity.slide;

import jakarta.validation.constraints.NotBlank;
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
public class CreateSlideElementRequest {
    @NotBlank(message = "SLIDE_ELEMENT_TYPE_REQUIRED")
    String slideElementType;

    @NotNull(message = "POSITION_X_REQUIRED")
    BigDecimal positionX;

    @NotNull(message = "POSITION_Y_REQUIRED")
    BigDecimal positionY;

    @PositiveOrZero(message = "WIDTH_NON_NEGATIVE")
    BigDecimal width;

    @PositiveOrZero(message = "HEIGHT_NON_NEGATIVE")
    BigDecimal height;

    BigDecimal rotation;

    Integer zIndex;

    String content;

    String sourceUrl;

    String entryAnimation;

    @PositiveOrZero(message = "ENTRY_ANIMATION_DURATION_NON_NEGATIVE")
    BigDecimal entryAnimationDuration;

    @PositiveOrZero(message = "ENTRY_ANIMATION_DELAY_NON_NEGATIVE")
    BigDecimal entryAnimationDelay;

    String exitAnimation;

    @PositiveOrZero(message = "EXIT_ANIMATION_DURATION_NON_NEGATIVE")
    BigDecimal exitAnimationDuration;

    @PositiveOrZero(message = "EXIT_ANIMATION_DELAY_NON_NEGATIVE")
    BigDecimal exitAnimationDelay;
}