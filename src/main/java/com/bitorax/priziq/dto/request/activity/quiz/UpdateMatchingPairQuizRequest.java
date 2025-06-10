package com.bitorax.priziq.dto.request.activity.quiz;

import lombok.*;
import lombok.experimental.FieldDefaults;
import jakarta.validation.constraints.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
public class UpdateMatchingPairQuizRequest extends UpdateQuizRequest {
    @NotBlank(message = "LEFT_COLUMN_NAME_REQUIRED")
    String leftColumnName;

    @NotBlank(message = "RIGHT_COLUMN_NAME_REQUIRED")
    String rightColumnName;

    @Override
    public String getType() {
        return "MATCHING_PAIRS";
    }
}