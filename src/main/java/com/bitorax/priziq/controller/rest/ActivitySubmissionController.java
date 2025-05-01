package com.bitorax.priziq.controller.rest;

import com.bitorax.priziq.dto.request.session.activity_submission.CreateActivitySubmissionRequest;
import com.bitorax.priziq.dto.response.common.ApiResponse;
import com.bitorax.priziq.dto.response.session.ActivitySubmissionResponse;
import com.bitorax.priziq.service.ActivitySubmissionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import static com.bitorax.priziq.utils.MetaUtils.buildMetaInfo;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@RequestMapping("/api/v1/activity-submissions")
public class ActivitySubmissionController {
    ActivitySubmissionService activitySubmissionService;

    @PostMapping
    public ApiResponse<ActivitySubmissionResponse> createActivitySubmission(@RequestBody @Valid CreateActivitySubmissionRequest createActivitySubmissionRequest, HttpServletRequest servletRequest) {
        return ApiResponse.<ActivitySubmissionResponse>builder()
                .message("Activity submission created successfully")
                .data(activitySubmissionService.createActivitySubmission(createActivitySubmissionRequest))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }
}
