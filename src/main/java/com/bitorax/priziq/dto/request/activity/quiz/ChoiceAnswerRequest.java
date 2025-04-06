package com.bitorax.priziq.dto.request.activity.quiz;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
public class ChoiceAnswerRequest {
    @NotBlank(message = "ANSWER_TEXT_REQUIRED")
    String answerText;

    @NotNull(message = "IS_CORRECT_REQUIRED")
    Boolean isCorrect;

    String explanation;
}
