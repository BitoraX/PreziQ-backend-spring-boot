package com.bitorax.priziq.service;

import com.bitorax.priziq.domain.activity.ActivityType;
import com.bitorax.priziq.dto.request.activity_types.CreateActivityTypeRequest;
import com.bitorax.priziq.dto.request.activity_types.UpdateActivityTypeRequest;
import com.bitorax.priziq.dto.response.activity_types.ActivityTypeResponse;
import com.bitorax.priziq.dto.response.common.PaginationResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface ActivityTypeService {
    ActivityTypeResponse createActivityType(CreateActivityTypeRequest createActivityTypeRequest);

    ActivityTypeResponse getActivityTypeById(String collectionId);

    PaginationResponse getAllActivityTypeWithQuery(Specification<ActivityType> spec, Pageable pageable);

    ActivityTypeResponse updateActivityTypeById(String collectionId, UpdateActivityTypeRequest updateActivityTypeRequest);

    void deleteActivityTypeById(String collectionId);
}
