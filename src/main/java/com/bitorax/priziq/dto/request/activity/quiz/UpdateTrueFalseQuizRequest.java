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
public class UpdateTrueFalseQuizRequest extends UpdateQuizRequest {
    @NotNull(message = "CORRECT_ANSWER_REQUIRED")
    Boolean correctAnswer;
}
