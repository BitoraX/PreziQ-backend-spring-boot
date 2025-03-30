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
    @NotBlank(message = "COLLECTION_ID_REQUIRED")
    String collectionId;

    @Builder.Default
    String activityType = "QUIZ_BUTTONS";

    String title;
    String description;
    Boolean isPublished;
    Integer orderIndex;
    String backgroundColor;
    String backgroundImage;
    String customBackgroundMusic;
}
