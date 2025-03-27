package com.bitorax.priziq.mapper;

import com.bitorax.priziq.domain.ActivityType;
import com.bitorax.priziq.dto.request.activity_types.CreateActivityTypeRequest;
import com.bitorax.priziq.dto.request.activity_types.UpdateActivityTypeRequest;
import com.bitorax.priziq.dto.response.activity_types.ActivityTypeResponse;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ActivityTypeMapper {
    ActivityTypeResponse activityTypeToResponse(ActivityType activityType);

    @Mapping(target = "activities", ignore = true)
    ActivityType createActivityTypeRequestToActivityType(CreateActivityTypeRequest createActivityTypeRequest);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "activities", ignore = true)
    void updateActivityTypeRequestToActivityType(@MappingTarget ActivityType activityType, UpdateActivityTypeRequest updateActivityTypeRequest);

    List<ActivityTypeResponse> activityTypeToActivityTypeResponseList(List<ActivityType> activityType);
}
