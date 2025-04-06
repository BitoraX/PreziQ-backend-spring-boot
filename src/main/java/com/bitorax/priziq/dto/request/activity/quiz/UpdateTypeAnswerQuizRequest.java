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
public class UpdateTypeAnswerQuizRequest extends UpdateQuizRequest {
    @NotBlank(message = "CORRECT_ANSWER_REQUIRED")
    String correctAnswer;

    @Override
    public String getType() {
        return "TYPE_ANSWER";
    }
}
