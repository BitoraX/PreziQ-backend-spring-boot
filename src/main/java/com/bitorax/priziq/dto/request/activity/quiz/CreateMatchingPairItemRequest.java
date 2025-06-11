package com.bitorax.priziq.dto.request.activity.quiz;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class CreateMatchingPairItemRequest {
    @NotBlank(message = "CONTENT_REQUIRED")
    String content;

    @NotNull(message = "IS_LEFT_COLUMN_REQUIRED")
    Boolean isLeftColumn;
}
