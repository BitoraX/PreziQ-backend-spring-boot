package com.bitorax.priziq.service;

import com.bitorax.priziq.dto.request.session.activity_submission.CreateActivitySubmissionRequest;
import com.bitorax.priziq.dto.response.session.ActivitySubmissionSummaryResponse;

public interface ActivitySubmissionService {
    ActivitySubmissionSummaryResponse createActivitySubmission(CreateActivitySubmissionRequest createActivitySubmissionRequest, String websocketSessionId);
}
