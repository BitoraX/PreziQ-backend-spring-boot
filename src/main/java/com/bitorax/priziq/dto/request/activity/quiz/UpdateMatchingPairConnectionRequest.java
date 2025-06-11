package com.bitorax.priziq.dto.request.activity.quiz;

import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class UpdateMatchingPairConnectionRequest {
    @Size(min = 1, message = "LEFT_ITEM_ID_INVALID")
    String leftItemId;

    @Size(min = 1, message = "RIGHT_ITEM_ID_INVALID")
    String rightItemId;
}
