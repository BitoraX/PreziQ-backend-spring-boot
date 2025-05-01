package com.bitorax.priziq.mapper;

import com.bitorax.priziq.domain.session.ActivitySubmission;
import com.bitorax.priziq.dto.response.session.ActivitySubmissionResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ActivitySubmissionMapper {
    ActivitySubmissionResponse activitySubmissionToResponse(ActivitySubmission activitySubmission);
}
