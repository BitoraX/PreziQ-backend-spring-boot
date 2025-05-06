package com.bitorax.priziq.dto.request.session.activity_submission;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class CreateActivitySubmissionRequest {
    @NotBlank(message = "SESSION_CODE_REQUIRED")
    String sessionCode;

    @NotBlank(message = "ACTIVITY_ID_REQUIRED")
    String activityId;

    @NotBlank(message = "ANSWER_CONTENT_REQUIRED")
    String answerContent;
}
