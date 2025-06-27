package com.bitorax.priziq.dto.cache;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SubmissionCacheDTO {
    String activitySubmissionId;
    String sessionParticipantId;
    String activityId;
    String answerContent;
    Boolean isCorrect;
    Integer responseScore;
}