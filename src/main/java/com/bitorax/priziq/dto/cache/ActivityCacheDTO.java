package com.bitorax.priziq.dto.cache;

import com.bitorax.priziq.constant.ActivityType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ActivityCacheDTO implements Serializable {
    String activityId;
    String title;
    Boolean isPublished;
    ActivityType activityType;
    Integer orderIndex;
}