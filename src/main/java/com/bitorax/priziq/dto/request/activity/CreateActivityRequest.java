package com.bitorax.priziq.dto.request.activity;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class CreateActivityRequest {
    String title;
    String description;
    Boolean isPublished;
    Integer orderIndex;
    String backgroundColor;
    String backgroundImage;
    String customBackgroundMusic;

    @NotBlank(message = "COLLECTION_ID_REQUIRED")
    String collectionId;

    @NotBlank(message = "ACTIVITY_TYPE_ID_REQUIRED")
    String activityTypeId;
}
