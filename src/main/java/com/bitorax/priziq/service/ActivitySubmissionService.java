package com.bitorax.priziq.service;

import com.bitorax.priziq.dto.request.session.activity_submission.CreateActivitySubmissionRequest;
import com.bitorax.priziq.dto.response.session.ActivitySubmissionResponse;

public interface ActivitySubmissionService {
    ActivitySubmissionResponse createActivitySubmission(CreateActivitySubmissionRequest createActivitySubmissionRequest);
}
