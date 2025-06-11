package com.bitorax.priziq.dto.request.activity.quiz;

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
    Boolean isLeftColumn;

    @Positive(message = "DISPLAY_ORDER_POSITIVE")
    Integer displayOrder;
}