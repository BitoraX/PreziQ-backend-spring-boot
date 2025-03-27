package com.bitorax.priziq.dto.request.activity_types;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class CreateActivityTypeRequest {
    @NotBlank(message = "ACTIVITY_TYPE_NAME_NOT_BLANK")
    String name;

    String description;

    String icon;
}
