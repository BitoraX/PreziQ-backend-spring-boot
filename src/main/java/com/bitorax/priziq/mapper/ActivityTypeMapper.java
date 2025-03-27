package com.bitorax.priziq.mapper;

import com.bitorax.priziq.domain.ActivityType;
import com.bitorax.priziq.dto.request.activity_types.CreateActivityTypeRequest;
import com.bitorax.priziq.dto.request.activity_types.UpdateActivityTypeRequest;
import com.bitorax.priziq.dto.response.activity_types.ActivityTypeResponse;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ActivityTypeMapper {
    ActivityTypeResponse activityTypeToResponse(ActivityType activityType);

    ActivityType createActivityTypeRequestToActivityType(CreateActivityTypeRequest createActivityTypeRequest);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateActivityTypeRequestToActivityType(@MappingTarget ActivityType activityType, UpdateActivityTypeRequest updateActivityTypeRequest);

    List<ActivityTypeResponse> activityTypeToActivityTypeResponseList(List<ActivityType> activityType);
}
