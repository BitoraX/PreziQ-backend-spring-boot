package com.bitorax.priziq.controller;

import com.bitorax.priziq.constant.ActivityType;
import com.bitorax.priziq.dto.request.activity.CreateActivityRequest;
import com.bitorax.priziq.dto.request.activity.UpdateActivityRequest;
import com.bitorax.priziq.dto.request.activity.quiz.UpdateQuizRequest;
import com.bitorax.priziq.dto.response.activity.ActivityResponse;
import com.bitorax.priziq.dto.response.activity.quiz.QuizResponse;
import com.bitorax.priziq.dto.response.common.ActivityTypeInfo;
import com.bitorax.priziq.dto.response.common.ApiResponse;
import com.bitorax.priziq.service.ActivityService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/types")
    ApiResponse<List<ActivityTypeInfo>> getAllActivityTypes(HttpServletRequest servletRequest){
        return ApiResponse.<List<ActivityTypeInfo>>builder()
                .message("Retrieved the list of activity types successfully")
                .data(ActivityType.getAllTypes())
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @PutMapping("/{activityId}/quiz")
    ApiResponse<QuizResponse> updateQuiz(@PathVariable String activityId, @RequestBody @Valid UpdateQuizRequest updateQuizRequest, HttpServletRequest servletRequest) {
        return ApiResponse.<QuizResponse>builder()
                .message("Quiz updated successfully")
                .data(activityService.updateQuiz(activityId, updateQuizRequest))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @DeleteMapping("/{activityId}")
    ApiResponse<Void> deleteActivity(@PathVariable String activityId, HttpServletRequest servletRequest) {
        activityService.deleteActivity(activityId);
        return ApiResponse.<Void>builder()
                .message("Activity deleted successfully")
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @PutMapping("/{activityId}")
    ApiResponse<ActivityResponse> updateActivity(@PathVariable String activityId, @RequestBody @Valid UpdateActivityRequest updateActivityRequest, HttpServletRequest servletRequest) {
        return ApiResponse.<ActivityResponse>builder()
                .message("Activity updated successfully")
                .data(activityService.updateActivity(activityId, updateActivityRequest))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }
}
