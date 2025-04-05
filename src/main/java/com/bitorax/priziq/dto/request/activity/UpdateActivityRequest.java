package com.bitorax.priziq.dto.request.activity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
public class UpdateActivityRequest {
    String activityType;
    String title;
    String description;
    Boolean isPublished;
    String backgroundColor;
    String backgroundImage;
    String customBackgroundMusic;
}
