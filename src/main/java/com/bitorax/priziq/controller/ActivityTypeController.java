package com.bitorax.priziq.controller;

import com.bitorax.priziq.domain.activity.ActivityType;
import com.bitorax.priziq.dto.request.activity_types.CreateActivityTypeRequest;
import com.bitorax.priziq.dto.request.activity_types.UpdateActivityTypeRequest;
import com.bitorax.priziq.dto.response.activity_types.ActivityTypeResponse;
import com.bitorax.priziq.dto.response.common.ApiResponse;
import com.bitorax.priziq.dto.response.common.PaginationResponse;
import com.bitorax.priziq.service.ActivityTypeService;
import com.turkraft.springfilter.boot.Filter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;

import static com.bitorax.priziq.utils.MetaUtils.buildMetaInfo;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@RequestMapping("/api/v1/activity-types")
public class ActivityTypeController {
    ActivityTypeService activityTypeService;

    @PostMapping
    ApiResponse<ActivityTypeResponse> createActivityType(@RequestBody @Valid CreateActivityTypeRequest createActivityTypeRequest, HttpServletRequest servletRequest) {
        return ApiResponse.<ActivityTypeResponse>builder()
                .message("Activity type created successfully")
                .data(activityTypeService.createActivityType(createActivityTypeRequest))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @PatchMapping("/{id}")
    ApiResponse<ActivityTypeResponse> updateActivityTypeById(@RequestBody UpdateActivityTypeRequest updateActivityTypeRequest, @PathVariable("id") String activityTypeId, HttpServletRequest servletRequest) {
        return ApiResponse.<ActivityTypeResponse>builder()
                .message("Activity type updated successfully")
                .data(activityTypeService.updateActivityTypeById(activityTypeId, updateActivityTypeRequest))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @GetMapping("/{id}")
    ApiResponse<ActivityTypeResponse> getActivityTypeById(@PathVariable("id") String activityTypeId, HttpServletRequest servletRequest) {
        return ApiResponse.<ActivityTypeResponse>builder()
                .message("Activity type retrieved successfully")
                .data(activityTypeService.getActivityTypeById(activityTypeId))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @GetMapping
    ApiResponse<PaginationResponse> getAllActivityTypeWithQuery(@Filter Specification<ActivityType> spec, Pageable pageable, HttpServletRequest servletRequest) {
        return ApiResponse.<PaginationResponse>builder()
                .message("Activity types retrieved successfully with query filters")
                .data(activityTypeService.getAllActivityTypeWithQuery(spec, pageable))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @DeleteMapping("/{id}")
    ApiResponse<Void> deleteActivityTypeById(@PathVariable("id") String activityTypeId, HttpServletRequest servletRequest) {
        activityTypeService.deleteActivityTypeById(activityTypeId);
        return ApiResponse.<Void>builder()
                .message("Activity type deleted successfully")
                .meta(buildMetaInfo(servletRequest))
                .build();
    }
}