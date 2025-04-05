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
public class UpdateReorderQuizRequest extends UpdateQuizRequest {
    @NotNull(message = "CORRECT_ORDER_REQUIRED")
    @Size(min = 1, message = "MINIMUM_CORRECT_ORDER")
    List<String> correctOrder;
}