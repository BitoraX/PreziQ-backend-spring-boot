package com.bitorax.priziq.dto.request.activity.quiz;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class UpdateAndReorderMatchingPairItemRequest {
    String content;

    @NotNull(message = "IS_LEFT_COLUMN_REQUIRED")
    Boolean isLeftColumn;

    @NotNull(message = "DISPLAY_ORDER_REQUIRED")
    @Positive(message = "DISPLAY_ORDER_POSITIVE")
    Integer displayOrder;
}