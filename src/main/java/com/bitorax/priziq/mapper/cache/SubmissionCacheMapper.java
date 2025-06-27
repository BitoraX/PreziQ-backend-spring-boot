package com.bitorax.priziq.mapper.cache;

import com.bitorax.priziq.domain.session.ActivitySubmission;
import com.bitorax.priziq.dto.cache.SubmissionCacheDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SubmissionCacheMapper {
    @Mapping(source = "sessionParticipant.sessionParticipantId", target = "sessionParticipantId")
    @Mapping(source = "activity.activityId", target = "activityId")
    SubmissionCacheDTO activitySubmissionToCacheDTO(ActivitySubmission activitySubmission);

    @Mapping(source = "sessionParticipantId", target = "sessionParticipant.sessionParticipantId")
    @Mapping(source = "activityId", target = "activity.activityId")
    ActivitySubmission submissionCacheDTOToActivitySubmission(SubmissionCacheDTO submissionCacheDTO);
}
