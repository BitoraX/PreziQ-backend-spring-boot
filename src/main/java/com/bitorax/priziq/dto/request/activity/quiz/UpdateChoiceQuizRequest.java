package com.bitorax.priziq.dto.request.activity.quiz;

import lombok.*;
import lombok.experimental.FieldDefaults;
import jakarta.validation.constraints.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
public class UpdateChoiceQuizRequest extends UpdateQuizRequest {
    @NotNull(message = "ANSWERS_REQUIRED")
    @Size(min = 2, message = "MINIMUM_ANSWERS")
    List<ChoiceAnswerRequest> answers;

    @Override
    public String getType() {
        return "CHOICE";
    }
}

