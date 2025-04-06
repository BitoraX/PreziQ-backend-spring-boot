package com.bitorax.priziq.dto.request.activity;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class UpdateActivityRequest {
    String activityType;
    String title;
    String description;
    Boolean isPublished;
    String backgroundColor;
    String backgroundImage;
    String customBackgroundMusic;
}
