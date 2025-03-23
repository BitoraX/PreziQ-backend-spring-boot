package com.bitorax.priziq.dto.request.collection;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class UpdateCollectionRequest {
    String title;
    String description;
    Boolean isPublished;
    String coverImage;
}
