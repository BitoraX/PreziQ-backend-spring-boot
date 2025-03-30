package com.bitorax.priziq.dto.response.common;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class ActivityTypeInfo {
    String key;
    String name;
    String description;
    String icon;
}
