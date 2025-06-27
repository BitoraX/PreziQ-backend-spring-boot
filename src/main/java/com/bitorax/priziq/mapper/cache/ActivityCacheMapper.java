package com.bitorax.priziq.mapper.cache;

import com.bitorax.priziq.domain.activity.Activity;
import com.bitorax.priziq.dto.cache.ActivityCacheDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ActivityCacheMapper {
    @Mapping(source = "collection.collectionId", target = "collectionId")
    ActivityCacheDTO activityToCacheDTO(Activity activity);

    @Mapping(source = "collectionId", target = "collection.collectionId")
    Activity activityCacheDTOToActivity(ActivityCacheDTO activityCacheDTO);
}
