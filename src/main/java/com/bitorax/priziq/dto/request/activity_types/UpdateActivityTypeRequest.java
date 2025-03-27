package com.bitorax.priziq.dto.request.activity_types;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class UpdateActivityTypeRequest {
    String name;
    String description;
    String icon;
}
