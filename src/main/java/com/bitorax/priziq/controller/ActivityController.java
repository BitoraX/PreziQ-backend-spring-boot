package com.bitorax.priziq.controller;

import com.bitorax.priziq.dto.request.activity.CreateActivityRequest;
import com.bitorax.priziq.dto.response.activity.ActivityResponse;
import com.bitorax.priziq.dto.response.common.ApiResponse;
import com.bitorax.priziq.service.ActivityService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.bitorax.priziq.utils.MetaUtils.buildMetaInfo;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@RequestMapping("/api/v1/activities")
public class ActivityController {
    ActivityService activityService;

    @PostMapping
    ApiResponse<ActivityResponse> createActivity(@RequestBody @Valid CreateActivityRequest createActivityRequest, HttpServletRequest servletRequest){
        return ApiResponse.<ActivityResponse>builder()
                .message("Activity created successfully")
                .data(activityService.createActivity(createActivityRequest))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }
}
