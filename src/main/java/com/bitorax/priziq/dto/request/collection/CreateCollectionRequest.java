package com.bitorax.priziq.dto.request.collection;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class CreateCollectionRequest {
    @NotBlank(message = "COLLECTION_TITLE_NOT_BLANK")
    String title;

    String description;
    Boolean isPublished;
    String coverImage;
}
