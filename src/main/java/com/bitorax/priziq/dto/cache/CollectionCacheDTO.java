package com.bitorax.priziq.dto.cache;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CollectionCacheDTO implements Serializable {
    String collectionId;
    String title;
    Boolean isPublished;
    String topic;
    List<String> activityIds;
}