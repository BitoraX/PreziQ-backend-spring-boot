package com.bitorax.priziq.dto.request.activity.quiz;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.*;
import lombok.experimental.FieldDefaults;
import jakarta.validation.constraints.*;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = UpdateChoiceQuizRequest.class, name = "CHOICE"),
        @JsonSubTypes.Type(value = UpdateReorderQuizRequest.class, name = "REORDER"),
        @JsonSubTypes.Type(value = UpdateTypeAnswerQuizRequest.class, name = "TYPE_ANSWER"),
        @JsonSubTypes.Type(value = UpdateTrueFalseQuizRequest.class, name = "TRUE_FALSE")
})
public abstract class UpdateQuizRequest {
    @NotBlank(message = "QUESTION_TEXT_REQUIRED")
    String questionText;

    @NotNull(message = "TIME_LIMIT_REQUIRED")
    @Positive(message = "TIME_LIMIT_POSITIVE")
    Integer timeLimitSeconds;

    @NotBlank(message = "POINT_TYPE_REQUIRED")
    String pointType;

    public abstract String getType();
}