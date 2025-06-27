package com.bitorax.priziq.dto.cache;

import com.bitorax.priziq.constant.CollectionTopicType;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CollectionCacheDTO {
    String collectionId;
    String creatorId;
    String title;
    String description;
    Boolean isPublished;
    String coverImage;
    String defaultBackgroundMusic;
    CollectionTopicType topic;
}