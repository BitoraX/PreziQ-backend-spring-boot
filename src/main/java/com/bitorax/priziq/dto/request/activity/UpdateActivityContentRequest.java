package com.bitorax.priziq.dto.request.activity;

import com.bitorax.priziq.domain.activity.quiz.QuizReorderStep;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class UpdateActivityContentRequest {
    @NotBlank(message = "ACTIVITY_ID_REQUIRED")
    String activityId;

    String questionText;
    Integer timeLimitSeconds;
    String pointType;

    List<String> quizAnswers;

    List<QuizReorderStep> quizReorderSteps;
}
