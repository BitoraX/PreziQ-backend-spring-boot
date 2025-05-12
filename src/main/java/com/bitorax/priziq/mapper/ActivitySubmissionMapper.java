package com.bitorax.priziq.mapper;

import com.bitorax.priziq.domain.session.ActivitySubmission;
import com.bitorax.priziq.domain.session.Session;
import com.bitorax.priziq.dto.response.session.ActivitySubmissionDetailResponse;
import com.bitorax.priziq.dto.response.session.ActivitySubmissionHistoryResponse;
import com.bitorax.priziq.dto.response.session.ActivitySubmissionSummaryResponse;
import com.bitorax.priziq.dto.response.session.SessionDetailResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ActivitySubmissionMapper {
    ActivitySubmissionDetailResponse activitySubmissionToDetailResponse(ActivitySubmission activitySubmission);

    ActivitySubmissionSummaryResponse activitySubmissionToSummaryResponse(ActivitySubmission activitySubmission);

    ActivitySubmissionHistoryResponse activitySubmissionToHistoryResponse(ActivitySubmission activitySubmission);

    List<ActivitySubmissionDetailResponse> activitySubmissionsToDetailResponseList(List<ActivitySubmission> activitySubmissions);

    List<ActivitySubmissionSummaryResponse> activitySubmissionsToSummaryResponseList(List<ActivitySubmission> activitySubmissions);
}
