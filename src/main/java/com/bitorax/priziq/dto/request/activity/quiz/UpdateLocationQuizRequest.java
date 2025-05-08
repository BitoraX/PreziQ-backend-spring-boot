package com.bitorax.priziq.dto.request.activity.quiz;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
public class UpdateLocationQuizRequest extends UpdateQuizRequest {
    @NotNull(message = "LOCATION_DATA_REQUIRED")
    List<LocationAnswerRequest> locationAnswers;

    @Override
    public String getType() {
        return "LOCATION";
    }
}
