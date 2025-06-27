package com.bitorax.priziq.dto.cache;

import com.bitorax.priziq.constant.ActivityType;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ActivityCacheDTO {
    String activityId;
    String collectionId;
    String title;
    String description;
    Boolean isPublished;
    Integer orderIndex;
    String backgroundColor;
    String backgroundImage;
    ActivityType activityType;
}