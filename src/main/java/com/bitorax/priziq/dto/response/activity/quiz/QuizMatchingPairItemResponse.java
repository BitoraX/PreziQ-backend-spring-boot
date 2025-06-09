package com.bitorax.priziq.dto.response.activity.quiz;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuizMatchingPairItemResponse {
    String quizMatchingPairItemId;
    String content;
    Boolean isLeftColumn;
    Integer orderIndex;
}
