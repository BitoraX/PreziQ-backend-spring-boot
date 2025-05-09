package com.bitorax.priziq.controller.rest;

import com.bitorax.priziq.domain.Achievement;
import com.bitorax.priziq.dto.request.achievement.CreateAchievementRequest;
import com.bitorax.priziq.dto.request.achievement.UpdateAchievementRequest;
import com.bitorax.priziq.dto.response.achievement.AchievementDetailResponse;
import com.bitorax.priziq.dto.response.achievement.AchievementSummaryResponse;
import com.bitorax.priziq.dto.response.common.ApiResponse;
import com.bitorax.priziq.dto.response.common.PaginationResponse;
import com.bitorax.priziq.service.AchievementService;
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
@RequestMapping("/api/v1/achievements")
public class AchievementController {

    AchievementService achievementService;

    @PostMapping
    ApiResponse<AchievementSummaryResponse> createAchievement(
            @RequestBody @Valid CreateAchievementRequest createAchievementRequest,
            HttpServletRequest servletRequest
    ) {
        return ApiResponse.<AchievementSummaryResponse>builder()
                .message("Achievement created successfully")
                .data(achievementService.createAchievement(createAchievementRequest))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @PatchMapping("/{achievementId}")
    ApiResponse<AchievementSummaryResponse> updateAchievementById(
            @RequestBody @Valid UpdateAchievementRequest updateAchievementRequest,
            @PathVariable String achievementId,
            HttpServletRequest servletRequest
    ) {
        return ApiResponse.<AchievementSummaryResponse>builder()
                .message("Achievement updated successfully")
                .data(achievementService.updateAchievementById(achievementId, updateAchievementRequest))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @GetMapping("/{achievementId}")
    ApiResponse<AchievementDetailResponse> getAchievementById(
            @PathVariable String achievementId,
            HttpServletRequest servletRequest
    ) {
        return ApiResponse.<AchievementDetailResponse>builder()
                .message("Achievement retrieved successfully")
                .data(achievementService.getAchievementById(achievementId))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @GetMapping
    ApiResponse<PaginationResponse> getAllAchievementsWithQuery(
            @Filter Specification<Achievement> spec,
            Pageable pageable,
            HttpServletRequest servletRequest
    ) {
        return ApiResponse.<PaginationResponse>builder()
                .message("Achievements retrieved successfully with query filters")
                .data(achievementService.getAllAchievementsWithQuery(spec, pageable))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @GetMapping("/me")
    ApiResponse<PaginationResponse> getMyAchievements(
            @Filter Specification<Achievement> spec,
            Pageable pageable,
            HttpServletRequest servletRequest
    ) {
        return ApiResponse.<PaginationResponse>builder()
                .message("My achievements retrieved successfully")
                .data(achievementService.getMyAchievements(spec, pageable))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @DeleteMapping("/{achievementId}")
    ApiResponse<Void> deleteAchievementById(
            @PathVariable String achievementId,
            HttpServletRequest servletRequest
    ) {
        achievementService.deleteAchievementById(achievementId);
        return ApiResponse.<Void>builder()
                .message("Achievement deleted successfully")
                .meta(buildMetaInfo(servletRequest))
                .build();
    }
}
