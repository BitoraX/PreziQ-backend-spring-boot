package com.bitorax.priziq.dto.request.activity.quiz;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class UpdateMatchingPairConnectionRequest {
    @NotBlank(message = "LEFT_ITEM_ID_REQUIRED")
    String leftItemId;

    @NotBlank(message = "RIGHT_ITEM_ID_REQUIRED")
    String rightItemId;
}
