package com.bitorax.priziq.dto.response.activity.quiz;

import com.bitorax.priziq.dto.response.common.AuditResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuizMatchingPairAnswerResponse extends AuditResponse {
    String quizMatchingPairAnswerId;

    String leftColumnName;
    String rightColumnName;

    List<QuizMatchingPairItemResponse> items;
    List<QuizMatchingPairConnectionResponse> connections;
}
