package com.bitorax.priziq.mapper;

import com.bitorax.priziq.domain.activity.Activity;
import com.bitorax.priziq.dto.request.activity.CreateActivityRequest;
import com.bitorax.priziq.dto.response.activity.ActivityResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ActivityMapper {
    @Mapping(target = "collection", ignore = true)
    @Mapping(target = "activityType", ignore = true)
    Activity createActivityRequestToActivity(CreateActivityRequest createActivityRequest);

    ActivityResponse activityToResponse(Activity activity);
}
